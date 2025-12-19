package com.example.demo.security.refresh;

import com.example.demo.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository repo;

    private final Duration refreshTtl = Duration.ofDays(1);

    public RefreshTokenServiceImpl(RefreshTokenRepository repo) {
        this.repo = repo;
    }

    @Override
    public String issue(User user, String sessionId) {
        String raw = generateRawToken();
        String hash = sha256Hex(raw);

        RefreshToken t = new RefreshToken();
        t.setUser(user);
        t.setSessionId(sessionId);
        t.setTokenHash(hash);
        t.setExpiresAt(Instant.now().plus(refreshTtl));

        repo.save(t);
        return raw;
    }

    @Override
    public RefreshToken verify(String rawToken) {
        return Optional.of(rawToken)
                .map(RefreshTokenServiceImpl::sha256Hex)
                .flatMap(repo::findByTokenHash)
                .filter(RefreshToken::isActive)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Invalid, expired, or revoked refresh token"
                        ));
    }

    @Override
    public String rotate(RefreshToken current) {
        current.setRevokedAt(Instant.now());
        repo.save(current);
        return issue(current.getUser(), current.getSessionId());
    }

    @Override
    public void revoke(RefreshToken token) {
        token.setRevokedAt(Instant.now());
        repo.save(token);
    }

    private static String generateRawToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private static String sha256Hex(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
