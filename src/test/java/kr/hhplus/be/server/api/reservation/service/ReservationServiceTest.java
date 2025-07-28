package kr.hhplus.be.server.api.reservation.service;

import kr.hhplus.be.server.api.payment.service.PaymentService;
import kr.hhplus.be.server.api.queue.service.QueueService;
import kr.hhplus.be.server.common.exception.AppException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.util.CommonUtil;
import kr.hhplus.be.server.domain.eventSchedule.repository.EventScheduleRepository;
import kr.hhplus.be.server.domain.queueToken.dto.QueueDto;
import kr.hhplus.be.server.domain.reservation.dto.ReservationDto;
import kr.hhplus.be.server.domain.reservation.entity.Reservation;
import kr.hhplus.be.server.domain.reservation.repository.ReservationRepository;
import kr.hhplus.be.server.domain.reservation.vo.ReservationStatusEnums;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private EventScheduleRepository eventScheduleRepository;

    @Mock
    private QueueService queueService;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private ReservationService reservationService;

    private String testToken;
    private UUID testUserId;
    private Long testSeatId;
    private Long testScheduleId;
    private Long testReservationId;
    private Seat testSeat;
    private Reservation testReservation;

    @BeforeEach
    void setUp() {
        testToken = "test-token-123";
        testUserId = UUID.randomUUID();
        testSeatId = 1L;
        testScheduleId = 1L;
        testReservationId = 1L;

        testSeat = new Seat();
        testSeat.setId(testSeatId);
        testSeat.setScheduleId(testScheduleId);
        testSeat.setSeatNumber(1);
        testSeat.setSeatType(SeatTypeAndValueEnums.STANDARD.getType());
        testSeat.setStatus(SeatStatusEnums.AVAILABLE.getStatus());

        testReservation = new Reservation();
        testReservation.setId(testReservationId);
        testReservation.setUserId(testUserId);
        testReservation.setSeatId(testSeatId);
        testReservation.setScheduleId(testScheduleId);
        testReservation.setStatus(ReservationStatusEnums.TEMP.getStatus());
        testReservation.setReservedAt(LocalDateTime.now());
        testReservation.setExpiresAt(LocalDateTime.now().plusMinutes(5));
    }

    @Test
    @DisplayName("좌석 예약 생성 - 성공")
    void createReservation_success() {

        // Given
        ReservationDto.SetReservation.Request request = new ReservationDto.SetReservation.Request();
        request.setScheduleId(testScheduleId);
        request.setSeatId(testSeatId);

        QueueDto.QueueTokenValidationView tokenInfo = QueueDto.QueueTokenValidationView.builder()
                .token(testToken)
                .userId(testUserId)
                .build();

        try (MockedStatic<CommonUtil> mockedCommonUtil = mockStatic(CommonUtil.class)) {
            mockedCommonUtil.when(CommonUtil::getQueueToken).thenReturn(testToken);
            
            when(queueService.validateToken(testToken)).thenReturn(tokenInfo);
            when(seatRepository.findById(testSeatId)).thenReturn(Optional.of(testSeat));
            when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);
            when(seatRepository.save(any(Seat.class))).thenReturn(testSeat);

            // When
            ReservationDto.SetReservation.Response response = reservationService.createReservation(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getReservationId()).isEqualTo(testReservationId);
            
            verify(queueService).validateToken(testToken);
            verify(seatRepository).findById(testSeatId);
            verify(reservationRepository).save(any(Reservation.class));
            verify(seatRepository).save(any(Seat.class));
            
            // 좌석 상태가 HOLD로 변경되었는지 확인
            assertThat(testSeat.getStatus()).isEqualTo(SeatStatusEnums.HOLD.getStatus());
            assertThat(testSeat.getHoldExpiresAt()).isNotNull();
        }
    }

    @Test
    @DisplayName("좌석 예약 생성 - 토큰 검증 실패")
    void createReservation_tokenValidationFails() {

        // Given
        ReservationDto.SetReservation.Request request = new ReservationDto.SetReservation.Request();
        request.setScheduleId(testScheduleId);
        request.setSeatId(testSeatId);

        try (MockedStatic<CommonUtil> mockedCommonUtil = mockStatic(CommonUtil.class)) {
            mockedCommonUtil.when(CommonUtil::getQueueToken).thenReturn(testToken);
            
            when(queueService.validateToken(testToken))
                    .thenThrow(new AppException(ErrorCode.INVALID_TOKEN));

            // When & Then
            assertThatThrownBy(() -> reservationService.createReservation(request))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_TOKEN.getCode());
            
            verify(queueService).validateToken(testToken);
            verify(seatRepository, never()).findById(any());
            verify(reservationRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("좌석 예약 생성 - 좌석을 찾을 수 없는 경우")
    void createReservation_seatNotFound() {

        // Given
        ReservationDto.SetReservation.Request request = new ReservationDto.SetReservation.Request();
        request.setScheduleId(testScheduleId);
        request.setSeatId(testSeatId);

        QueueDto.QueueTokenValidationView tokenInfo = QueueDto.QueueTokenValidationView.builder()
                .token(testToken)
                .userId(testUserId)
                .build();

        try (MockedStatic<CommonUtil> mockedCommonUtil = mockStatic(CommonUtil.class)) {
            mockedCommonUtil.when(CommonUtil::getQueueToken).thenReturn(testToken);
            
            when(queueService.validateToken(testToken)).thenReturn(tokenInfo);
            when(seatRepository.findById(testSeatId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> reservationService.createReservation(request))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DATA_NOT_FOUND.getCode());
            
            verify(queueService).validateToken(testToken);
            verify(seatRepository).findById(testSeatId);
            verify(reservationRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("좌석 예약 생성 - 예약 불가능한 좌석인 경우")
    void createReservation_seatNotAvailable() {

        // Given
        ReservationDto.SetReservation.Request request = new ReservationDto.SetReservation.Request();
        request.setScheduleId(testScheduleId);
        request.setSeatId(testSeatId);

        // 이미 예약된 좌석으로 설정
        testSeat.setStatus(SeatStatusEnums.RESERVED.getStatus());

        QueueDto.QueueTokenValidationView tokenInfo = QueueDto.QueueTokenValidationView.builder()
                .token(testToken)
                .userId(testUserId)
                .build();

        try (MockedStatic<CommonUtil> mockedCommonUtil = mockStatic(CommonUtil.class)) {
            mockedCommonUtil.when(CommonUtil::getQueueToken).thenReturn(testToken);
            
            when(queueService.validateToken(testToken)).thenReturn(tokenInfo);
            when(seatRepository.findById(testSeatId)).thenReturn(Optional.of(testSeat));

            // When & Then
            assertThatThrownBy(() -> reservationService.createReservation(request))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SEAT_NOT_AVAILABLE.getCode());
            
            verify(queueService).validateToken(testToken);
            verify(seatRepository).findById(testSeatId);
            verify(reservationRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("예약 목록 조회 - 성공")
    void getReservationList_success() {

        // Given
        List<ReservationDto.ReservationSummaryView> mockReservationList = Arrays.asList(
                ReservationDto.ReservationSummaryView.builder()
                        .id(1L)
                        .eventCode("EVENT001")
                        .eventName("정민우1 콘서트")
                        .seatNumber(1)
                        .status(ReservationStatusEnums.TEMP.getStatus())
                        .reservedAt(LocalDateTime.now())
                        .expiresAt(LocalDateTime.now().plusMinutes(5))
                        .build()
        );

        QueueDto.QueueTokenValidationView tokenInfo = QueueDto.QueueTokenValidationView.builder()
                .token(testToken)
                .userId(testUserId)
                .build();

        try (MockedStatic<CommonUtil> mockedCommonUtil = mockStatic(CommonUtil.class)) {
            mockedCommonUtil.when(CommonUtil::getQueueToken).thenReturn(testToken);
            
            when(queueService.validateToken(testToken)).thenReturn(tokenInfo);
            when(reservationRepository.getReservationList(testUserId)).thenReturn(mockReservationList);

            // When
            ReservationDto.GetReservationList.Response response = reservationService.getReservationList();

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getList()).hasSize(1);
            assertThat(response.getList().get(0).getEventName()).isEqualTo("정민우1 콘서트");
            
            verify(queueService).validateToken(testToken);
            verify(reservationRepository).getReservationList(testUserId);
        }
    }

    @Test
    @DisplayName("예약 확정 - 성공")
    void confirmReservation_success() {

        // Given
        when(reservationRepository.findById(testReservationId)).thenReturn(Optional.of(testReservation));
        when(seatRepository.findById(testSeatId)).thenReturn(Optional.of(testSeat));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);
        when(seatRepository.save(any(Seat.class))).thenReturn(testSeat);

        // When
        reservationService.confirmReservation(testReservationId);

        // Then
        verify(reservationRepository).findById(testReservationId);
        verify(seatRepository).findById(testSeatId);
        verify(reservationRepository).save(testReservation);
        verify(seatRepository).save(testSeat);
        
        assertThat(testReservation.getStatus()).isEqualTo(ReservationStatusEnums.CONFIRMED.getStatus());
        assertThat(testSeat.getStatus()).isEqualTo(SeatStatusEnums.RESERVED.getStatus());
    }
}