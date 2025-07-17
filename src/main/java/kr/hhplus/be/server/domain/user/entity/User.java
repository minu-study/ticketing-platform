package kr.hhplus.be.server.domain.user.entity;

import jakarta.persistence.*;
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

@Entity
@DynamicUpdate
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "user")
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(nullable = false)
    private String userName;

    @Column(nullable = false)
    private int balance;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime insertAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updateAt;

}