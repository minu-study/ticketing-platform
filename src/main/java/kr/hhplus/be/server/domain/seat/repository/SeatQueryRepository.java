package kr.hhplus.be.server.domain.seat.repository;

import kr.hhplus.be.server.domain.seat.dto.SeatDto;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SeatQueryRepository  {

    List<SeatDto.SeatView> getSeats(Long scheduleId, LocalDateTime now);

}