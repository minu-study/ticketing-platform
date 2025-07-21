package kr.hhplus.be.server.domain.user.repository;

import kr.hhplus.be.server.domain.user.dto.UserDto;
import kr.hhplus.be.server.domain.user.entity.User;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserQueryRepository {

    UserDto.UserView getUserView(String queueTokenString);
    User getUser(String queueTokenString);

}
