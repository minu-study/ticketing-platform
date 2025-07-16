package kr.hhplus.be.server.api.user.service;

import kr.hhplus.be.server.domain.user.entity.User;
import kr.hhplus.be.server.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class Userservice {

    private final UserRepository userRepository;

    public Userservice(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Transactional
    public User createUser(String userName) {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, userName, 0);
        return userRepository.save(user);
    }



}
