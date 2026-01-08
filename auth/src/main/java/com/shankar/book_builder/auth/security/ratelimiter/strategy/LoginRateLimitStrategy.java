package com.shankar.book_builder.auth.security.ratelimiter.strategy;

import com.shankar.book_builder.auth.security.auth.LoginRequest;
import com.shankar.book_builder.auth.security.config.AppSecurityProperties;
import io.github.bucket4j.Bandwidth;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LoginRateLimitStrategy extends BaseLimitingStrategy {
    private final ObjectMapper om;
    private final AppSecurityProperties props;

    @Override
    public String resolveKey(HttpServletRequest request) {
        return getUsername(request)
                .map(u -> "login:" + getIp(request) + ":" + u)
                .orElseGet(() -> "login:" + getIp(request) + ":unknown-user");
    }

    @Override
    public Bandwidth resolveBandwidth(HttpServletRequest request, AppSecurityProperties props) {
        int tokens = getUsername(request)
                .map(u -> props.getRateLimiter().getLoginToken())
                .orElseGet(() -> props.getRateLimiter().getLoginToken() / 2);

        return createBandwidth(tokens, props);
    }

    private Optional<String> getUsername(HttpServletRequest request) {
        return getBody(request)
                .flatMap(this::tryParseLogin)
                .map(LoginRequest::username)
                .map(String::trim)
                .filter(u -> !u.isBlank());
    }

    private Optional<LoginRequest> tryParseLogin(String body) {
        try {
            return Optional.of(om.readValue(body, LoginRequest.class));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean matches(HttpServletRequest request) {
        String path = props.getAuthAllowPathPrefix() + "/login";
        return PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, path).matches(request);
    }

    @Override
    public String getName() {
        return "login";
    }
}
