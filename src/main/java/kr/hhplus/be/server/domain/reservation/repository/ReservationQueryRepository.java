package kr.hhplus.be.server.domain.reservation.repository;

import kr.hhplus.be.server.domain.reservation.dto.ReservationDto;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReservationQueryRepository {

    List<ReservationDto.ReservationSummaryView> getReservationList(UUID userId);

    ReservationDto.ReservationDetailView getReservationDetail(Long reservationId);

}
