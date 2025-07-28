package kr.hhplus.be.server.api.balanceLog.service;

import kr.hhplus.be.server.domain.balanceLog.entity.BalanceLog;
import kr.hhplus.be.server.domain.balanceLog.repository.BalanceLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BalanceLogService {

    private final BalanceLogRepository balanceLogRepository;

    @Async("logExecutor")
    public void saveLogAsync(UUID userId, int amount, String type) {

        BalanceLog balanceLog = BalanceLog.builder()
                .userId(userId)
                .amount(amount)
                .type(type)
                .build();
        balanceLogRepository.save(balanceLog);

    }

}
