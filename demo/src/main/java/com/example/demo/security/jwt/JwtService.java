package com.example.demo.security.jwt;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;

public interface JwtService {
    String username(String token);
    String generateToken(UserDetails details);
    String generateToken(UserDetails details, String sessionId);
    String generateToken(Map<String,Object> extraClaims, UserDetails details);
    long expirationTime();
    boolean validateToken(String token, UserDetails details);
    String getSessionId(String token);
}
