package kr.hhplus.be.server.api.event.controller;

import kr.hhplus.be.server.common.util.ResponseUtil;
import kr.hhplus.be.server.domain.event.dto.EventDto;
import kr.hhplus.be.server.api.event.service.EventService;
import kr.hhplus.be.server.common.model.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/event")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    /**
     * 이벤트 목록 조회 API
     */
    @GetMapping
    public ResponseEntity<ApiResponse> getEvents(EventDto.GetEventList.Request param) {
        EventDto.GetEventList.Response response = eventService.getEventList(param);
        return ResponseUtil.convertResponse(response);
    }

    /**
     * 예약 가능 날짜 조회 API
     * 예약 가능한 콘서트 날짜와 콘서트 정보 리스트를 조회
     */
    @GetMapping("/schedules")
    public ResponseEntity<ApiResponse> getAvailableSchedules(EventDto.GetEventScheduleList.Request param) {
        EventDto.GetEventScheduleList.Response response = eventService.getAvailableEventSchedules(param);
        return ResponseUtil.convertResponse(response);
    }
}