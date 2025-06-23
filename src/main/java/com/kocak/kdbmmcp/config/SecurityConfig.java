package com.kocak.kdbmmcp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable) // CSRF korumasını devre dışı bırak
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/mcp/**").permitAll() // WebSocket endpoint'i için erişim izni
                .requestMatchers("/actuator/**").permitAll() // Actuator endpoint'leri için erişim izni
                .anyRequest().permitAll() // Tüm isteklere izin ver
            );

        return http.build();
    }
}
