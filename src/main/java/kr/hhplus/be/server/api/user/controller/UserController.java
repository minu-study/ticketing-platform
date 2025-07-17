package kr.hhplus.be.server.api.user.controller;

import kr.hhplus.be.server.domain.balanceLog.dto.BalanceDto;
import kr.hhplus.be.server.domain.user.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/users")
public class UserController {

    public final Map<UUID, User> users = new ConcurrentHashMap<>();

    public UserController() {

        users.put(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"), new User(UUID.fromString("550e8400-e29b-41d4-a716-446655440000")
                , "정민우1", 100000, LocalDateTime.now(), LocalDateTime.now()));
        users.put(UUID.fromString("550e8400-e29b-41d4-a716-446655440001"), new User(UUID.fromString("550e8400-e29b-41d4-a716-446655440001")
                , "정민우2", 50000, LocalDateTime.now(), LocalDateTime.now()));
        users.put(UUID.fromString("550e8400-e29b-41d4-a716-446655440002"), new User(UUID.fromString("550e8400-e29b-41d4-a716-446655440002")
                , "정민우3", 200000, LocalDateTime.now(), LocalDateTime.now()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(users.get(id));
    }

    @GetMapping("/{userId}/balance")
    public ResponseEntity<BalanceDto.BalanceView> getUserBalance(@PathVariable UUID userId) {

        User user = users.get(userId);
        Integer balance = user.getBalance();
        if (balance == null) {
            return ResponseEntity.notFound().build();
        }

        BalanceDto.BalanceView view = BalanceDto.BalanceView.builder()
                .userId(userId)
                .balance(balance)
                .build();

        return ResponseEntity.ok(view);
    }

    @PostMapping("/{userId}/balance/charge")
    public ResponseEntity<BalanceDto.BalanceView> chargeBalance(@PathVariable UUID userId,
                                                                @RequestBody  BalanceDto.ChargeBalance.Request request) {
        if (request.getAmount() <= 0) {
            return ResponseEntity.badRequest().build();
        }
        User user = users.get(userId);
        Integer currentBalance = user.getBalance();
        if (currentBalance == null) {
            currentBalance = 0;
        }
        
        int newBalance = currentBalance + request.getAmount();
        user.setBalance(newBalance);

        BalanceDto.BalanceView view = BalanceDto.BalanceView.builder()
                .userId(userId)
                .balance(newBalance)
                .build();
        
        return ResponseEntity.ok(view);
    }
}