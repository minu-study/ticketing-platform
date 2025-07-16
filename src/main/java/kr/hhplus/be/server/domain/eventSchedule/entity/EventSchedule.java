package kr.hhplus.be.server.domain.eventSchedule.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.domain.event.entity.Event;
import kr.hhplus.be.server.domain.seat.entity.Seat;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "event_schedule")
@EntityListeners(AuditingEntityListener.class)
public class EventSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long eventId;

    @Column(nullable = false)
    private LocalDateTime startDateTime;

    @Column(nullable = false)
    private LocalDateTime endDateTime;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime insertAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updateAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eventId", insertable = false, updatable = false)
    private Event event;
    
    @OneToMany(mappedBy = "scheduleId", fetch = FetchType.LAZY)
    private List<Seat> seats = new ArrayList<>();

    protected EventSchedule() {
    }

    public EventSchedule(Long eventId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        this.eventId = eventId;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }

}