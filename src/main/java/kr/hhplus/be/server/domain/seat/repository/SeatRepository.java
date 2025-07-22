package kr.hhplus.be.server.domain.seat.repository;

import kr.hhplus.be.server.domain.seat.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long>, SeatQueryRepository {

    int countByScheduleId(Long scheduleId);
    int countByScheduleIdAndStatus(Long scheduleId, String status);
    int countByScheduleIdAndStatusAndHoldExpiresAtAfter(Long scheduleId, String status, LocalDateTime holdExpiresAt);
}