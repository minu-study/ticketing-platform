package kr.hhplus.be.server.api.seat.service;

import kr.hhplus.be.server.api.queue.service.QueueService;
import kr.hhplus.be.server.common.exception.AppException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.domain.queueToken.dto.QueueDto;
import kr.hhplus.be.server.domain.seat.dto.SeatDto;
import kr.hhplus.be.server.domain.seat.entity.Seat;
import kr.hhplus.be.server.domain.seat.repository.SeatRepository;
import kr.hhplus.be.server.domain.seat.vo.SeatStatusEnums;
import kr.hhplus.be.server.domain.seat.vo.SeatTypeAndValueEnums;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeatServiceTest {

    @Mock
    private QueueService queueService;

    @Mock
    private SeatRepository seatRepository;

    @InjectMocks
    private SeatService seatService;

    private String testToken;
    private Long testScheduleId;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testToken = "test-token-123";
        testScheduleId = 1L;
        testUserId = UUID.randomUUID();
    }

    @Test
    @DisplayName("좌석 목록 조회 - 성공")
    void getSeats_success() {

        // Given
        SeatDto.getSeats.Request request = new SeatDto.getSeats.Request();
        request.setScheduleId(testScheduleId);

        List<SeatDto.SeatView> mockSeatList = Arrays.asList(
                SeatDto.SeatView.builder()
                        .eventScheduleId(testScheduleId)
                        .seatNumber(1)
                        .seatType(SeatTypeAndValueEnums.STANDARD.getType())
                        .status(SeatStatusEnums.AVAILABLE.getStatus())
                        .build(),
                SeatDto.SeatView.builder()
                        .eventScheduleId(testScheduleId)
                        .seatNumber(2)
                        .seatType(SeatTypeAndValueEnums.STANDARD.getType())
                        .status(SeatStatusEnums.AVAILABLE.getStatus())
                        .build(),
                SeatDto.SeatView.builder()
                        .eventScheduleId(testScheduleId)
                        .seatNumber(3)
                        .seatType(SeatTypeAndValueEnums.VIP.getType())
                        .status(SeatStatusEnums.RESERVED.getStatus())
                        .build()
        );

        QueueDto.QueueTokenValidationView validationView = QueueDto.QueueTokenValidationView.builder()
                .token(testToken)
                .userId(testUserId)
                .build();

        try (MockedStatic<CommonUtil> mockedCommonUtil = mockStatic(CommonUtil.class)) {
            mockedCommonUtil.when(CommonUtil::getQueueToken).thenReturn(testToken);
            
            when(queueService.validateToken(testToken)).thenReturn(validationView);
            when(seatRepository.getSeats(eq(testScheduleId), any(LocalDateTime.class)))
                    .thenReturn(mockSeatList);

            // When
            SeatDto.getSeats.Response response = seatService.getSeats(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getList()).hasSize(3);
            assertThat(response.getList().get(0).getSeatNumber()).isEqualTo(1);
            assertThat(response.getList().get(0).getStatus()).isEqualTo(SeatStatusEnums.AVAILABLE.getStatus());
            assertThat(response.getList().get(2).getSeatType()).isEqualTo(SeatTypeAndValueEnums.VIP.getType());
            assertThat(response.getList().get(2).getStatus()).isEqualTo(SeatStatusEnums.RESERVED.getStatus());
            
            verify(queueService).validateToken(testToken);
            verify(seatRepository).getSeats(eq(testScheduleId), any(LocalDateTime.class));
        }
    }

    @Test
    @DisplayName("좌석 목록 조회 - 토큰 검증 실패")
    void getSeats_tokenValidationFails() {

        // Given
        SeatDto.getSeats.Request request = new SeatDto.getSeats.Request();
        request.setScheduleId(testScheduleId);

        try (MockedStatic<CommonUtil> mockedCommonUtil = mockStatic(CommonUtil.class)) {
            mockedCommonUtil.when(CommonUtil::getQueueToken).thenReturn(testToken);
            
            when(queueService.validateToken(testToken))
                    .thenThrow(new AppException(ErrorCode.INVALID_TOKEN));

            // When & Then
            assertThatThrownBy(() -> seatService.getSeats(request))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_TOKEN.getCode());
            
            verify(queueService).validateToken(testToken);
            verify(seatRepository, never()).getSeats(any(), any());
        }
    }

    @Test
    @DisplayName("좌석 목록 조회 - 빈 목록 반환")
    void getSeats_returnEmptyList() {
        // Given
        SeatDto.getSeats.Request request = new SeatDto.getSeats.Request();
        request.setScheduleId(testScheduleId);

        QueueDto.QueueTokenValidationView validationView = QueueDto.QueueTokenValidationView.builder()
                .token(testToken)
                .userId(testUserId)
                .build();

        try (MockedStatic<CommonUtil> mockedCommonUtil = mockStatic(CommonUtil.class)) {
            mockedCommonUtil.when(CommonUtil::getQueueToken).thenReturn(testToken);
            
            when(queueService.validateToken(testToken)).thenReturn(validationView);
            when(seatRepository.getSeats(eq(testScheduleId), any(LocalDateTime.class)))
                    .thenReturn(Arrays.asList());

            // When
            SeatDto.getSeats.Response response = seatService.getSeats(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getList()).isEmpty();
            
            verify(queueService).validateToken(testToken);
            verify(seatRepository).getSeats(eq(testScheduleId), any(LocalDateTime.class));
        }
    }

    @Test
    @DisplayName("좌석 동기화 - 만료된 HOLD 좌석들을 AVAILABLE로 변경")
    void syncSeats_success_whenExpiredHoldSeatsExist() {

        // Given
        LocalDateTime now = LocalDateTime.now();
        
        Seat expiredSeat1 = new Seat();
        expiredSeat1.setId(1L);
        expiredSeat1.setScheduleId(testScheduleId);
        expiredSeat1.setSeatNumber(1);
        expiredSeat1.setSeatType(SeatTypeAndValueEnums.STANDARD.getType());
        expiredSeat1.setStatus(SeatStatusEnums.HOLD.getStatus());
        expiredSeat1.setHoldExpiresAt(now.minusMinutes(5));
        
        Seat expiredSeat2 = new Seat();
        expiredSeat2.setId(2L);
        expiredSeat2.setScheduleId(testScheduleId);
        expiredSeat2.setSeatNumber(2);
        expiredSeat2.setSeatType(SeatTypeAndValueEnums.STANDARD.getType());
        expiredSeat2.setStatus(SeatStatusEnums.HOLD.getStatus());
        expiredSeat2.setHoldExpiresAt(now.minusMinutes(3));

        List<Seat> expiredHoldSeats = Arrays.asList(expiredSeat1, expiredSeat2);

        when(seatRepository.findByStatusAndHoldExpiresAtBefore(
                SeatStatusEnums.HOLD.getStatus(), now))
                .thenReturn(expiredHoldSeats);
        when(seatRepository.save(any(Seat.class))).thenReturn(expiredSeat1);

        // When
        seatService.syncSeats(now);

        // Then
        verify(seatRepository).findByStatusAndHoldExpiresAtBefore(
                SeatStatusEnums.HOLD.getStatus(), now);
        verify(seatRepository, times(2)).save(any(Seat.class));
        
        assertThat(expiredSeat1.getStatus()).isEqualTo(SeatStatusEnums.AVAILABLE.getStatus());
        assertThat(expiredSeat1.getHoldExpiresAt()).isNull();
        assertThat(expiredSeat2.getStatus()).isEqualTo(SeatStatusEnums.AVAILABLE.getStatus());
        assertThat(expiredSeat2.getHoldExpiresAt()).isNull();
    }

    @Test
    @DisplayName("좌석 동기화 - 만료된 HOLD 좌석이 없는 경우")
    void syncSeats_nothing_whenNoExpiredHoldSeats() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        when(seatRepository.findByStatusAndHoldExpiresAtBefore(
                SeatStatusEnums.HOLD.getStatus(), now))
                .thenReturn(Arrays.asList());

        // When
        seatService.syncSeats(now);

        // Then
        verify(seatRepository).findByStatusAndHoldExpiresAtBefore(
                SeatStatusEnums.HOLD.getStatus(), now);
        verify(seatRepository, never()).save(any(Seat.class));
    }

    @Test
    @DisplayName("좌석 동기화 - 저장 중 예외 발생 시 로그 기록 후 계속 진행")
    void syncSeats_continueProcessing() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        Seat expiredSeat1 = new Seat();
        expiredSeat1.setId(1L);
        expiredSeat1.setScheduleId(testScheduleId);
        expiredSeat1.setSeatNumber(1);
        expiredSeat1.setSeatType(SeatTypeAndValueEnums.STANDARD.getType());
        expiredSeat1.setStatus(SeatStatusEnums.HOLD.getStatus());
        expiredSeat1.setHoldExpiresAt(now.minusMinutes(5));
        
        Seat expiredSeat2 = new Seat();
        expiredSeat2.setId(2L);
        expiredSeat2.setScheduleId(testScheduleId);
        expiredSeat2.setSeatNumber(2);
        expiredSeat2.setSeatType(SeatTypeAndValueEnums.STANDARD.getType());
        expiredSeat2.setStatus(SeatStatusEnums.HOLD.getStatus());
        expiredSeat2.setHoldExpiresAt(now.minusMinutes(3));

        List<Seat> expiredHoldSeats = Arrays.asList(expiredSeat1, expiredSeat2);

        when(seatRepository.findByStatusAndHoldExpiresAtBefore(
                SeatStatusEnums.HOLD.getStatus(), now))
                .thenReturn(expiredHoldSeats);
        
        when(seatRepository.save(expiredSeat1))
                .thenThrow(new RuntimeException("error"));
        when(seatRepository.save(expiredSeat2))
                .thenReturn(expiredSeat2);

        // When
        seatService.syncSeats(now);

        // Then
        verify(seatRepository).findByStatusAndHoldExpiresAtBefore(
                SeatStatusEnums.HOLD.getStatus(), now);
        verify(seatRepository, times(2)).save(any(Seat.class));
        
        assertThat(expiredSeat2.getStatus()).isEqualTo(SeatStatusEnums.AVAILABLE.getStatus());
        assertThat(expiredSeat2.getHoldExpiresAt()).isNull();
    }
}