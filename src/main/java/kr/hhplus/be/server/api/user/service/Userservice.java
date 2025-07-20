package kr.hhplus.be.server.api.user.service;

import kr.hhplus.be.server.common.exception.AppException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.domain.user.dto.UserDto;
import kr.hhplus.be.server.domain.user.entity.User;
import kr.hhplus.be.server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class Userservice {

    private final UserRepository userRepository;

    @Transactional
    public void createUser(UserDto.SetUser.Request param) {

        Boolean existedUser = userRepository.existsByEmail(param.getEmail());
        if (Boolean.TRUE.equals(existedUser)) {
            throw new AppException(ErrorCode.AUTH002);
        }

        User user = User.createUser(param.getUserName(), param.getEmail());
        userRepository.save(user);
    }

}
