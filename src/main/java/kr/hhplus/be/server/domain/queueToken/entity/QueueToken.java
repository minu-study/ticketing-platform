package kr.hhplus.be.server.domain.queueToken.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.queueToken.dto.QueueDto;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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


    public QueueToken createToken(UUID userId, int position, String token) {
        this.userId = userId;
        this.position = position;
        this.token = token;
        this.issuedAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusMinutes(30);
        return this;
    }

    public QueueDto.QueueTokenInfoView getTokenInfo() {
        return QueueDto.QueueTokenInfoView.builder()
                .token(this.token)
                .userId(this.userId)
                .position(this.position)
                .issuedAt(this.issuedAt)
                .expiresAt(this.expiresAt)
                .build();
    }

    public Boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    public void setExpireToken() {
        this.expiresAt = LocalDateTime.now();
    }


}