package kr.hhplus.be.server.api.reservation.controller;

import kr.hhplus.be.server.common.model.ApiResponse;
import kr.hhplus.be.server.common.util.ResponseUtil;
import kr.hhplus.be.server.domain.reservation.dto.ReservationDto;
import kr.hhplus.be.server.api.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservation")
@RequiredArgsConstructor
@Slf4j
public class ReservationController {

    private final ReservationService reservationService;

    /**
     * 좌석 예약 요청 API
     */
    @PostMapping
    public ResponseEntity<ApiResponse> createReservation(@RequestBody ReservationDto.SetReservation.Request param) {
        ReservationDto.SetReservation.Response response = reservationService.createReservation(param);
        return ResponseUtil.convertResponse(response);
    }

    /**
     * 예약 목록 조회 API
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse> getReservationList() {
        ReservationDto.GetReservationList.Response response = reservationService.getReservationList();
        return ResponseUtil.convertResponse(response);
    }

    /**
     * 예약 상세 조회 API
     */
    @GetMapping
    public ResponseEntity<ApiResponse> getReservation(@RequestBody ReservationDto.GetReservation.Request param) {
        ReservationDto.GetReservation.Response response = reservationService.getReservation(param);
        return ResponseUtil.convertResponse(response);
    }

    /**
     * 예약 취소 API
     */
    @DeleteMapping("/{reservationId}")
    public ResponseEntity<ApiResponse> cancelReservation(@RequestBody ReservationDto.CancelReservation.Request param) {
        reservationService.cancelReservation(param);
        return ResponseUtil.convertResponse();
    }
}