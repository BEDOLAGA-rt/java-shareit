package ru.practicum.shareit.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {

    @Value("${shareit-server.url}")
    private String serverUrl;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        // Используем SimpleClientHttpRequestFactory вместо HttpComponentsClientHttpRequestFactory
        return builder
                .requestFactory(SimpleClientHttpRequestFactory.class)
                .setConnectTimeout(Duration.ofSeconds(5))
                .build();
    }
}