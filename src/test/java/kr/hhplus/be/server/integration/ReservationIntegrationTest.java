package kr.hhplus.be.server.integration;

import kr.hhplus.be.server.api.event.service.EventService;
import kr.hhplus.be.server.api.payment.service.PaymentService;
import kr.hhplus.be.server.api.queue.service.QueueService;
import kr.hhplus.be.server.api.reservation.service.ReservationService;
import kr.hhplus.be.server.api.seat.service.SeatService;
import kr.hhplus.be.server.domain.category.entity.Category;
import kr.hhplus.be.server.domain.category.repository.CategoryRepository;
import kr.hhplus.be.server.domain.event.dto.EventDto;
import kr.hhplus.be.server.domain.event.entity.Event;
import kr.hhplus.be.server.domain.event.repository.EventRepository;
import kr.hhplus.be.server.domain.eventSchedule.entity.EventSchedule;
import kr.hhplus.be.server.domain.eventSchedule.repository.EventScheduleRepository;
import kr.hhplus.be.server.domain.payment.dto.PaymentDto;
import kr.hhplus.be.server.domain.queueToken.dto.QueueDto;
import kr.hhplus.be.server.domain.queueToken.entity.QueueToken;
import kr.hhplus.be.server.domain.queueToken.repository.QueueTokenRepository;
import kr.hhplus.be.server.domain.reservation.dto.ReservationDto;
import kr.hhplus.be.server.domain.reservation.entity.Reservation;
import kr.hhplus.be.server.domain.reservation.repository.ReservationRepository;
import kr.hhplus.be.server.domain.reservation.vo.ReservationStatusEnums;
import kr.hhplus.be.server.domain.seat.dto.SeatDto;
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

@DisplayName("콘서트 예약 통합 테스트")
@Transactional
@Rollback
public class ReservationIntegrationTest extends TestContainer {

    @Autowired
    private QueueService queueService;
    @Autowired
    private EventService eventService;
    @Autowired
    private SeatService seatService;
    @Autowired
    private ReservationService reservationService;
    @Autowired
    private PaymentService paymentService;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private EventScheduleRepository eventScheduleRepository;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private QueueTokenRepository queueTokenRepository;
    @Autowired
    private ReservationRepository reservationRepository;

    private User testUser;
    private Category testCategory;
    private Event testEvent;
    private EventSchedule testSchedule;
    private Seat testSeat;
    private QueueToken testToken;


    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUserName("테스트유저");
        testUser.setEmail("test@example.com");
        testUser.setBalance(100000);
        userRepository.save(testUser);

        testCategory = new Category();
        testCategory.setCode("CONCERT");
        testCategory.setName("콘서트");
        categoryRepository.save(testCategory);

        testEvent = Event.builder()
                .categoryId(testCategory.getId())
                .code("ME001")
                .name("정민우1 콘서트")
                .enable(true)
                .build();
        eventRepository.save(testEvent);

        testSchedule = EventSchedule.builder()
                .eventId(testEvent.getId())
                .startDateTime(LocalDateTime.now().plusDays(7))
                .endDateTime(LocalDateTime.now().plusDays(7).plusHours(3))
                .build();
        eventScheduleRepository.save(testSchedule);

        testSeat = Seat.builder()
                .scheduleId(testSchedule.getId())
                .seatNumber(1)
                .seatType(SeatTypeAndValueEnums.VIP.getType())
                .status(SeatStatusEnums.AVAILABLE.getStatus())
                .build();
        seatRepository.save(testSeat);

        testToken = QueueToken.builder()
                .userId(testUser.getId())
                .token("test-token-123")
                .position(1)
                .issuedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .build();
        queueTokenRepository.save(testToken);
    }


    @Test
    @DisplayName("전체 예약 플로우 - 토큰발급부터 결제완료까지")
    void issueTokenToPayment() {

        // 대기열 토큰 발급
        QueueDto.GetQueueToken.Request tokenRequest = new QueueDto.GetQueueToken.Request();
        tokenRequest.setUserId(testUser.getId());

        QueueDto.GetQueueToken.Response tokenResponse = queueService.issueToken(tokenRequest);
        String token = tokenResponse.getView().getToken();
        assertThat(token).isNotNull();

        setAuthToken(token);

        // 이벤트 목록 조회
        EventDto.GetEventList.Request eventRequest = new EventDto.GetEventList.Request();
        EventDto.GetEventList.Response eventResponse = eventService.getEventList(eventRequest);
        assertThat(eventResponse.getList()).isNotEmpty();

        // 좌석 조회
        SeatDto.getSeats.Request seatRequest = new SeatDto.getSeats.Request();
        seatRequest.setScheduleId(testSchedule.getId());

        SeatDto.getSeats.Response seatResponse = seatService.getSeats(seatRequest);
        assertThat(seatResponse.getList()).hasSize(1);
        assertThat(seatResponse.getList().get(0).getStatus()).isEqualTo(SeatStatusEnums.AVAILABLE.getStatus());

        // 좌석 예약
        ReservationDto.SetReservation.Request reservationRequest = new ReservationDto.SetReservation.Request();
        reservationRequest.setScheduleId(testSchedule.getId());
        reservationRequest.setSeatId(testSeat.getId());

        ReservationDto.SetReservation.Response reservationResponse =
                reservationService.createReservation(reservationRequest);
        Long reservationId = reservationResponse.getReservationId();
        assertThat(reservationId).isNotNull();

        // 결제 처리
        PaymentDto.SetPayment.Request paymentRequest = new PaymentDto.SetPayment.Request();
        paymentRequest.setReservationId(reservationId);

        paymentService.processPayment(paymentRequest);

        // 최종 상태 검증
        // 사용자 잔액 확인
        User updatedUser = userRepository.findById(testUser.getId()).get();
        assertThat(updatedUser.getBalance()).isEqualTo(50000);

        // 예약 상태 확인
        Reservation reservation = reservationRepository.findById(reservationId).get();
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatusEnums.CONFIRMED.getStatus());

        // 좌석 상태 확인
        Seat seat = seatRepository.findById(testSeat.getId()).get();
        assertThat(seat.getStatus()).isEqualTo(SeatStatusEnums.RESERVED.getStatus());
    }

    @Test
    @DisplayName("대기열 토큰 중복 발급 방지")
    void preventIssueDuplicateQueueToken() {

        QueueDto.GetQueueToken.Request request = new QueueDto.GetQueueToken.Request();
        request.setUserId(testUser.getId());

        // 첫 번째 토큰 발급
        QueueDto.GetQueueToken.Response first = queueService.issueToken(request);

        // 두 번째 토큰 발급
        QueueDto.GetQueueToken.Response second = queueService.issueToken(request);

        // 같은 토큰이어야 함
        assertThat(first.getView().getToken()).isEqualTo(second.getView().getToken());
        assertThat(first.getView().getPosition()).isEqualTo(second.getView().getPosition());
    }

    private void setAuthToken(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Auth-Queue-Token", token);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }


}
