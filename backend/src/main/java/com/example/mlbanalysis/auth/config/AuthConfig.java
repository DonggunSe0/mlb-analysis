package com.example.mlbanalysis.auth.config;

import java.time.Clock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("login")
@EnableConfigurationProperties(AuthProperties.class)
public class AuthConfig {
    @Bean
    public Clock authClock() {
        return Clock.systemUTC();
    }
}
