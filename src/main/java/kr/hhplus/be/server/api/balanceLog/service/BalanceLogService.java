package kr.hhplus.be.server.api.balanceLog.service;

import kr.hhplus.be.server.domain.balanceLog.entity.BalanceLog;
import kr.hhplus.be.server.domain.balanceLog.repository.BalanceLogRepository;
import kr.hhplus.be.server.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BalanceLogService {

    private final BalanceLogRepository balanceLogRepository;

    @Async
    public void saveChargeLogAsync(User user, int amount) {

        try {
            BalanceLog balanceLog = new BalanceLog().createCharge(user, amount);
            balanceLogRepository.save(balanceLog);
        } catch (Exception e) {
            log.error("saveChargeLogAsync error : {}", e.getMessage());
        }
    }


    @Async
    public void savePaymentLogAsync(User user, int amount) {
        try {
            BalanceLog balanceLog = new BalanceLog().createPayment(user, amount);
            balanceLogRepository.save(balanceLog);
        } catch (Exception e) {
            log.error("savePaymentLogAsync error : {}", e.getMessage());
        }
    }

}
