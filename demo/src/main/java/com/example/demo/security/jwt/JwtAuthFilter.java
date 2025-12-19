package com.example.demo.security.jwt;

import static com.example.demo.security.CookieUtils.*;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Autowired
    public JwtAuthFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        extractCookie(request, TokenType.JWT_COOKIE)
                .ifPresentOrElse(
                        token -> handleTokenPresent(request, response, filterChain, token),
                        () -> doFilterUnchecked(request, response, filterChain)
                );
    }

    private void handleTokenPresent(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain,
                                    String token) {
        try {
            processJwtToken(request, token);
            doFilterUnchecked(request, response, filterChain);
        } catch (Exception e) {
            handleAuthenticationError(e);
            doFilterUnchecked(request, response, filterChain);
        }
    }

    private void doFilterUnchecked(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain) {
        try {
            filterChain.doFilter(request, response);
        } catch (IOException | ServletException e) {
            throw new RuntimeException("Filter chain processing failed", e);
        }
    }

    private void processJwtToken(HttpServletRequest request, String jwt) {
        String username = jwtService.username(jwt);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (username != null && authentication == null) {
            authenticateUser(request, jwt, username);
        }
    }

    private void authenticateUser(HttpServletRequest request, String jwt, String username) {
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
        if (jwtService.validateToken(jwt, userDetails)) {
            setSecurityContextAuthentication(request, userDetails);
        }
    }

    private void setSecurityContextAuthentication(HttpServletRequest request, UserDetails userDetails) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    private void handleAuthenticationError(Exception e) {
        // TODO: Replace with proper logger
        System.out.println("Error with JWT Authentication: " + e.getMessage());
        e.printStackTrace();
    }

}
