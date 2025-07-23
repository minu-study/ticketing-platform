package kr.hhplus.be.server.domain.event.repository;

import kr.hhplus.be.server.api.event.dto.EventDto;
import kr.hhplus.be.server.domain.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface EventQueryRepository {

    List<EventDto.EventSummaryView> getEventList(Long categoryId);

    List<EventDto.GetEventScheduleInfoView> getEventScheduleInfo(Long eventId, LocalDateTime now);

}