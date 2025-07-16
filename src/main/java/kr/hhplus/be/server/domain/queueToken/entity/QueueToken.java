package kr.hhplus.be.server.domain.queueToken.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@DynamicUpdate
@Table(name = "queue_token")
@EntityListeners(AuditingEntityListener.class)
public class QueueToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "BINARY(16)", nullable = false)
    private UUID userId;

    @Column(unique = true, nullable = false)
    private String token;

    @Column(nullable = false)
    private int position;

    @Column(nullable = false)
    private LocalDateTime issuedAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime insertAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updateAt;

    protected QueueToken() {
    }

    public QueueToken(String token, UUID userId, int position, LocalDateTime issuedAt, LocalDateTime expiresAt) {
        this.token = token;
        this.userId = userId;
        this.position = position;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
    }

}