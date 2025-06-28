package com.dharshi.apigateway;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class HealthController {

    @GetMapping("/health")
    public Mono<String> health() {
        return Mono.just("API Gateway is healthy");
    }

    @GetMapping("/")
    public Mono<String> root() {
        return Mono.just("API Gateway is running");
    }
} 