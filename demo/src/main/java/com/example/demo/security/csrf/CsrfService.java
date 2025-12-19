package com.example.demo.security.csrf;

public interface CsrfService {
    String generateToken(String sessionIdentifier);
    boolean validateToken(String token, String sessionIdentifier);
}
