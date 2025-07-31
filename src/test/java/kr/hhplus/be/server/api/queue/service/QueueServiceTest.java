package kr.hhplus.be.server.api.queue.service;

import kr.hhplus.be.server.common.exception.AppException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.domain.queueToken.dto.QueueDto;
import kr.hhplus.be.server.domain.queueToken.entity.QueueToken;
import kr.hhplus.be.server.domain.queueToken.repository.QueueTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QueueServiceTest {

    @Mock
    private QueueTokenRepository queueTokenRepository;

    @InjectMocks
    private QueueService queueService;

    private UUID testUserId;
    private String testToken;
    private QueueToken testQueueToken;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testToken = "test-token-123";
        testQueueToken = QueueToken.builder()
                .id(1L)
                .userId(testUserId)
                .token(testToken)
                .position(5)
                .issuedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .build();
    }

    @Test
    @DisplayName("새로운 토큰 발급 - 성공")
    void issueToken_success() {

        // Given
        QueueDto.GetQueueToken.Request request = new QueueDto.GetQueueToken.Request();
        request.setUserId(testUserId);

        when(queueTokenRepository.findByUserIdAndExpiresAtAfter(eq(testUserId), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());
        when(queueTokenRepository.findMaxPositionByExpiresAtAfter(any(LocalDateTime.class)))
                .thenReturn(4);
        when(queueTokenRepository.save(any(QueueToken.class)))
                .thenReturn(testQueueToken);

        // When
        QueueDto.GetQueueToken.Response response = queueService.issueToken(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getView()).isNotNull();
        verify(queueTokenRepository).save(any(QueueToken.class));
    }

    @Test
    @DisplayName("기존 토큰 반환 - 유효한 토큰이 이미 존재하는 경우")
    void issueToken_returnExistingToken() {

        // Given
        QueueDto.GetQueueToken.Request request = new QueueDto.GetQueueToken.Request();
        request.setUserId(testUserId);

        when(queueTokenRepository.findByUserIdAndExpiresAtAfter(eq(testUserId), any(LocalDateTime.class)))
                .thenReturn(Optional.of(testQueueToken));

        // When
        QueueDto.GetQueueToken.Response response = queueService.issueToken(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getView()).isNotNull();
        verify(queueTokenRepository, never()).save(any(QueueToken.class));
    }

    @Test
    @DisplayName("대기열 위치 조회 - 성공")
    void getQueuePosition_success() {

        // Given
        try (MockedStatic<CommonUtil> mockedCommonUtil = mockStatic(CommonUtil.class)) {
            mockedCommonUtil.when(CommonUtil::getQueueToken).thenReturn(testToken);
            
            when(queueTokenRepository.findByToken(testToken))
                    .thenReturn(Optional.of(testQueueToken));
            when(queueTokenRepository.countByPositionLessThanAndExpiresAtAfter(
                    eq(testQueueToken.getPosition()), any(LocalDateTime.class)))
                    .thenReturn(3);

            // When
            QueueDto.GetQueuePosition.Response response = queueService.getQueuePosition();

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getView()).isNotNull();
            assertThat(response.getView().getToken()).isEqualTo(testToken);
            assertThat(response.getView().getUserId()).isEqualTo(testUserId);
        }
    }

    @Test
    @DisplayName("대기열 위치 조회 - 토큰이 존재하지 않는 경우")
    void getQueuePosition_tokenNotFound() {

        // Given
        try (MockedStatic<CommonUtil> mockedCommonUtil = mockStatic(CommonUtil.class)) {
            mockedCommonUtil.when(CommonUtil::getQueueToken).thenReturn(testToken);
            
            when(queueTokenRepository.findByToken(testToken))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> queueService.getQueuePosition())
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_TOKEN.getCode());
        }
    }

    @Test
    @DisplayName("대기열 위치 조회 - 토큰이 만료된 경우")
    void getQueuePosition_tokenExpired() {

        // Given
        QueueToken expiredToken = QueueToken.builder()
                .id(1L)
                .userId(testUserId)
                .token(testToken)
                .position(5)
                .issuedAt(LocalDateTime.now().minusHours(1))
                .expiresAt(LocalDateTime.now().minusMinutes(10))
                .build();

        try (MockedStatic<CommonUtil> mockedCommonUtil = mockStatic(CommonUtil.class)) {
            mockedCommonUtil.when(CommonUtil::getQueueToken).thenReturn(testToken);
            
            when(queueTokenRepository.findByToken(testToken))
                    .thenReturn(Optional.of(expiredToken));

            // When & Then
            assertThatThrownBy(() -> queueService.getQueuePosition())
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EXPIRED_TOKEN.getCode());
        }
    }

    @Test
    @DisplayName("토큰 유효성 검증 - 성공")
    void validateToken_success() {

        // Given
        when(queueTokenRepository.findByToken(testToken))
                .thenReturn(Optional.of(testQueueToken));

        // When
        QueueDto.QueueTokenValidationView result = queueService.validateToken(testToken);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo(testToken);
        assertThat(result.getUserId()).isEqualTo(testUserId);
    }

    @Test
    @DisplayName("토큰 유효성 검증 - 토큰이 존재하지 않는 경우")
    void validateToken_tokenNotFound() {

        // Given
        when(queueTokenRepository.findByToken(testToken))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> queueService.validateToken(testToken))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_TOKEN.getCode());
    }

    @Test
    @DisplayName("토큰 유효성 검증 - 토큰이 만료된 경우")
    void validateToken_tokenExpired() {

        // Given
        QueueToken expiredToken = QueueToken.builder()
                .id(1L)
                .userId(testUserId)
                .token(testToken)
                .position(5)
                .issuedAt(LocalDateTime.now().minusHours(1))
                .expiresAt(LocalDateTime.now().minusMinutes(10))
                .build();

        when(queueTokenRepository.findByToken(testToken))
                .thenReturn(Optional.of(expiredToken));

        // When & Then
        assertThatThrownBy(() -> queueService.validateToken(testToken))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EXPIRED_TOKEN.getCode());
    }

    @Test
    @DisplayName("토큰 만료 처리 - 성공")
    void expireToken_success() {

        // Given
        when(queueTokenRepository.findByToken(testToken))
                .thenReturn(Optional.of(testQueueToken));

        // When
        queueService.expireToken(testToken);

        // Then
        verify(queueTokenRepository).findByToken(testToken);
        assertThat(testQueueToken.getExpiresAt()).isBefore(LocalDateTime.now().plusSeconds(1));
    }

    @Test
    @DisplayName("토큰 만료 처리 - 토큰이 존재하지 않는 경우")
    void expireToken_tokenNotFound() {

        // Given
        when(queueTokenRepository.findByToken(testToken))
                .thenReturn(Optional.empty());

        // When
        queueService.expireToken(testToken);

        // Then
        verify(queueTokenRepository).findByToken(testToken);
    }
}