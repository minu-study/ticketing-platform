package kr.hhplus.be.server.integration;

import kr.hhplus.be.server.api.payment.service.PaymentService;
import kr.hhplus.be.server.common.exception.AppException;
import kr.hhplus.be.server.domain.payment.dto.PaymentDto;
import kr.hhplus.be.server.domain.queueToken.entity.QueueToken;
import kr.hhplus.be.server.domain.queueToken.repository.QueueTokenRepository;
import kr.hhplus.be.server.domain.reservation.entity.Reservation;
import kr.hhplus.be.server.domain.reservation.repository.ReservationRepository;
import kr.hhplus.be.server.domain.reservation.vo.ReservationStatusEnums;
import kr.hhplus.be.server.domain.seat.entity.Seat;
import kr.hhplus.be.server.domain.seat.repository.SeatRepository;
import kr.hhplus.be.server.domain.seat.vo.SeatStatusEnums;
import kr.hhplus.be.server.domain.seat.vo.SeatTypeAndValueEnums;
import kr.hhplus.be.server.domain.user.entity.User;
import kr.hhplus.be.server.domain.user.repository.UserRepository;
import kr.hhplus.be.server.support.TestContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("결제 통합 테스트")
@Transactional
@Rollback
public class PaymentIntegrationTest extends TestContainer {

    @Autowired
    private PaymentService paymentService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private QueueTokenRepository queueTokenRepository;

    private User testUser;
    private Reservation testReservation;
    private Seat testSeat;
    private QueueToken testToken;

    @BeforeEach
    void setUp() {

        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUserName("결제테스트유저");
        testUser.setEmail("payment@test.com");
        testUser.setBalance(100000);
        userRepository.save(testUser);

        testSeat = Seat.builder()
                .scheduleId(1L)
                .seatNumber(1)
                .seatType(SeatTypeAndValueEnums.VIP.getType())
                .status(SeatStatusEnums.HOLD.getStatus())
                .build();
        seatRepository.save(testSeat);

        testReservation = Reservation.builder()
                .userId(testUser.getId())
                .seatId(testSeat.getId())
                .scheduleId(1L)
                .status(ReservationStatusEnums.TEMP.getStatus())
                .reservedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .build();
        reservationRepository.save(testReservation);

        testToken = QueueToken.builder()
                .userId(testUser.getId())
                .token("test-token-123")
                .position(1)
                .issuedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .build();
        queueTokenRepository.save(testToken);

        // 헤더에 토큰 설정
        setAuthToken(testToken.getToken());
    }


    @Test
    @DisplayName("정상 결제 시나리오")
    void paymentSuccessProcess() {

        // given
        PaymentDto.SetPayment.Request request = new PaymentDto.SetPayment.Request();
        request.setReservationId(testReservation.getId());

        // when
        paymentService.processPayment(request);

        // then
        User user = userRepository.findById(testUser.getId()).get();
        assertThat(user.getBalance()).isEqualTo(50000);

        // 예약 상태가 CONFIRMED로 변경되었는지 확인
        Reservation reservation = reservationRepository.findById(testReservation.getId()).get();
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatusEnums.CONFIRMED.getStatus());

        // 좌석 상태가 RESERVED로 변경되었는지 확인
        Seat seat = seatRepository.findById(testSeat.getId()).get();
        assertThat(seat.getStatus()).isEqualTo(SeatStatusEnums.RESERVED.getStatus());
    }

    @Test
    @DisplayName("잔액 부족으로 결제 실패")
    void paymentFailProcessWhenInsufficientBalance() {

        // given
        testUser.setBalance(10000);
        userRepository.save(testUser);

        PaymentDto.SetPayment.Request request = new PaymentDto.SetPayment.Request();
        request.setReservationId(testReservation.getId());

        // when & then
        assertThatThrownBy(() -> paymentService.processPayment(request))
                .isInstanceOf(AppException.class);

        User user = userRepository.findById(testUser.getId()).get();
        assertThat(user.getBalance()).isEqualTo(10000);

        Reservation reservation = reservationRepository.findById(testReservation.getId()).get();
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatusEnums.TEMP.getStatus());
    }

    @Test
    @DisplayName("만료된 예약으로 결제 실패")
    void paymentFailProcessWhenReservationExpired() {

        // given
        testReservation.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        reservationRepository.save(testReservation);

        PaymentDto.SetPayment.Request request = new PaymentDto.SetPayment.Request();
        request.setReservationId(testReservation.getId());

        // when & then
        assertThatThrownBy(() -> paymentService.processPayment(request))
                .isInstanceOf(AppException.class);
    }

    private void setAuthToken(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Auth-Queue-Token", token);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }


}
