package kr.hhplus.be.server.domain.event.repository;

import kr.hhplus.be.server.domain.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface EventRepository extends JpaRepository<Event, Long>, EventQueryRepository{

}