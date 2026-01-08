package com.shankar.book_builder.auth.security.auth;

import org.springframework.security.core.userdetails.UserDetails;

public interface AuthService {
    UserDetails authenticate(LoginRequest request);
}
