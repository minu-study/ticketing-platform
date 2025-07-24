package kr.hhplus.be.server.api.user.service;

import kr.hhplus.be.server.api.balanceLog.service.BalanceLogService;
import kr.hhplus.be.server.common.exception.AppException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.util.CommonUtil;
import kr.hhplus.be.server.domain.balanceLog.dto.BalanceDto;
import kr.hhplus.be.server.domain.user.dto.UserDto;
import kr.hhplus.be.server.domain.user.entity.User;
import kr.hhplus.be.server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final BalanceLogService balanceLogService;

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

    @Transactional(readOnly = true)
    public UserDto.GetUser.Response getUser() {

        String token = CommonUtil.getQueueToken();
        UserDto.UserView view = userRepository.getUserView(token);
        return UserDto.GetUser.Response.builder()
                .view(view)
                .build();
    }

    @Transactional
    public BalanceDto.ChargeBalance.Response chargeBalance(BalanceDto.ChargeBalance.Request param) {

        String token = CommonUtil.getQueueToken();

        if (param.getAmount() <= 0) {
            throw new AppException(ErrorCode.PAYMENT003);
        }

        User user = userRepository.getUser(token);
        user.chargeBalance(param.getAmount());

        BalanceDto.BalanceView view = BalanceDto.BalanceView.builder()
                .userId(user.getId())
                .balance(user.getBalance())
                .build();

        balanceLogService.saveChargeLogAsync(user, param.getAmount());

        return BalanceDto.ChargeBalance.Response.builder()
                .view(view)
                .build();
    }

}
