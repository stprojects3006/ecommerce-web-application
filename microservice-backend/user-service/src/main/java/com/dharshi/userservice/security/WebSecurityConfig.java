package com.dharshi.userservice.security;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class WebSecurityConfig {

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Local development origins
        configuration.addAllowedOrigin("http://localhost");
        configuration.addAllowedOrigin("http://localhost:80");
        configuration.addAllowedOrigin("http://localhost:8080");
        configuration.addAllowedOrigin("http://localhost:5173");
        configuration.addAllowedOrigin("https://localhost");
        configuration.addAllowedOrigin("https://localhost:443");
        configuration.addAllowedOrigin("https://localhost:8080");
        configuration.addAllowedOrigin("https://localhost:5173");
        
        // Production origins (HTTP and HTTPS)
        configuration.addAllowedOrigin("http://18.217.148.69");
        configuration.addAllowedOrigin("http://18.217.148.69:80");
        configuration.addAllowedOrigin("http://18.217.148.69:8080");
        configuration.addAllowedOrigin("http://18.217.148.69:5173");
        configuration.addAllowedOrigin("https://18.217.148.69");
        configuration.addAllowedOrigin("https://18.217.148.69:443");
        configuration.addAllowedOrigin("https://18.217.148.69:8080");
        configuration.addAllowedOrigin("https://18.217.148.69:5173");
        
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                               auth.anyRequest().permitAll()
                );

        return http.build();
    }
}
