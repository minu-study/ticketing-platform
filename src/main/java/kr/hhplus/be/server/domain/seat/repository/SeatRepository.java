package kr.hhplus.be.server.domain.seat.repository;

import kr.hhplus.be.server.domain.seat.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long>, SeatQueryRepository {

    List<Seat> findByStatusAndHoldExpiresAtBefore(String status, LocalDateTime holdExpiresAt);
}