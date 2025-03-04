package com.client.api.rasmooplus.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@Configuration
public class WebSecurityConfig {

    private static final String[] AUTH_SWAGGER_LIST = {
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/v2/api-docs/**",
            "/swagger-resources/**"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(authorize -> {
                    authorize.requestMatchers(HttpMethod.GET, AUTH_SWAGGER_LIST);
                    authorize.requestMatchers(HttpMethod.GET, "/subscription-type");
                    authorize.requestMatchers(HttpMethod.POST, "/user");
                    authorize.requestMatchers(HttpMethod.POST, "/payment/process");
                    authorize.requestMatchers(HttpMethod.POST, "/auth");
                    authorize.requestMatchers( "/auth/recovery-code/*");
                    authorize.anyRequest().authenticated();
                })
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();

    }

}
