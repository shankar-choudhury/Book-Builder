package com.shankar.book_builder.auth.security.ratelimiter.strategy;

import com.shankar.book_builder.auth.security.config.AppSecurityProperties;
import io.github.bucket4j.Bandwidth;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

import static com.shankar.book_builder.auth.security.CookieUtils.TokenType.REFRESH;
import static com.shankar.book_builder.auth.security.CookieUtils.extract;

@Component
@RequiredArgsConstructor
public class RefreshRateLimitStrategy extends BaseLimitingStrategy {
    private final AppSecurityProperties props;

    @Override
    public String resolveKey(HttpServletRequest request) {
        return extract(request, REFRESH)
                .map(raw -> "refresh:" + getIp(request) + ":" + sha256Hex(raw))
                .orElse("refresh:no-refresh-cookie");
    }

    @Override
    public Bandwidth resolveBandwidth(HttpServletRequest request, AppSecurityProperties props) {
        int tokens = extract(request, REFRESH)
                .map(raw -> props.getRateLimiter().getRefreshToken())
                .orElse(1);

        return createBandwidth(tokens, props);
    }

    private String sha256Hex(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            return Integer.toHexString(raw.hashCode());
        }
    }

    @Override
    public boolean matches(HttpServletRequest request) {
        String path = props.getAuthAllowPathPrefix() + "/refresh";
        return PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, path).matches(request);
    }

    @Override
    public String getName() {
        return "refresh";
    }
}
