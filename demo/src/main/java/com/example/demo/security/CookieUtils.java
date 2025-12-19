package com.example.demo.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseCookie;

import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class CookieUtils {
    public enum TokenType {
        JWT_COOKIE("JWTCookie"),
        CSRF_COOKIE("XSRF-TOKEN"),
        CSRF_HEADER("X-XSRF-TOKEN"),
        REFRESH_COOKIE("RefreshCookie");

        private final String value;

        TokenType(String value) {
            this.value = value;
        }

        public String getLabel() {
            return value;
        }
    }
    private CookieUtils(){}
    public static Optional<String> extractCookie(HttpServletRequest request, TokenType tokenType) {
        return Optional.ofNullable(request.getCookies())
                .stream()
                .flatMap(Arrays::stream)
                .filter(cookie -> Objects.equals(tokenType.getLabel(), cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    public static ResponseCookie createCookie(TokenType tokenType, String value, boolean secure, boolean httpOnly) {
        return ResponseCookie.from(tokenType.getLabel(), value)
                .path("/")
                .secure(secure)
                .sameSite("Lax")
                .httpOnly(httpOnly)
                .maxAge(Duration.ofDays(1))
                .build();
    }

    public static ResponseCookie createExpiredCookie(TokenType tokenType, boolean httpOnly) {
        return ResponseCookie.from(tokenType.getLabel(), "")
                .secure(true)
                .sameSite("Lax")
                .httpOnly(httpOnly)
                .maxAge(0)
                .build();
    }
}

