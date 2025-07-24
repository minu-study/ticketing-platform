package kr.hhplus.be.server.domain.user.repository;

import com.querydsl.core.types.Expression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.hhplus.be.server.domain.user.dto.UserDto;
import kr.hhplus.be.server.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

import static com.querydsl.core.types.Projections.fields;
import static kr.hhplus.be.server.domain.queueToken.entity.QQueueToken.queueToken;
import static kr.hhplus.be.server.domain.user.entity.QUser.user;


@RequiredArgsConstructor
public class UserQueryRepositoryImpl implements UserQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public UserDto.UserView getUserView(String queueTokenString) {

        Expression<UserDto.UserView> selectExpr = fields(UserDto.UserView.class,
                user.id,
                user.userName,
                user.email,
                user.balance

        );

        return queryFactory.select(selectExpr)
                .from(user)
                .innerJoin(queueToken).on(user.id.eq(queueToken.userId))
                .where(queueToken.token.eq(queueTokenString))
                .fetchOne();

    }

    @Override
    public User getUser(String queueTokenString) {
        return queryFactory.select(user)
                .from(user)
                .innerJoin(queueToken).on(user.id.eq(queueToken.userId))
                .where(queueToken.token.eq(queueTokenString))
                .fetchOne();
    }


}
