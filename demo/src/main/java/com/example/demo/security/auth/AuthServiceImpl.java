package com.example.demo.security.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService{
    private final AuthenticationManager manager;

    public AuthServiceImpl(AuthenticationManager manager) {
        this.manager = manager;
    }

    @Override
    public UserDetails authenticate(LoginRequest request) {
        Authentication auth = manager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                ));
        return (UserDetails) auth.getPrincipal();
    }
}
