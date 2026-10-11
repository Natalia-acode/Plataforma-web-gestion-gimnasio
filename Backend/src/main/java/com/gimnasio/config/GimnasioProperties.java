package com.gimnasio.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gimnasio")
public record GimnasioProperties(Jwt jwt, Cors cors) {

    public record Jwt(String secret, long expirationMs) {
    }

    public record Cors(String allowedOrigins) {
    }
}
