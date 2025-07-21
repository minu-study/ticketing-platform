package kr.hhplus.be.server.api.user.controller;

import jakarta.validation.Valid;
import kr.hhplus.be.server.api.user.service.Userservice;
import kr.hhplus.be.server.common.model.ApiResponse;
import kr.hhplus.be.server.common.util.CommonUtil;
import kr.hhplus.be.server.domain.balanceLog.dto.BalanceDto;
import kr.hhplus.be.server.domain.user.dto.UserDto;
import kr.hhplus.be.server.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final Userservice userservice;

    // 유저 생성
    @PostMapping
    public ResponseEntity<ApiResponse> createUser(@Valid @RequestBody UserDto.SetUser.Request param) {
        log.info("createUser param : {}", param);
        userservice.createUser(param);
        return CommonUtil.convertResponse();
    }

    // 유저 조회
    @GetMapping
    public ResponseEntity<ApiResponse> getUser() {
        UserDto.GetUser.Response response = userservice.getUser();
        log.info("getUser response : {}", response);
        return CommonUtil.convertResponse(response);
    }







}