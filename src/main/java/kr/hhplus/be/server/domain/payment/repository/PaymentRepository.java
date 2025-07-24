package kr.hhplus.be.server.domain.payment.repository;

import kr.hhplus.be.server.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long>, PaymentQueryRepository {

    Optional<Payment> findByReservationIdAndStatus(Long reservationId, String status);

}