package kr.hhplus.be.server.domain.balanceLog.repository;

import kr.hhplus.be.server.domain.balanceLog.entity.BalanceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BalanceLogRepository extends JpaRepository<BalanceLog, Long> {
}