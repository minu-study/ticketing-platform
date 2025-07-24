package kr.hhplus.be.server.domain.reservation.repository;

import com.querydsl.core.types.Expression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.hhplus.be.server.domain.reservation.dto.ReservationDto;
import kr.hhplus.be.server.domain.user.dto.UserDto;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

import static com.querydsl.core.types.Projections.fields;
import static kr.hhplus.be.server.domain.event.entity.QEvent.event;
import static kr.hhplus.be.server.domain.eventSchedule.entity.QEventSchedule.eventSchedule;
import static kr.hhplus.be.server.domain.payment.entity.QPayment.payment;
import static kr.hhplus.be.server.domain.reservation.entity.QReservation.reservation;
import static kr.hhplus.be.server.domain.seat.entity.QSeat.seat;

@RequiredArgsConstructor
public class ReservationQueryRepositoryImpl implements ReservationQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<ReservationDto.ReservationSummaryView> getReservationList(UUID userId) {

        Expression<ReservationDto.ReservationSummaryView> selectExpr = fields(ReservationDto.ReservationSummaryView.class,

                reservation.id,
                event.code.as("eventCode"),
                event.name.as("eventName"),
                eventSchedule.startDateTime.as("eventStartDateTime"),
                eventSchedule.endDateTime.as("eventEndDateTime"),
                seat.seatNumber,
                reservation.status,
                reservation.reservedAt,
                reservation.expiresAt,
                reservation.canceledAt
        );

        return jpaQueryFactory.select(selectExpr)
                .from(reservation)
                .innerJoin(eventSchedule).on(eventSchedule.eq(reservation.eventSchedule))
                .innerJoin(event).on(event.eq(eventSchedule.event))
                .innerJoin(seat).on(seat.eventSchedule.eq(eventSchedule))
                .where(reservation.userId.eq(userId))
                .fetch();
    }

    @Override
    public ReservationDto.ReservationDetailView getReservationDetail(Long reservationId) {

        Expression<ReservationDto.ReservationDetailView> selectExpr = fields(ReservationDto.ReservationDetailView.class,

                reservation.id,
                reservation.userId,
                seat.id.as("seatId"),
                event.id.as("eventId"),
                eventSchedule.id.as("eventScheduleId"),
                event.code.as("eventCode"),
                event.name.as("eventName"),
                eventSchedule.startDateTime.as("eventStartDateTime"),
                eventSchedule.endDateTime.as("eventEndDateTime"),
                seat.seatType.as("seatType"),
                seat.seatNumber,
                reservation.status,
                reservation.reservedAt,
                reservation.expiresAt,
                reservation.canceledAt,
                payment.amount.as("paymentAmount")
        );

        return jpaQueryFactory.select(selectExpr)
                .from(reservation)
                .innerJoin(eventSchedule).on(eventSchedule.eq(reservation.eventSchedule))
                .innerJoin(event).on(event.eq(eventSchedule.event))
                .innerJoin(seat).on(seat.eventSchedule.eq(eventSchedule))
                .leftJoin(payment).on(payment.reservationId.eq(reservation.id))
                .where(reservation.id.eq(reservationId))
                .fetchOne();

    }

}
