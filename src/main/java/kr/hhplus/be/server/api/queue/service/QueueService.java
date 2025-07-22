package kr.hhplus.be.server.api.queue.service;

import kr.hhplus.be.server.api.queue.dto.QueueDto;
import kr.hhplus.be.server.common.exception.AppException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.util.CommonUtil;
import kr.hhplus.be.server.domain.queueToken.entity.QueueToken;
import kr.hhplus.be.server.domain.queueToken.repository.QueueTokenRepository;
import kr.hhplus.be.server.domain.queueToken.vo.TokenStatusEnums;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QueueService {

    private final QueueTokenRepository queueTokenRepository;
    
    // 대기열 정책 설정
    private static final int TOKEN_EXPIRY_MINUTES = 30; // 토큰 만료 시간 (분)
    private static final int MAX_ACTIVE_USERS = 20; // 동시 활성 사용자 수
    private static final int ESTIMATED_PROCESSING_TIME_PER_USER = 5; // 사용자당 예상 처리 시간 (분)


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
            throw new AppException(ErrorCode.AUTH004);
        }
        QueueToken queueToken = queueTokenOptional.get();

        if (queueToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.AUTH005);
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
     */
    @Transactional(readOnly = true)
    public QueueDto.ValidationResponse validateToken(String token) {
        Optional<QueueToken> queueTokenOptional = queueTokenRepository.findByToken(token);
        
        if (queueTokenOptional.isEmpty()) {
            return QueueDto.ValidationResponse.builder()
                    .valid(false)
                    .message("Token not found")
                    .build();
        }
        QueueToken queueToken = queueTokenOptional.get();

        if (queueToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            return QueueDto.ValidationResponse.builder()
                    .valid(false)
                    .userId(queueToken.getUserId())
                    .status("EXPIRED")
                    .message("Token expired")
                    .build();
        }

        int currentPosition = getCurrentPosition(queueToken);
        String status = determineTokenStatus(queueToken, currentPosition);
        boolean isActive = "ACTIVE".equals(status);

        return QueueDto.ValidationResponse.builder()
                .valid(isActive)
                .userId(queueToken.getUserId())
                .status(status)
                .message(isActive ? "Token is valid and active" : "Token is valid but waiting")
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