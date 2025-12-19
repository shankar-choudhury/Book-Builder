package com.example.demo.security.auth;

import org.springframework.security.core.userdetails.UserDetails;

public interface AuthService {
    UserDetails authenticate(LoginRequest request);
}
