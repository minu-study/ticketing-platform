package kr.hhplus.be.server.domain.balanceLog.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

import static kr.hhplus.be.server.domain.balanceLog.vo.BalanceActionEnums.CHARGE;
import static kr.hhplus.be.server.domain.balanceLog.vo.BalanceActionEnums.USE;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
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

    public BalanceLog createCharge(User user, int amount) {
        this.userId = user.getId();
        this.amount = amount;
        this.type = CHARGE.getAction();
        return this;
    }

    public BalanceLog createPayment(User user, int amount) {
        this.userId = user.getId();
        this.amount = amount;
        this.type = USE.getAction();
        return this;
    }

}
