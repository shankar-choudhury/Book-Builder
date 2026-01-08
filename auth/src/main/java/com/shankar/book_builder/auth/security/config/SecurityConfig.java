package com.shankar.book_builder.auth.security.config;

import com.shankar.book_builder.auth.security.CookieUtils;
import com.shankar.book_builder.auth.security.jwt.JwtCookieAuthFilter;
import com.shankar.book_builder.auth.security.ratelimiter.RateLimitingFilter;
import com.shankar.book_builder.auth.security.ratelimiter.RequestCachingFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static com.shankar.book_builder.auth.security.CookieUtils.TokenType.*;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AppSecurityProperties props;
    private final JwtCookieAuthFilter jwtFilter;
    private final RateLimitingFilter rateLimitingFilter;
    private final RequestCachingFilter requestCachingFilter;
    private final AuthenticationProvider authProvider;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        CsrfTokenRequestAttributeHandler requestHandler = new XorCsrfTokenRequestAttributeHandler();

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(requestHandler)
                        .ignoringRequestMatchers(
                                props.getAuthAllowPathPrefix() + "/register",
                                props.getAuthAllowPathPrefix() + "/refresh")
                        .sessionAuthenticationStrategy(new NullAuthenticatedSessionStrategy())
                )

                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(requestCachingFilter, CsrfFilter.class)
                .addFilterAfter(rateLimitingFilter, RequestCachingFilter.class)

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                props.getAuthAllowPathPrefix() + "/register",
                                props.getAuthAllowPathPrefix() + "/csrf",
                                props.getAuthAllowPathPrefix() + "/login"
                                ).permitAll()
                        .anyRequest().authenticated()
                )

                .logout(logout -> logout
                        .logoutUrl("/api/logout")
                        .logoutSuccessHandler((req, res, auth) -> {
                            CookieUtils.addExpired(res, JWT, props);
                            CookieUtils.addExpired(res, REFRESH, props);
                            CookieUtils.addExpired(res, CSRF, props);
                            res.setStatus(HttpServletResponse.SC_OK);
                        })
                )

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> res.sendError(401, "Unauthorized"))
                        .accessDeniedHandler((req, res, e) -> res.sendError(403, "Forbidden"))
                )

                .authenticationProvider(authProvider);

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of(props.getFrontendOrigin()));
        cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);
        cfg.setExposedHeaders(List.of("X-XSRF-TOKEN"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
