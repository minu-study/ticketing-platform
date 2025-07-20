package kr.hhplus.be.server.domain.eventSchedule.repository;

import kr.hhplus.be.server.domain.eventSchedule.entity.EventSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface EventScheduleRepository extends JpaRepository<EventSchedule, Long> {

}