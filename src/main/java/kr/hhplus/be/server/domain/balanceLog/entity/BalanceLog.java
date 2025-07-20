package kr.hhplus.be.server.domain.balanceLog.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "balance_log")
public class BalanceLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "BINARY(16)", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private int amount;

    @Column(nullable = false)
    private String type;

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime createdAt;

    public BalanceLog() {
    }

    private BalanceLog(UUID userId, int amount, String type) {
        this.userId = userId;
        this.amount = amount;
        this.type = type;
        this.createdAt = LocalDateTime.now();
    }


}
