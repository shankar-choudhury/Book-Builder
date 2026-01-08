package com.shankar.book_builder.auth.security.ratelimiter.strategy;

import com.shankar.book_builder.auth.security.config.AppSecurityProperties;
import io.github.bucket4j.Bandwidth;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;

@Component
public class RegisterRateLimitStrategy extends BaseLimitingStrategy {
    private final RequestMatcher matcher;

    public RegisterRateLimitStrategy(AppSecurityProperties props) {
        this.matcher = PathPatternRequestMatcher.withDefaults()
                .matcher(HttpMethod.POST, props.getAuthAllowPathPrefix() + "/register");
    }

    @Override
    public String resolveKey(HttpServletRequest request) {
        return "register:" + getIp(request);
    }

    @Override
    public Bandwidth resolveBandwidth(HttpServletRequest request, AppSecurityProperties props) {
        return createBandwidth(props.getRateLimiter().getRegisterToken(), props);
    }

    @Override
    public boolean matches(HttpServletRequest request) {
        return matcher.matches(request);
    }

    @Override
    public String getName() {
        return "register";
    }
}
