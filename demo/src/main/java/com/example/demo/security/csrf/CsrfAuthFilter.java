package com.example.demo.security.csrf;

import static com.example.demo.security.CookieUtils.*;
import static com.example.demo.security.CookieUtils.TokenType.*;

import com.example.demo.security.jwt.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class CsrfAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final CsrfService csrfService;

    public CsrfAuthFilter(JwtService jwtService, CsrfService csrfService) {
        this.jwtService = jwtService;
        this.csrfService = csrfService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        extractCookie(request, JWT_COOKIE)
                .filter(jwt -> extractCookie(request, CSRF_COOKIE).isEmpty())
                .ifPresent(jwt -> addCsrfCookieToResponse(jwt, response));

        filterChain.doFilter(request, response);
    }

    private void addCsrfCookieToResponse(String jwt, HttpServletResponse response) {
        String sessionId = jwtService.getSessionId(jwt);
        String csrfToken = csrfService.generateToken(sessionId);
        ResponseCookie csrfCookie = createCookie(CSRF_COOKIE, csrfToken, true, false);
        response.addHeader(HttpHeaders.SET_COOKIE, csrfCookie.toString());
    }
}
