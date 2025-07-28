package kr.hhplus.be.server.scheduler;

import kr.hhplus.be.server.api.queue.service.QueueService;
import kr.hhplus.be.server.api.reservation.service.ReservationService;
import kr.hhplus.be.server.api.seat.service.SeatService;
import kr.hhplus.be.server.domain.reservation.entity.Reservation;
import kr.hhplus.be.server.domain.reservation.repository.ReservationRepository;
import kr.hhplus.be.server.domain.reservation.vo.ReservationStatusEnums;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenAndReservationScheduler {

    private final ReservationRepository reservationRepository;

    private final ReservationService reservationService;
    private final QueueService queueService;
    private final SeatService seatService;


    /**
     * 만료된 좌석 HOLD 상태 정리
     * 매 30초마다 실행 (예약 정리와 함께)
     */
    @Scheduled(fixedRate = 30000)
    @Transactional
    public void syncExpiredSeatHolds() {
        log.info("syncExpiredSeatHolds, start");
        seatService.syncSeats(LocalDateTime.now());
        log.info("syncExpiredSeatHolds, end");
    }

    /**
     * 만료된 임시 예약 정리
     * 매 30초마다 실행
     */
    @Scheduled(fixedRate = 30000)
    public void syncExpiredReservations() {

        log.info("syncExpiredReservations, start");

        LocalDateTime now = LocalDateTime.now();
        List<Reservation> expiredReservations = reservationRepository
                .findByStatusAndExpiresAtBefore(ReservationStatusEnums.TEMP.getStatus(), now);
        if (!expiredReservations.isEmpty()) {
            for (Reservation reservation : expiredReservations) {
                reservationService.processExpiredReservation(reservation, now);
            }
        }

        log.info("syncExpiredReservations, end");

    }


    /**
     * 대기열 위치 업데이트
     * 매 1분마다 실행
     */
    @Scheduled(fixedRate = 60000)
    public void syncQueuePositions() {

        log.info("syncQueuePositions, start");

        LocalDateTime now = LocalDateTime.now();
        queueService.cleanupExpiredTokens(now);
        queueService.reorderTokens(now);

        log.info("syncQueuePositions, end");

    }

}