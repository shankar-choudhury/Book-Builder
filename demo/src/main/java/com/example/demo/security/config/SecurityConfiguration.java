package com.example.demo.security.config;

import static com.example.demo.security.CookieUtils.*;
import static com.example.demo.security.CookieUtils.TokenType.*;

import com.example.demo.security.csrf.CsrfAuthFilter;
import com.example.demo.security.csrf.CsrfValidatorFilter;
import com.example.demo.security.jwt.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    private final AppSecurityProperties properties;
    private final AuthenticationProvider authProvider;
    private final JwtAuthFilter jwtFilter;
    private final CsrfAuthFilter csrfFilter;
    private final CsrfValidatorFilter csrfTokenFilter;

    @Autowired
    public SecurityConfiguration(AppSecurityProperties properties, AuthenticationProvider authProvider, JwtAuthFilter jwtFilter, CsrfAuthFilter csrfFilter, CsrfValidatorFilter csrfTokenFilter) {
        this.properties = properties;
        this.authProvider = authProvider;
        this.jwtFilter = jwtFilter;
        this.csrfFilter = csrfFilter;
        this.csrfTokenFilter = csrfTokenFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(csrfFilter, BasicAuthenticationFilter.class)
                .addFilterAfter(csrfTokenFilter, CsrfAuthFilter.class)

                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .sessionManagement(session -> session.sessionCreationPolicy((SessionCreationPolicy.STATELESS)))

                .authorizeHttpRequests(auth -> auth.requestMatchers( properties.getAuthAllowPathPrefix() + "/**","/api/users/find-user/**").permitAll()
                        .anyRequest().authenticated())

                .logout(logout -> logout
                        .logoutUrl("/api/logout")
                        .logoutSuccessHandler(logoutSuccessHandler())
                        .deleteCookies(JWT_COOKIE.getLabel(), CSRF_COOKIE.getLabel(), REFRESH_COOKIE.getLabel()))

                .authenticationProvider(authProvider)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of(properties.getFrontendOrigin()));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));

        config.setAllowCredentials(true);
        config.setExposedHeaders(List.of(CSRF_HEADER.getLabel()));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {

        return (request, response, authentication) -> {
            ResponseCookie clearJwt = createExpiredCookie(JWT_COOKIE, true);
            ResponseCookie clearRefresh = createExpiredCookie(REFRESH_COOKIE, true);
            ResponseCookie clearCsrf = createExpiredCookie(CSRF_COOKIE, false);

            response.addHeader(HttpHeaders.SET_COOKIE, clearJwt.toString());
            response.addHeader(HttpHeaders.SET_COOKIE, clearRefresh.toString());
            response.addHeader(HttpHeaders.SET_COOKIE, clearCsrf.toString());

            response.setStatus(HttpStatus.OK.value());
        };
    }

}
