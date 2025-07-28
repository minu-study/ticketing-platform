package kr.hhplus.be.server.api.queue.service;

import kr.hhplus.be.server.domain.queueToken.dto.QueueDto;
import kr.hhplus.be.server.common.exception.AppException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.util.CommonUtil;
import kr.hhplus.be.server.domain.queueToken.entity.QueueToken;
import kr.hhplus.be.server.domain.queueToken.repository.QueueTokenRepository;
import kr.hhplus.be.server.domain.queueToken.vo.TokenStatusEnums;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueueService {

    private final QueueTokenRepository queueTokenRepository;
    
    // 대기열 정책 설정
    private static final int TOKEN_EXPIRY_MINUTES = 30; // 토큰 만료 시간 (분)
    private static final int MAX_ACTIVE_USERS = 20; // 동시 활성 사용자 수
    private static final int ESTIMATED_PROCESSING_TIME_PER_USER = 5; // 사용자당 예상 처리 시간 (분)
    private static final int EXTENSION_MINUTES = 5; // 연장 시간 (분)

    @Transactional
    public QueueDto.GetQueueToken.Response issueToken(QueueDto.GetQueueToken.Request param) {

        Optional<QueueToken> existingTokenOptional = queueTokenRepository.findByUserIdAndExpiresAtAfter(param.getUserId(), LocalDateTime.now());
        if (existingTokenOptional.isPresent()) {
            QueueToken existingToken = existingTokenOptional.get();
            return QueueDto.GetQueueToken.Response.builder()
                    .view(existingToken.getTokenInfo())
                    .build();
        }

        QueueToken queueToken = QueueToken.builder()
                .userId(param.getUserId())
                .token(generateToken())
                .position(getNextPosition())
                .issuedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(TOKEN_EXPIRY_MINUTES))
                .build();
        queueTokenRepository.save(queueToken);
        
        return QueueDto.GetQueueToken.Response.builder()
                .view(queueToken.getTokenInfo())
                .build();
    }


    @Transactional(readOnly = true)
    public QueueDto.GetQueuePosition.Response getQueuePosition() {

        String token = CommonUtil.getQueueToken();

        Optional<QueueToken> queueTokenOptional = queueTokenRepository.findByToken(token);
        if (queueTokenOptional.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
        QueueToken queueToken = queueTokenOptional.get();

        if (queueToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.EXPIRED_TOKEN);
        }

        int currentPosition = getCurrentPosition(queueToken);
        int estimatedWaitTime = calculateEstimatedWaitTime(currentPosition);
        String status = determineTokenStatus(queueToken, currentPosition);

        QueueDto.QueuePositionView view = QueueDto.QueuePositionView.builder()
                .token(token)
                .userId(queueToken.getUserId())
                .position(currentPosition)
                .estimatedWaitTime(estimatedWaitTime)
                .status(status)
                .expiresAt(queueToken.getExpiresAt())
                .build();

        return QueueDto.GetQueuePosition.Response.builder()
                .view(view)
                .build();
    }

    /**
     * 토큰 유효성 검증
     * 아직 대기 순번일때 예외 발생
     */
    @Transactional(readOnly = true)
    public QueueDto.QueueTokenValidationView validateToken(String token) {

        Optional<QueueToken> queueTokenOptional = queueTokenRepository.findByToken(token);
        
        if (queueTokenOptional.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
        QueueToken queueToken = queueTokenOptional.get();

        if (queueToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.EXPIRED_TOKEN);
        }

        // FIXME: 인증토큰을 겸하기 때문에 대기열 상태 여부에 따라 예외 발생시키면 안됨..설계 미스
//        int currentPosition = getCurrentPosition(queueToken);
//        String status = determineTokenStatus(queueToken, currentPosition);
//
//        if (!status.equals(TokenStatusEnums.ACTIVE.getStatus())) {
//            throw new AppException(ErrorCode.QUEUE001);
//        }

        return QueueDto.QueueTokenValidationView.builder()
                .token(queueToken.getToken())
                .userId(queueToken.getUserId())
                .build();
    }

    /**
     * 토큰 만료 처리 (결제 완료 시 호출)
     */
    @Transactional
    public void expireToken(String token) {
        Optional<QueueToken> queueTokenOptional = queueTokenRepository.findByToken(token);
        queueTokenOptional.ifPresent(queueToken -> queueToken.setExpiresAt(LocalDateTime.now()));
    }

    /**
     * 토큰 만료 시간 연장 (결제 과정에서 호출)
     * 최대 3회까지만 연장 가능
     */
    @Transactional
    public Boolean extendToken() {

        String token = CommonUtil.getQueueToken();

        Optional<QueueToken> queueTokenOptional = queueTokenRepository.findByToken(token);
        
        if (queueTokenOptional.isEmpty()) {
            return Boolean.FALSE;
        }
        QueueToken queueToken = queueTokenOptional.get();

        // 토큰이 이미 만료시간을 넘겼다면 X
        if (queueToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            return Boolean.FALSE;
        }

        // 토큰이 현재 활성상태가 아니라면 X
        int currentPosition = getCurrentPosition(queueToken);
        String status = determineTokenStatus(queueToken, currentPosition);
        if (!status.equals(TokenStatusEnums.ACTIVE.getStatus())) {
            return Boolean.FALSE;
        }

        // 만료시간 * 3 이 최대 연장된 시간이고 이걸 넘겼다면 발행 X
        int maxAllowedExpiryMinutes = EXTENSION_MINUTES * 3;
        LocalDateTime maxAllowedExpiry = queueToken.getIssuedAt().plusMinutes(maxAllowedExpiryMinutes);
        LocalDateTime newExpiry = queueToken.getExpiresAt().plusMinutes(EXTENSION_MINUTES);
        
        if (newExpiry.isAfter(maxAllowedExpiry)) {
            return Boolean.FALSE;
        }
        
        queueToken.setExpiresAt(newExpiry);
        queueTokenRepository.save(queueToken);
        
        return true;
    }


    @Transactional
    public void cleanupExpiredTokens(LocalDateTime now) {

        try {
            List<QueueToken> expiredTokens = queueTokenRepository.findByExpiresAtBefore(now);
            if (!expiredTokens.isEmpty()) {
                queueTokenRepository.deleteAll(expiredTokens);
            }
        } catch (Exception e) {
            log.error("cleanupExpiredTokens_failed to process error : {}", e.getMessage());
        }
    }

    @Transactional
    public void reorderTokens(LocalDateTime now) {
        try {

            List<QueueToken> activeTokens = queueTokenRepository.findByExpiresAtAfterOrderByIssuedAt(now);

            if (!activeTokens.isEmpty()) {

                for (int i = 0; i < activeTokens.size(); i++) {
                    QueueToken token = activeTokens.get(i);
                    int newPosition = i + 1;

                    if (token.getPosition() != newPosition) {
                        token.setPosition(newPosition);
                        queueTokenRepository.save(token);
                    }
                }
            }

        } catch (Exception e) {
            log.error("reorderTokens_failed to process error : {}", e.getMessage());
        }
    }

    private String generateToken() {
        return UUID.randomUUID().toString();
    }

    private int getNextPosition() {
        // 현재 대기열에서 가장 큰 position + 1
        Integer maxPosition = queueTokenRepository.findMaxPositionByExpiresAtAfter(LocalDateTime.now());
        return (maxPosition != null ? maxPosition : 0) + 1;
    }

    private int getCurrentPosition(QueueToken queueToken) {
        // 해당 토큰보다 앞선 순서의 활성 토큰 수 계산
        return queueTokenRepository.countByPositionLessThanAndExpiresAtAfter(
                queueToken.getPosition(), LocalDateTime.now());
    }

    private int calculateEstimatedWaitTime(int position) {
        // 현재 활성 사용자 수를 고려한 예상 대기 시간 계산
        int waitingAhead = Math.max(0, position - MAX_ACTIVE_USERS);
        return waitingAhead * ESTIMATED_PROCESSING_TIME_PER_USER;
    }

    private String determineTokenStatus(QueueToken queueToken, int currentPosition) {
        if (queueToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            return TokenStatusEnums.EXPIRED.getStatus();
        }
        return currentPosition <= MAX_ACTIVE_USERS ? TokenStatusEnums.ACTIVE.getStatus() : TokenStatusEnums.WAITING.getStatus();
    }
}