package com.client.api.rasmooplus.service.impl;

import com.client.api.rasmooplus.component.HttpComponent;
import com.client.api.rasmooplus.dto.KeycloakOAuthDto;
import com.client.api.rasmooplus.exception.BadRequestException;
import com.client.api.rasmooplus.dto.LoginDto;
import com.client.api.rasmooplus.dto.TokenDto;
import com.client.api.rasmooplus.service.AuthenticationService;
import com.client.api.rasmooplus.service.UserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String PASSWORD = "password";

    @Value("${keycloak.auth-server-uri}")
    private String keycloakUri;


    @Autowired
    private HttpComponent httpComponent;

    @Override
    public String auth(LoginDto dto) {
        try {
            MultiValueMap<String, String> keycloakOAuth = KeycloakOAuthDto.builder()
                    .clientId(dto.getClientId())
                    .clientSecret(dto.getClientSecret())
                    .grantType(dto.getGrantType())
                    .username(dto.getUsername())
                    .password(dto.getPassword())
                    .refreshToken(dto.getRefreshToken())
                    .build();

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(keycloakOAuth, httpComponent.httpHeaders());
            ResponseEntity<String> response = httpComponent.restTemplate().postForEntity(
                    keycloakUri + "/realms/REALM_RASPLUS_API/protocol/openid-connect/token", request, String.class
            );
            return response.getBody();
        } catch (Exception e) {
            throw new BadRequestException("Erro ao formatar token - "+e.getMessage());
        }
    }

}
