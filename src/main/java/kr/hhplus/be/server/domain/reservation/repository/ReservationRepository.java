package kr.hhplus.be.server.domain.reservation.repository;

import kr.hhplus.be.server.domain.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long>, ReservationQueryRepository {
    List<Reservation> findByStatusAndExpiresAtBefore(String status, LocalDateTime expiresAt);

}