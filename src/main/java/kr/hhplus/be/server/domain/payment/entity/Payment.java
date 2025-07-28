package kr.hhplus.be.server.domain.payment.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.payment.vo.PaymentStatusEnums;
import kr.hhplus.be.server.domain.reservation.entity.Reservation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Getter
@Setter
@Table(name = "PAYMENT")
@DynamicUpdate
@EntityListeners(AuditingEntityListener.class)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "BINARY(16)", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private Long reservationId;

    @Column(nullable = false)
    private int amount;

    @Column(nullable = false)
    private String status;

    @Column
    private LocalDateTime paidAt;

    @Column
    private LocalDateTime canceledAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime insertAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updateAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservationId", insertable = false, updatable = false)
    private Reservation reservation;

    public static Payment create(UUID userId, Long reservationId, int amount) {
        Payment payment = new Payment();
        payment.userId = userId;
        payment.reservationId = reservationId;
        payment.amount = amount;
        payment.status = PaymentStatusEnums.PENDING.getStatus();
        return payment;
    }

    public void complete() {
        this.status = PaymentStatusEnums.COMPLETE.getStatus();
        this.paidAt = LocalDateTime.now();
    }

    public void fail() {
        this.status = PaymentStatusEnums.FAILED.getStatus();
    }

    public void cancel() {
        this.status = PaymentStatusEnums.CANCELED.getStatus();
        this.canceledAt = LocalDateTime.now();
    }

    public boolean isCompleted() {
        return PaymentStatusEnums.COMPLETE.getStatus().equals(this.status);
    }

    public boolean isFailed() {
        return PaymentStatusEnums.FAILED.getStatus().equals(this.status);
    }

}