package com.shankar.book_builder.auth.security;

import com.shankar.book_builder.auth.security.config.AppSecurityProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;


public interface CookieUtils {
    enum TokenType {
        JWT("JWT"),
        REFRESH("REFRESH"),
        CSRF("XSRF-TOKEN");

        private final String value;

        TokenType(String value) {
            this.value = value;
        }

        public String getLabel() {
            return value;
        }
    }

    static Optional<String> extract(HttpServletRequest request, TokenType expectedCookieName) {
        return Optional.ofNullable(request.getCookies())
                .stream()
                .flatMap(Arrays::stream)
                .filter(cookie -> Objects.equals(expectedCookieName.getLabel(), cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    static void add(HttpServletResponse res, TokenType name, String value, boolean httpOnly, AppSecurityProperties props) {
        ResponseCookie cookie = ResponseCookie.from(name.getLabel(), value)
                .path(props.getCookies().getPath())
                .httpOnly(httpOnly)
                .secure(props.getCookies().isSecure())
                .sameSite(props.getCookies().getSameSite())
                .build();

        res.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    static void addExpired(HttpServletResponse res, TokenType tokenType, AppSecurityProperties props) {
        ResponseCookie cookie = ResponseCookie.from(tokenType.getLabel(), "")
                .path(props.getCookies().getPath())
                .httpOnly(true)
                .secure(props.getCookies().isSecure())
                .sameSite(props.getCookies().getSameSite())
                .maxAge(0)
                .build();

        res.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
