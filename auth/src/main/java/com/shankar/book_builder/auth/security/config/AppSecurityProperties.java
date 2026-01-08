package com.shankar.book_builder.auth.security.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Setter
@Validated
@ConfigurationProperties(prefix = "app.security")
@Getter
public class AppSecurityProperties {
    @NotBlank
    private String authAllowPathPrefix;
    @NotBlank
    private String frontendOrigin;
    @Valid
    private final Secrets secrets = new Secrets();
    @Valid
    private final Cookies cookies = new Cookies();
    @Valid
    private final RateLimiter rateLimiter = new RateLimiter();

    @Setter
    @Getter
    public static class Secrets {
        @NotBlank
        private String pepper;
        @NotBlank
        private String jwt;
        @NotBlank
        private String csrf;
    }

    @Getter
    @Setter
    public static class RateLimiter {
        @NotNull
        private Integer registerToken;
        @NotNull
        private Integer loginToken;
        @NotNull
        private Integer refreshToken;
        @NotNull
        private Integer windowSeconds;
        @NotNull
        private Integer maxBuckets;
    }

    @Setter
    @Getter
    public static class Cookies {
        private boolean secure = true;
        private String sameSite = "Lax";
        private String path = "/";
    }
}
