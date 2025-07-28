package kr.hhplus.be.server.api.user.controller;

import jakarta.validation.Valid;
import kr.hhplus.be.server.api.user.service.UserService;
import kr.hhplus.be.server.common.model.ApiResponse;
import kr.hhplus.be.server.common.util.CommonUtil;
import kr.hhplus.be.server.domain.balanceLog.dto.BalanceDto;
import kr.hhplus.be.server.domain.user.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userservice;

    /**
     * 유저 생성 API
     * 이름과 이메일을 통해 유저를 생성
     */
    @PostMapping
    public ResponseEntity<ApiResponse> createUser(@Valid @RequestBody UserDto.SetUser.Request param) {
        log.info("createUser param : {}", param);
        userservice.createUser(param);
        return CommonUtil.convertResponse();
    }

    /**
     * 유저 조회 API
     * 토큰을 통해 유저정보를 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse> getUser() {
        UserDto.GetUser.Response response = userservice.getUser();
        log.info("getUser response : {}", response);
        return CommonUtil.convertResponse(response);
    }

    /**
     * 유저 잔액 충전 API
     * 요청 금액에 따라 잔액을 충전
     */
    @PostMapping
    public ResponseEntity<ApiResponse> chargeBalance(@Valid @RequestBody BalanceDto.ChargeBalance.Request param) {
        log.info("chargeBalance param : {}", param);
        BalanceDto.ChargeBalance.Response response = userservice.chargeBalance(param);
        log.info("chargeBalance response : {}", response);
        return CommonUtil.convertResponse(response);
    }



}