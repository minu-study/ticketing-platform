package kr.hhplus.be.server.domain.event.repository;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.hhplus.be.server.api.event.dto.EventDto;
import kr.hhplus.be.server.domain.seat.entity.QSeat;
import kr.hhplus.be.server.domain.seat.vo.SeatStatusEnums;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import static com.querydsl.core.types.Projections.fields;
import static kr.hhplus.be.server.domain.category.entity.QCategory.category;
import static kr.hhplus.be.server.domain.event.entity.QEvent.event;
import static kr.hhplus.be.server.domain.eventSchedule.entity.QEventSchedule.eventSchedule;


@RequiredArgsConstructor
public class EventQueryRepositoryImpl implements EventQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<EventDto.EventSummaryView> getEventList(Long categoryId) {

        Expression<EventDto.EventSummaryView> selectExpr = fields(EventDto.EventSummaryView.class,
                category.code.as("categoryCode"),
                category.name.as("categoryName"),
                event.id.as("eventId"),
                event.code.as("eventCode"),
                event.name.as("eventName")
        );

        return queryFactory.select(selectExpr)
                .from(event)
                .innerJoin(category).on(event.categoryId.eq(category.id))
                .where(
                        event.enable.isTrue(),
                        eqCategoryId(categoryId)
                )
                .fetch();


    }

    @Override
    public List<EventDto.GetEventScheduleInfoView> getEventScheduleInfo(Long eventId, LocalDateTime now) {

        QSeat seatSub = new QSeat("seatSub");

        NumberExpression<Integer> totalSeats = Expressions
                .asNumber(
                        JPAExpressions
                                .select(seatSub.count())
                                .from(seatSub)
                                .where(seatSub.eventSchedule.id.eq(eventSchedule.id))
                )
                .castToNum(Integer.class);

        NumberExpression<Integer> availableSeats = Expressions
                .asNumber(
                        JPAExpressions
                                .select(seatSub.count())
                                .from(seatSub)
                                .where(seatSub.eventSchedule.id.eq(eventSchedule.id)
                                        .and(seatSub.status.eq(SeatStatusEnums.AVAILABLE.getStatus())))
                )
                .castToNum(Integer.class);

        NumberExpression<Integer> reservedSeats = Expressions
                .asNumber(
                        JPAExpressions
                                .select(seatSub.count())
                                .from(seatSub)
                                .where(seatSub.eventSchedule.id.eq(eventSchedule.id)
                                        .and(seatSub.status.eq(SeatStatusEnums.RESERVED.getStatus())))
                )
                .castToNum(Integer.class);

        NumberExpression<Integer> holdSeats = Expressions
                .asNumber(
                        JPAExpressions
                                .select(seatSub.count())
                                .from(seatSub)
                                .where(seatSub.eventSchedule.id.eq(eventSchedule.id)
                                        .and(seatSub.status.eq(SeatStatusEnums.HOLD.getStatus()))
                                        .and(seatSub.holdExpiresAt.after(now)))
                )
                .castToNum(Integer.class);


        Expression<EventDto.GetEventScheduleInfoView> selectExpr = fields(EventDto.GetEventScheduleInfoView.class,
                event.id.as("eventId"),
                eventSchedule.id.as("eventScheduleId"),
                category.code.as("categoryCode"),
                category.name.as("categoryName"),
                event.code.as("eventCode"),
                event.name.as("eventName"),
                eventSchedule.startDateTime,
                eventSchedule.endDateTime,
                totalSeats.as("totalSeats"),
                availableSeats.as("availableSeats"),
                reservedSeats.as("reservedSeats"),
                holdSeats.as("holdSeats")
        );

        return queryFactory.select(selectExpr)
                .from(eventSchedule)
                .innerJoin(event).on(eventSchedule.event.id.eq(event.id))
                .innerJoin(category).on(event.categoryId.eq(category.id))
                .where(
                        event.enable.isTrue(),
                        event.id.eq(eventId),
                        eventSchedule.startDateTime.loe(now),
                        eventSchedule.endDateTime.goe(now)
                )
                .fetch();
    }


    private BooleanExpression eqCategoryId(Long categoryId) {
        return categoryId != null ? event.categoryId.eq(categoryId) : null;
    }


}