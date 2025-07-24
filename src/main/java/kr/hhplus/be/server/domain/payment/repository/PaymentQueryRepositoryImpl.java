package kr.hhplus.be.server.domain.payment.repository;

import com.querydsl.core.types.Expression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.hhplus.be.server.domain.payment.dto.PaymentDto;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

import static com.querydsl.core.types.Projections.fields;
import static kr.hhplus.be.server.domain.payment.entity.QPayment.payment;

@RequiredArgsConstructor
public class PaymentQueryRepositoryImpl implements PaymentQueryRepository {

    private final JPAQueryFactory queryFactory;


    public List<PaymentDto.PaymentView> getPaymentList(UUID userId) {

        Expression<PaymentDto.PaymentView> selectExpr = fields(PaymentDto.PaymentView.class,
                payment.id,
                payment.userId,
                payment.reservationId,
                payment.amount,
                payment.status,
                payment.paidAt,
                payment.canceledAt
        );

        return queryFactory.select(selectExpr)
                .from(payment)
                .where(payment.userId.eq(userId))
                .fetch();

    }


}
