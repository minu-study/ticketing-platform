package kr.hhplus.be.server.api.seat.service;

import kr.hhplus.be.server.api.queue.service.QueueService;
import kr.hhplus.be.server.common.util.CommonUtil;
import kr.hhplus.be.server.domain.seat.dto.SeatDto;
import kr.hhplus.be.server.domain.seat.entity.Seat;
import kr.hhplus.be.server.domain.seat.repository.SeatRepository;
import kr.hhplus.be.server.domain.seat.vo.SeatStatusEnums;
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
     * 특정 이벤트 일정의 좌석 전체 조회
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


    @Transactional
    public void syncSeats(LocalDateTime now) {

        List<Seat> expiredHoldSeats = seatRepository
                .findByStatusAndHoldExpiresAtBefore(SeatStatusEnums.HOLD.getStatus(), now);

        if (!expiredHoldSeats.isEmpty()) {

            for (Seat seat : expiredHoldSeats) {
                try {
                        seat.setStatus(SeatStatusEnums.AVAILABLE.getStatus());
                        seat.setHoldExpiresAt(null);
                        seatRepository.save(seat);
                    } catch(Exception e) {
                        log.error("syncSeats, update error, seat id : {}", seat.getId());
                    }
            }
        }
    }

}
