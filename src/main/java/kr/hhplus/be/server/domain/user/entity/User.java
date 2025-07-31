package kr.hhplus.be.server.domain.user.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.common.exception.AppException;
import kr.hhplus.be.server.common.exception.ErrorCode;
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
@DynamicUpdate
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user")
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(nullable = false)
    private String userName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private int balance;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime insertAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updateAt;


    // 정적 팩토리 메서드 - 새로운 User 생성
    public static User createUser(String userName, String email) {
        User user = new User();
        user.id = UUID.randomUUID();
        user.userName = userName;
        user.email = email;
        user.balance = 0;
        return user;
    }

    // 잔액 충전 메서드
    public void chargeBalance(int amount) {
        if (amount <= 0) {
            throw new AppException(ErrorCode.INVALID_CHARGE_AMOUNT);
        }
        this.balance += amount;
    }

    // 잔액 사용 메서드
    public void useBalance(int amount) {
        if (amount <= 0) {
            throw new AppException(ErrorCode.INSUFFICIENT_PAYMENT_AMOUNT);
        }
        if (this.balance < amount) {
            throw new AppException(ErrorCode.INSUFFICIENT_PAYMENT_AMOUNT);
        }
        this.balance -= amount;
    }


}