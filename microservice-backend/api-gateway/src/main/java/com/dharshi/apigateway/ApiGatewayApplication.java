package com.dharshi.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

	@Bean
	public CorsFilter corsFilter() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);
		
		// Local development origins
		config.addAllowedOrigin("http://localhost");
		config.addAllowedOrigin("http://localhost:80");
		config.addAllowedOrigin("http://localhost:8080");
		config.addAllowedOrigin("http://localhost:5173");
		
		// Production origins (HTTP and HTTPS)
		config.addAllowedOrigin("http://18.217.148.69");
		config.addAllowedOrigin("http://18.217.148.69:80");
		config.addAllowedOrigin("http://18.217.148.69:8080");
		config.addAllowedOrigin("http://18.217.148.69:5173");
		config.addAllowedOrigin("https://18.217.148.69");
		config.addAllowedOrigin("https://18.217.148.69:443");
		config.addAllowedOrigin("https://18.217.148.69:8080");
		config.addAllowedOrigin("https://18.217.148.69:5173");
		
		config.addAllowedHeader("*");
		config.addAllowedMethod("*");
		config.setMaxAge(3600L);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return new CorsFilter(source);
	}

}
