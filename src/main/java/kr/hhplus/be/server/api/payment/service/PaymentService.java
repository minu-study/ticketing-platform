package kr.hhplus.be.server.api.payment.service;

import kr.hhplus.be.server.api.balanceLog.service.BalanceLogService;
import kr.hhplus.be.server.api.queue.service.QueueService;
import kr.hhplus.be.server.api.reservation.service.ReservationService;
import kr.hhplus.be.server.common.exception.AppException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.util.TokenExtractor;
import kr.hhplus.be.server.domain.balanceLog.vo.BalanceActionEnums;
import kr.hhplus.be.server.domain.payment.dto.PaymentDto;
import kr.hhplus.be.server.domain.payment.entity.Payment;
import kr.hhplus.be.server.domain.payment.repository.PaymentRepository;
import kr.hhplus.be.server.domain.payment.vo.PaymentStatusEnums;
import kr.hhplus.be.server.domain.queueToken.dto.QueueDto;
import kr.hhplus.be.server.domain.reservation.entity.Reservation;
import kr.hhplus.be.server.domain.reservation.repository.ReservationRepository;
import kr.hhplus.be.server.domain.reservation.vo.ReservationStatusEnums;
import kr.hhplus.be.server.domain.seat.entity.Seat;
import kr.hhplus.be.server.domain.seat.repository.SeatRepository;
import kr.hhplus.be.server.domain.seat.vo.SeatTypeAndValueEnums;
import kr.hhplus.be.server.domain.user.entity.User;
import kr.hhplus.be.server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final SeatRepository seatRepository;
    private final QueueService queueService;
    private final ReservationService reservationService;
    private final BalanceLogService balanceLogService;

    /**
     * 결제 처리
     */
    @Transactional
    public void processPayment(PaymentDto.SetPayment.Request param) {

        String token = TokenExtractor.getQueueToken();
        QueueDto.QueueTokenValidationView tokenInfo = queueService.validateToken(token);
        UUID userId = tokenInfo.getUserId();

        queueService.extendToken();

        Reservation reservation = reservationRepository.findById(param.getReservationId())
                .orElseThrow(() ->  {
                    log.error("Reservation not found for reservationId: {}", param.getReservationId());
                    return new AppException(ErrorCode.DATA_NOT_FOUND);
                });

        // Temp 상태 예약이 아니거나 예약이 만료된 경우 결제 안됨
        if (!ReservationStatusEnums.TEMP.getStatus().equals(reservation.getStatus()) || reservation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.INVALID_RESERVATION_STATUS_FOR_PAYMENT);
        }

        Seat seat = seatRepository.findById(reservation.getSeatId())
                .orElseThrow(() ->  {
                    log.error("Seat not found for seatId: {}", reservation.getSeatId());
                    return new AppException(ErrorCode.DATA_NOT_FOUND);
                });

        int paymentAmount = SeatTypeAndValueEnums.valueOf(seat.getSeatType()).getValue();

        User user = userRepository.findById(userId)
                .orElseThrow(() ->  {
                    log.error("User not found for userId: {}", userId);
                    return new AppException(ErrorCode.DATA_NOT_FOUND);
                });

        if (user.getBalance() < paymentAmount) {
            throw new AppException(ErrorCode.INSUFFICIENT_PAYMENT_AMOUNT);
        }

        Payment payment = Payment.create(userId, param.getReservationId(), paymentAmount);
        payment = paymentRepository.saveAndFlush(payment);

        try {

            // 잔액 차감
            user.useBalance(paymentAmount);
            userRepository.save(user);

            // 결제 완료 처리
            payment.complete();
            paymentRepository.save(payment);

            // 예약 및 좌석 완료 처리
            reservationService.confirmReservation(param.getReservationId());

            // 토큰 만료 처리
            queueService.expireToken(token);

            // 결제 로깅
            balanceLogService.saveLogAsync(user.getId(), paymentAmount, BalanceActionEnums.USE.getAction());

        } catch (Exception e) {
            log.error("Payment processing failed for reservation: {}", param.getReservationId(), e);
            updateFailPayment(payment);
            throw new AppException(ErrorCode.PAYMENT_PROCESSING_ERROR);
        }

    }

    /**
     * 결제 내역 조회
     */
    @Transactional(readOnly = true)
    public PaymentDto.GetPayment.Response getPayment(PaymentDto.GetPayment.Request param) {

        String token = TokenExtractor.getQueueToken();
        queueService.validateToken(token);

        List<PaymentDto.PaymentView> list = paymentRepository.getPaymentList(param.getUserId());

        return PaymentDto.GetPayment.Response.builder()
                .list(list)
                .build();
    }

    /**
     * 잔액 원복 (예약 취소때 사용)
     */
    @Transactional
    public void paymentRefund(Reservation reservation) {

        Optional<Payment> paymentOptional = paymentRepository.findByReservationIdAndStatus(
                reservation.getId(), PaymentStatusEnums.COMPLETE.getStatus());

        if (!paymentOptional.isPresent()) {

            Payment payment = paymentOptional.get();

            payment.cancel();
            paymentRepository.save(payment);

            User user = userRepository.findById(reservation.getUserId())
                    .orElseThrow(() -> {
                        log.error("User not found for userId: {}", reservation.getUserId());
                        return new AppException(ErrorCode.DATA_NOT_FOUND);
                    });

            user.chargeBalance(payment.getAmount());
            userRepository.save(user);

            balanceLogService.saveLogAsync(user.getId(), payment.getAmount(), BalanceActionEnums.REFUND.getAction());

        }

    }



    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateFailPayment(Payment payment) {
        payment.fail();
        paymentRepository.save(payment);
    }

}