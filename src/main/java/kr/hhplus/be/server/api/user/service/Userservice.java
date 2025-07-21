package kr.hhplus.be.server.api.user.service;

import kr.hhplus.be.server.common.exception.AppException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.model.ApiResponse;
import kr.hhplus.be.server.common.util.CommonUtil;
import kr.hhplus.be.server.domain.balanceLog.dto.BalanceDto;
import kr.hhplus.be.server.domain.queueToken.entity.QueueToken;
import kr.hhplus.be.server.domain.queueToken.repository.QueueTokenRepository;
import kr.hhplus.be.server.domain.user.dto.UserDto;
import kr.hhplus.be.server.domain.user.entity.User;
import kr.hhplus.be.server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class Userservice {

    private final UserRepository userRepository;
    private final QueueTokenRepository queueTokenRepository;

    @Transactional
    public void createUser(UserDto.SetUser.Request param) {

        Boolean existedUser = userRepository.existsByEmail(param.getEmail());
        if (Boolean.TRUE.equals(existedUser)) {
            throw new AppException(ErrorCode.AUTH002);
        }

        User user = User.createUser(param.getUserName(), param.getEmail());
        userRepository.save(user);
    }

    public UserDto.GetUser.Response getUser() {

        String token = CommonUtil.getQueueToken();
        UserDto.UserView view = userRepository.getUserView(token);
        return UserDto.GetUser.Response.builder()
                .view(view)
                .build();
    }

    @Transactional
    public BalanceDto.BalanceView chargeBalance(BalanceDto.ChargeBalance.Request param) {

        String token = CommonUtil.getQueueToken();

        if (param.getAmount() <= 0) {
            throw new AppException(ErrorCode.PAYMENT003);
        }

        User user = userRepository.getUser(token);
        user.chargeBalance(param.getAmount());

        return BalanceDto.BalanceView.builder()
                .userId(user.getId())
                .balance(user.getBalance())
                .build();
    }

}
