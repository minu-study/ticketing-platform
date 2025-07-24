package kr.hhplus.be.server.domain.seat.repository;

import com.querydsl.core.types.Expression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.hhplus.be.server.domain.seat.dto.SeatDto;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import static com.querydsl.core.types.Projections.fields;
import static kr.hhplus.be.server.domain.event.entity.QEvent.event;
import static kr.hhplus.be.server.domain.eventSchedule.entity.QEventSchedule.eventSchedule;
import static kr.hhplus.be.server.domain.seat.entity.QSeat.seat;

@RequiredArgsConstructor
public class SeatQueryRepositoryImpl implements SeatQueryRepository {

    private final JPAQueryFactory queryFactory;


    @Override
    public List<SeatDto.SeatView> getSeats(Long scheduleId, LocalDateTime now) {

        Expression<SeatDto.SeatView> selectExpr = fields(SeatDto.SeatView.class,
                eventSchedule.id.as("eventScheduleId"),
                seat.seatNumber,
                seat.seatType,
                seat.status
        );

        return queryFactory.select(selectExpr)
                .from(seat)
                .innerJoin(eventSchedule).on(seat.eventSchedule.eq(eventSchedule))
                .innerJoin(event).on(eventSchedule.event.eq(event))
                .where(
                        event.enable.isTrue()
                                .and(eventSchedule.id.eq(scheduleId))
                                .and(eventSchedule.startDateTime.loe(now))
                                .and(eventSchedule.endDateTime.goe(now))
                )
                .fetch();

    }

}
