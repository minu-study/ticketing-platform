package kr.hhplus.be.server.api.seat.controller;

import jakarta.validation.Valid;
import kr.hhplus.be.server.api.seat.service.SeatService;
import kr.hhplus.be.server.common.model.ApiResponse;
import kr.hhplus.be.server.common.util.CommonUtil;
import kr.hhplus.be.server.domain.seat.dto.SeatDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/api/seat")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    /**
     * 예약 가능 좌석 조회 API
     * 이벤트 스케줄 식별자를 통해 해당 식별자에 묶인 좌석들을 조회
     * 좌석별 상태(예약 가능, 예약 불가)를 함께 제공
     */
    @GetMapping
    public ResponseEntity<ApiResponse> getSeats(@Valid SeatDto.getSeats.Request param) {
        log.info("getAvailableSeats param : {}", param);
        SeatDto.getSeats.Response response = seatService.getSeats(param);
        log.info("getAvailableSeats response : {}", response);
        return CommonUtil.convertResponse(response);
    }

}
