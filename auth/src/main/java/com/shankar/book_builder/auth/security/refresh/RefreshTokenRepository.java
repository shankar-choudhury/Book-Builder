package com.shankar.book_builder.auth.security.refresh;

import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<@NonNull RefreshToken, @NonNull Long> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);
}
