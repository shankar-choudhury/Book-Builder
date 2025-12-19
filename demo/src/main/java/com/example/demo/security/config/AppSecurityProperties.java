package com.example.demo.security.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
}