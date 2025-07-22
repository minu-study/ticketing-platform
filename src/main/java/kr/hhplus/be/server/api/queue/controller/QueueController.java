package kr.hhplus.be.server.api.queue.controller;

import kr.hhplus.be.server.api.queue.dto.QueueDto;
import kr.hhplus.be.server.api.queue.service.QueueService;
import kr.hhplus.be.server.common.model.ApiResponse;
import kr.hhplus.be.server.common.util.CommonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/queue")
@RequiredArgsConstructor
public class QueueController {

    private final QueueService queueService;

    /**
     * 대기열 토큰 발급 API
     * 서비스 이용을 위한 대기열 토큰 발급
     */
    @PostMapping("/token")
    public ResponseEntity<ApiResponse> issueToken(@RequestBody QueueDto.GetQueueToken.Request param) {
        log.info("issueToken param : {}", param);
        QueueDto.GetQueueToken.Response response = queueService.issueToken(param);
        log.info("issueToken response : {}", response);
        return CommonUtil.convertResponse(response);
    }

    /**
     * 대기번호 조회 API
     * 사용자 토큰을 통해 대기 순번을 조회
     */
    @GetMapping("/position")
    public ResponseEntity<ApiResponse> getQueuePosition() {
        QueueDto.GetQueuePosition.Response response = queueService.getQueuePosition();
        log.info("getQueuePosition response : {}", response);
        return CommonUtil.convertResponse(response);
    }

}