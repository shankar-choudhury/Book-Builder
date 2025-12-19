package com.example.demo.security.csrf;

import static com.example.demo.security.CookieUtils.*;

import com.example.demo.security.config.AppSecurityProperties;
import com.example.demo.security.csrf.exception.*;
import com.example.demo.security.jwt.JwtService;
import com.example.demo.security.jwt.MissingJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

@Component
public class CsrfValidatorFilter extends OncePerRequestFilter {
    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");

    private final JwtService jwtService;
    private final CsrfService csrfService;
    private final AppSecurityProperties properties;


    public CsrfValidatorFilter(JwtService jwtService, CsrfService csrfService, AppSecurityProperties properties) {
        this.jwtService = jwtService;
        this.csrfService = csrfService;
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        if (isSafeMethod(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = extractCookie(request, TokenType.JWT_COOKIE)
                    .orElseThrow(() -> new MissingJwtException("JWT token not found"));

            String sessionId = jwtService.getSessionId(jwt);

            String header = requireCsrfHeader(request);
            String cookie = requireCsrfCookie(request);

            if (!header.equals(cookie)) {
                throw new MismatchingCsrfValuesException(header, cookie, "CSRF tokens do not match");
            }

            if (!csrfService.validateToken(header, sessionId)) {
                throw new InvalidCsrfTokenException(header, "Invalid CSRF token");
            }

            filterChain.doFilter(request, response);
        } catch (SecurityException e) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        }
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith(properties.getAuthAllowPathPrefix());
    }

    private String requireCsrfHeader(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(TokenType.CSRF_HEADER.getLabel()))
                .filter(StringUtils::hasText)
                .orElseThrow(() -> new MissingCsrfTokenException("CSRF token not found in header"));
    }

    private String requireCsrfCookie(HttpServletRequest request) {
        return extractCookie(request, TokenType.CSRF_COOKIE)
                .filter(StringUtils::hasText)
                .orElseThrow(() -> new MissingCsrfTokenException("CSRF token not found in cookie"));
    }

    private boolean isSafeMethod(String method) {
        return SAFE_METHODS.contains(method.toUpperCase());
    }

}
