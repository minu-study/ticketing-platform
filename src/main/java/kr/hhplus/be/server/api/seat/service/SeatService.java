package kr.hhplus.be.server.api.seat.service;

import kr.hhplus.be.server.api.queue.service.QueueService;
import kr.hhplus.be.server.common.util.CommonUtil;
import kr.hhplus.be.server.domain.seat.dto.SeatDto;
import kr.hhplus.be.server.domain.seat.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class SeatService {

    private final QueueService queueService;
    private final SeatRepository seatRepository;

    /**
     * 특정 일정의 예약 가능한 좌석 조회
     */
    @Transactional(readOnly = true)
    public SeatDto.getSeats.Response getSeats(SeatDto.getSeats.Request param) {

        String token = CommonUtil.getQueueToken();
        queueService.validateToken(token);

        List<SeatDto.SeatView> list = seatRepository.getSeats(param.getScheduleId(), LocalDateTime.now());

        return SeatDto.getSeats.Response.builder()
                .list(list)
                .build();
    }


}
