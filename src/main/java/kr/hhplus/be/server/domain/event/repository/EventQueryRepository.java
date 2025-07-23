package kr.hhplus.be.server.domain.event.repository;

import kr.hhplus.be.server.domain.event.dto.EventDto;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface EventQueryRepository {

    List<EventDto.EventSummaryView> getEventList(Long categoryId);

    List<EventDto.GetEventScheduleInfoView> getEventScheduleInfo(Long eventId, LocalDateTime now);

}