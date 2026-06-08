package com.example.mlbanalysis.auth.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth")
public class AuthProperties {
    private Duration tokenTtl = Duration.ofDays(7);
    private int tokenBytes = 32;

    public Duration getTokenTtl() {
        return tokenTtl;
    }

    public void setTokenTtl(Duration tokenTtl) {
        if (tokenTtl == null || tokenTtl.isNegative() || tokenTtl.isZero()) {
            throw new IllegalArgumentException("auth.token-ttl must be positive");
        }
        this.tokenTtl = tokenTtl;
    }

    public int getTokenBytes() {
        return tokenBytes;
    }

    public void setTokenBytes(int tokenBytes) {
        if (tokenBytes < 16) {
            throw new IllegalArgumentException("auth.token-bytes must be at least 16");
        }
        this.tokenBytes = tokenBytes;
    }
}
