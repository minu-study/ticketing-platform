package kr.hhplus.be.server.domain.seat.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.eventSchedule.entity.EventSchedule;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@DynamicUpdate
@Table(name = "seat")
@EntityListeners(AuditingEntityListener.class)
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long scheduleId;

    @Column(nullable = false)
    private int seatNumber;

    @Column(nullable = false)
    private String seatType;

    @Column(nullable = false)
    private String status;

    @Column
    private LocalDateTime holdExpiresAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime insertAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updateAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scheduleId", insertable = false, updatable = false)
    private EventSchedule eventSchedule;

    protected Seat() {
    }

    public Seat(Long scheduleId, int seatNumber, String seatType) {
        this.scheduleId = scheduleId;
        this.seatNumber = seatNumber;
        this.seatType = seatType;
        this.status = "AVAILABLE";
    }

}