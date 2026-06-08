package com.example.mlbanalysis.common.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(MlbApiProperties.class)
public class MlbApiClientConfig {

    @Bean
    public ClientHttpRequestFactory mlbClientHttpRequestFactory(MlbApiProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.getConnectTimeout());
        factory.setReadTimeout(properties.getReadTimeout());
        return factory;
    }

    @Bean
    @Qualifier("mlbRestClient")
    public RestClient mlbRestClient(
            MlbApiProperties properties,
            ClientHttpRequestFactory mlbClientHttpRequestFactory
    ) {
        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(mlbClientHttpRequestFactory)
                .build();
    }
}
