package kr.hhplus.be.server.domain.queueToken.repository;

import kr.hhplus.be.server.domain.queueToken.entity.QueueToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface QueueTokenRepository extends JpaRepository<QueueToken, Long> {
    Optional<QueueToken> findByToken(String token);
    Optional<QueueToken> findByUserIdAndExpiresAtAfter(UUID userId, LocalDateTime expiresAt);
    Integer findMaxPositionByExpiresAtAfter(LocalDateTime expiresAt);
    int countByPositionLessThanAndExpiresAtAfter(int position, LocalDateTime expiresAt);
    List<QueueToken> findByExpiresAtBefore(LocalDateTime expiresAt);
    List<QueueToken> findByExpiresAtAfterOrderByIssuedAt(LocalDateTime now);
}