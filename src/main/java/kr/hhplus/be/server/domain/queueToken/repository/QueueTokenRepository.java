package kr.hhplus.be.server.domain.queueToken.repository;

import kr.hhplus.be.server.domain.queueToken.entity.QueueToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface QueueTokenRepository extends JpaRepository<QueueToken, Long> {
}