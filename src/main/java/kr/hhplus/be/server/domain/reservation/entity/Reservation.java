package kr.hhplus.be.server.domain.reservation.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.eventSchedule.entity.EventSchedule;
import kr.hhplus.be.server.domain.seat.entity.Seat;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@DynamicUpdate
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reservation")
@EntityListeners(AuditingEntityListener.class)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "BINARY(16)", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private Long seatId;

    @Column(nullable = false)
    private Long scheduleId;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private LocalDateTime reservedAt;

    @Column
    private LocalDateTime expiresAt;

    @Column
    private LocalDateTime canceledAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime insertAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updateAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seatId", insertable = false, updatable = false)
    private Seat seat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scheduleId", insertable = false, updatable = false)
    private EventSchedule eventSchedule;

}