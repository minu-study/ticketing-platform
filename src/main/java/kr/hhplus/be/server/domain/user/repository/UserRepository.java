package kr.hhplus.be.server.domain.user.repository;

import kr.hhplus.be.server.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, UserQueryRepository {

    Boolean existsByEmail(String email);
}