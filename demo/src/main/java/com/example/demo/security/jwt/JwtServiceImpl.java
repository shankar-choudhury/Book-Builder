package com.example.demo.security.jwt;

import com.example.demo.security.config.AppSecurityProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;

@Service
public class JwtServiceImpl implements JwtService{
    private final String secretKey;
    private final long jwtExpiration = 3600_000;

    public JwtServiceImpl(AppSecurityProperties properties) {
        this.secretKey = properties.getSecrets().getJwt();
    }

    @Override
    public String username(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public String generateToken(UserDetails details) {
        return generateToken(new HashMap<>(), details);
    }

    @Override
    public String generateToken(UserDetails details, String sessionId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sessionId", sessionId);
        return buildToken(claims, details, jwtExpiration);
    }

    @Override
    public String generateToken(Map<String, Object> extraClaims, UserDetails details) {
        Map<String, Object> claims = new HashMap<>(extraClaims);
        claims.put("sessionId", UUID.randomUUID().toString());
        return buildToken(claims, details, jwtExpiration);
    }

    @Override
    public long expirationTime() {
        return jwtExpiration;
    }

    @Override
    public boolean validateToken(String token, UserDetails details) {
        String username = username(token);
        return Objects.equals(username, details.getUsername()) && !isTokenExpired(token);
    }

    @Override
    public String getSessionId(String token) {
        return extractClaim(token, claims -> claims.get("sessionId", String.class));
    }

    private String buildToken(
            Map<String,Object> extraClaims,
            UserDetails details,
            long expiration
    ) {
        return Jwts.builder()
                .claims()
                .add(extraClaims)
                .subject(details.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .and()
                .signWith(getSignInKey())
                .compact();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {return extractClaim(token, Claims::getExpiration);}

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
