package kr.hhplus.be.server.api.event.service;

import kr.hhplus.be.server.api.event.dto.EventDto;
import kr.hhplus.be.server.api.queue.service.QueueService;
import kr.hhplus.be.server.common.util.CommonUtil;
import kr.hhplus.be.server.domain.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final EventRepository eventRepository;
    private final QueueService queueService;

    @Transactional(readOnly = true)
    public EventDto.GetEventList.Response getEventList(EventDto.GetEventList.Request param) {

        String token = CommonUtil.getQueueToken();
        queueService.validateToken(token);

        List<EventDto.EventSummaryView> list = eventRepository.getEventList(param.getCategoryId());
        
        return EventDto.GetEventList.Response.builder()
                .list(list)
                .build();
    }

    @Transactional(readOnly = true)
    public EventDto.GetEventScheduleList.Response getAvailableEventSchedules(EventDto.GetEventScheduleList.Request param) {

        String token = CommonUtil.getQueueToken();
        queueService.validateToken(token);

        List<EventDto.GetEventScheduleInfoView> list = eventRepository.getEventScheduleInfo(param.getEventId(), LocalDateTime.now());
        
        return EventDto.GetEventScheduleList.Response.builder()
                .list(list)
                .build();
    }

}