package com.shankar.book_builder.auth.security.jwt;

import com.shankar.book_builder.auth.users.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtEncoder encoder;
    private final Duration ttl = Duration.ofHours(1);

    public String issue(User user, String sessionId) {
        Instant now = Instant.now();
        JwsHeader headers = JwsHeader.with(MacAlgorithm.HS256).build();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(user.getUsername())
                .issuedAt(now)
                .expiresAt(now.plus(ttl))
                .claim("sid", sessionId)
                .build();

        return encoder.encode(JwtEncoderParameters.from(headers, claims)).getTokenValue();
    }

    public Duration ttl() {
        return ttl;
    }
}
