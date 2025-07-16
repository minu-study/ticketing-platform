package kr.hhplus.be.server.domain.payment.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.reservation.entity.Reservation;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
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

    protected Payment() {
    }

    private Payment(UUID userId, Long reservationId, int amount, String status) {
        this.userId = userId;
        this.reservationId = reservationId;
        this.amount = amount;
        this.status = status;
    }

}