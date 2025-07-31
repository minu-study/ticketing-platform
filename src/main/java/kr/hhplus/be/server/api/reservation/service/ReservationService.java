package kr.hhplus.be.server.api.reservation.service;

import kr.hhplus.be.server.api.payment.service.PaymentService;
import kr.hhplus.be.server.api.queue.service.QueueService;
import kr.hhplus.be.server.common.exception.AppException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.util.CommonUtil;
import kr.hhplus.be.server.domain.queueToken.dto.QueueDto;
import kr.hhplus.be.server.domain.reservation.dto.ReservationDto;
import kr.hhplus.be.server.domain.eventSchedule.entity.EventSchedule;
import kr.hhplus.be.server.domain.eventSchedule.repository.EventScheduleRepository;
import kr.hhplus.be.server.domain.reservation.entity.Reservation;
import kr.hhplus.be.server.domain.reservation.repository.ReservationRepository;
import kr.hhplus.be.server.domain.reservation.vo.ReservationStatusEnums;
import kr.hhplus.be.server.domain.seat.entity.Seat;
import kr.hhplus.be.server.domain.seat.repository.SeatRepository;
import kr.hhplus.be.server.domain.seat.vo.SeatStatusEnums;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;
    private final EventScheduleRepository eventScheduleRepository;

    private final QueueService queueService;
    private final PaymentService paymentService;

    // 임시 배정 시간 (분)
    private static final int TEMPORARY_HOLD_MINUTES = 5;

    @Transactional
    public ReservationDto.SetReservation.Response createReservation(ReservationDto.SetReservation.Request param) {

        String token = CommonUtil.getQueueToken();
        QueueDto.QueueTokenValidationView tokenInfo = queueService.validateToken(token);

        Seat seat = seatRepository.findById(param.getSeatId())
                .orElseThrow(() -> {
                    log.error("Seat not found for seatId: {}", param.getSeatId());
                    return new AppException(ErrorCode.DATA_NOT_FOUND);
                });

        if (!isAvailableForReservationSeat(seat)) {
            log.error("Seat is not available for reservation: {}", param.getSeatId());
            throw new AppException(ErrorCode.SEAT_NOT_AVAILABLE);
        }

        // 임시 예약 생성
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(TEMPORARY_HOLD_MINUTES);
        Reservation reservation = Reservation.builder()
                .userId(tokenInfo.getUserId())
                .seatId(seat.getId())
                .scheduleId(param.getScheduleId())
                .status(ReservationStatusEnums.TEMP.getStatus())
                .reservedAt(now)
                .expiresAt(expiresAt)
                .build();

        reservation = reservationRepository.save(reservation);

        seat.setStatus(SeatStatusEnums.HOLD.getStatus()) ;
        seat.setHoldExpiresAt(expiresAt);
        seatRepository.save(seat);

        return ReservationDto.SetReservation.Response.builder()
                .reservationId(reservation.getId())
                .build();
    }

    @Transactional(readOnly = true)
    public ReservationDto.GetReservationList.Response getReservationList() {

        String token = CommonUtil.getQueueToken();
        QueueDto.QueueTokenValidationView tokenInfo = queueService.validateToken(token);

        List<ReservationDto.ReservationSummaryView> list = reservationRepository.getReservationList(tokenInfo.getUserId());
        
        return ReservationDto.GetReservationList.Response.builder()
                .list(list)
                .build();
    }

    @Transactional(readOnly = true)
    public ReservationDto.GetReservation.Response getReservation(ReservationDto.GetReservation.Request param) {

        String token = CommonUtil.getQueueToken();
        queueService.validateToken(token);

        ReservationDto.ReservationDetailView view = reservationRepository.getReservationDetail(param.getReservationId());

        return ReservationDto.GetReservation.Response.builder()
                .view(view)
                .build();
    }

    @Transactional
    public void cancelReservation(ReservationDto.CancelReservation.Request param) {

        String token = CommonUtil.getQueueToken();
        queueService.validateToken(token);

        Reservation reservation = reservationRepository.findById(param.getReservationId())
                .orElseThrow(() ->  {
                    log.error("reservation not found for reservationId: {}", param.getReservationId());
                    throw new AppException(ErrorCode.DATA_NOT_FOUND);
                });

        // 취소 가능한 상태인지 확인
        if (Boolean.TRUE.equals(isCancelAvailableForReservation(reservation))) {
            log.error("cancelReservation, reservation is not cancel available for reservationId: {}", param.getReservationId());
            throw new AppException(ErrorCode.RESERVATION_CANNOT_BE_CANCELED);
        }

        reservation.setStatus(ReservationStatusEnums.CANCELED.getStatus());
        reservation.setCanceledAt(LocalDateTime.now());
        reservationRepository.save(reservation);

        seatRepository.findById(reservation.getSeatId()).ifPresent(
                seat -> {
                    seat.setStatus(SeatStatusEnums.AVAILABLE.getStatus());
                    seat.setHoldExpiresAt(null);
                    seatRepository.save(seat);
                }
        );

        paymentService.paymentRefund(reservation);
    }

    /**
     * 예약 확정 (결제 완료 시 호출)
     */
    @Transactional
    public void confirmReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() ->  {
                    log.error("Reservation not found for reservationId: {}", reservationId);
                    return new AppException(ErrorCode.DATA_NOT_FOUND);
                });

        reservation.setStatus(ReservationStatusEnums.CONFIRMED.getStatus());
        reservation.setExpiresAt(null);
        reservationRepository.save(reservation);

        Seat seat = seatRepository.findById(reservation.getSeatId())
                .orElseThrow(() ->  {
                    log.error("Seat not found for seatId: {}", reservation.getSeatId());
                    return new AppException(ErrorCode.DATA_NOT_FOUND);
                });
        seat.setStatus(SeatStatusEnums.RESERVED.getStatus());
        seat.setHoldExpiresAt(null);
        seatRepository.save(seat);
    }


    @Transactional
    public void processExpiredReservation(Reservation reservation, LocalDateTime now) {
        try {
            reservation.setStatus(ReservationStatusEnums.EXPIRED.getStatus());
            reservation.setExpiresAt(now);
            reservationRepository.save(reservation);

            Seat seat = seatRepository.findById(reservation.getSeatId()).orElse(null);
            if (seat != null && SeatStatusEnums.HOLD.getStatus().equals(seat.getStatus())) {
                seat.setStatus(SeatStatusEnums.AVAILABLE.getStatus());
                seat.setHoldExpiresAt(null);
                seatRepository.save(seat);
            }

        } catch (Exception e) {
            log.error("processExpiredReservation_Failed to process expired reservation: {}, error: {}", reservation.getId(), e.getMessage());
        }
    }


    /*
     * 예약 취소는 예약 상태가 TEMP 이거나
     * 이벤트 시작 하루 전까지만 취소 가능함 (CONFIRMED)
     */
    private Boolean isCancelAvailableForReservation(Reservation reservation) {

        if (ReservationStatusEnums.TEMP.getStatus().equals(reservation.getStatus())) {
            return true;
        }

        if (ReservationStatusEnums.CONFIRMED.getStatus().equals(reservation.getStatus())) {

            EventSchedule eventSchedule = eventScheduleRepository.findById(reservation.getScheduleId())
                    .orElseThrow(() -> {
                        log.error("isCancelAvailableForReservation_EventSchedule not found for scheduleId: {}", reservation.getScheduleId());
                        return new AppException(ErrorCode.DATA_NOT_FOUND);
                    });

            LocalDateTime cancelDeadline = eventSchedule.getStartDateTime().minusDays(1);
            LocalDateTime now = LocalDateTime.now();

            return now.isBefore(cancelDeadline);
        }

        return false;


    }

    private Boolean isAvailableForReservationSeat(Seat seat) {

        if (SeatStatusEnums.AVAILABLE.getStatus().equals(seat.getStatus())) {
            return true;
        }
        
        if (SeatStatusEnums.HOLD.getStatus().equals(seat.getStatus()) && seat.getHoldExpiresAt() != null) {
            return seat.getHoldExpiresAt().isBefore(LocalDateTime.now());
        }
        
        return false;
    }

}