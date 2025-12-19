package com.example.demo.security.refresh;

import com.example.demo.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens",
        indexes = { @Index(name = "idx_refresh_token_hash", columnList = "tokenHash", unique = true) })
@Getter
@Setter
@NoArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User user;

    @Column(nullable = false, unique = true, length = 64)
    private String tokenHash; // hex SHA-256

    @Column(nullable = false, length = 36)
    private String sessionId; // stable per “login session”

    @Column(nullable = false)
    private Instant expiresAt;

    @Column
    private Instant revokedAt;

    public boolean isActive() {
        return revokedAt == null && Instant.now().isBefore(expiresAt);
    }
}
