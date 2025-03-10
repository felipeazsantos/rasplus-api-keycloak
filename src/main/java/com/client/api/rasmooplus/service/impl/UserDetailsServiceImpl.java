package com.client.api.rasmooplus.service.impl;

import com.client.api.rasmooplus.component.HttpComponent;
import com.client.api.rasmooplus.dto.LoginDto;
import com.client.api.rasmooplus.dto.UserDetailsDto;
import com.client.api.rasmooplus.dto.oauth.UserRepresentationDto;
import com.client.api.rasmooplus.exception.BadRequestException;
import com.client.api.rasmooplus.exception.NotFoudException;
import com.client.api.rasmooplus.integration.MailIntegration;
import com.client.api.rasmooplus.model.redis.UserRecoveryCode;
import com.client.api.rasmooplus.repositoy.redis.UserRecoveryCodeRepository;
import com.client.api.rasmooplus.service.AuthenticationService;
import com.client.api.rasmooplus.service.UserDetailsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Value("${webservices.rasplus.redis.recoverycode.timeout}")
    private String recoveryCodeTimeout;

    @Value("${keycloak.credentials.client-id}")
    private String clientId;

    @Value("${keycloak.credentials.client-secret}")
    private String clientSecret;

    @Value("${keycloak.credentials.authorization-grant-type}")
    private String grantType;

    @Value("${keycloak.auth-server-uri}")
    private String keycloakUri;

    @Autowired
    private UserRecoveryCodeRepository userRecoveryCodeRepository;

    @Autowired
    private MailIntegration mailIntegration;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HttpComponent httpComponent;

    @Override
    public void sendRecoveryCode(String email) {

//        UserRecoveryCode userRecoveryCode;
//        String code = String.format("%04d", new Random().nextInt(10000));
//        var userRecoveryCodeOpt = userRecoveryCodeRepository.findByEmail(email);
//
//        if (userRecoveryCodeOpt.isEmpty()) {
//
//            var user = userDetailsRepository.findByUsername(email);
//            if (user.isEmpty()) {
//                throw new NotFoudException("Usuário não encontrado");
//            }
//
//            userRecoveryCode = new UserRecoveryCode();
//            userRecoveryCode.setEmail(email);
//
//        } else {
//            userRecoveryCode = userRecoveryCodeOpt.get();
//        }
//        userRecoveryCode.setCode(code);
//        userRecoveryCode.setCreationDate(LocalDateTime.now());
//
//        userRecoveryCodeRepository.save(userRecoveryCode);
//        mailIntegration.send(email, "Código de recuperação de conta: "+code, "Código de recuperação de conta");
    }

    @Override
    public boolean recoveryCodeIsValid(String recoveryCode, String email) {

        var userRecoveryCodeOpt = userRecoveryCodeRepository.findByEmail(email);

        if (userRecoveryCodeOpt.isEmpty()) {
            throw new NotFoudException("Usuário não encontrado");
        }

        UserRecoveryCode userRecoveryCode = userRecoveryCodeOpt.get();

        LocalDateTime timeout = userRecoveryCode.getCreationDate().plusMinutes(Long.parseLong(recoveryCodeTimeout));
        LocalDateTime now = LocalDateTime.now();

        return recoveryCode.equals(userRecoveryCode.getCode()) && now.isBefore(timeout);
    }

    @Override
    public void updatePasswordByRecoveryCode(UserDetailsDto userDetailsDto) {

//        if (recoveryCodeIsValid(userDetailsDto.getRecoveryCode(), userDetailsDto.getEmail())) {
//            var userDetails = userDetailsRepository.findByUsername(userDetailsDto.getEmail());
//
//            UserCredentials userCredentials = userDetails.get();
//
//            userCredentials.setPassword(PasswordUtils.encode(userDetailsDto.getPassword()));
//
//            userDetailsRepository.save(userCredentials);
//        }
    }

    @Override
    public void createAuthUser(UserRepresentationDto userRepresentationDto) {
        try {
            HttpHeaders httpHeaders = getHttpHeaders();
            HttpEntity<UserRepresentationDto> request = new HttpEntity<>(userRepresentationDto, httpHeaders);
            httpComponent.restTemplate().postForEntity(
              keycloakUri + "/admin/realms/REALM_RASPLUS_API/users",
              request,
              String.class
            );
        } catch (Exception ex) {
            throw new BadRequestException(ex.getMessage());
        }
    }

    @Override
    public void updateAuthUser(UserRepresentationDto userRepresentationDto, String currentEmail) {
        try {HttpHeaders httpHeaders = getHttpHeaders();
           String userId = getUserAuthId(currentEmail, httpHeaders);
           HttpEntity<UserRepresentationDto> request = new HttpEntity<>(userRepresentationDto, httpHeaders);
           httpComponent.restTemplate().put(
                   keycloakUri + "/admin/realms/REALM_RASPLUS_API/users/{id}",
                   request,
                   userId
           );
        } catch (Exception ex) {
            throw new BadRequestException(ex.getMessage());
        }
    }

    private String getUserAuthId(String currentEmail, HttpHeaders httpHeaders) throws JsonProcessingException {
        HttpEntity<UserRepresentationDto> request = new HttpEntity<>(httpHeaders);
        Map<String, String> uriVariables = new HashMap<>();
        uriVariables.put("email", currentEmail);
        uriVariables.put("exact", "true");
        String responseGetUser = httpComponent.restTemplate().exchange(
                keycloakUri + "/admin/realms/REALM_RASPLUS_API/users?email={email}&exact={exact}",
                HttpMethod.GET,
                request,
                String.class,
                uriVariables
        ).getBody();

        List<Map<String, Object>> users = objectMapper.readValue(responseGetUser, new TypeReference<List<Map<String, Object>>>() {
        });
        if (users.isEmpty()) {
            throw new BadRequestException("Error to update user");
        }
        return users.get(0).get("id").toString();
    }

    private String getAdminCliAccessToken() throws JsonProcessingException {
        LoginDto loginDto = new LoginDto();
        loginDto.setClientId(clientId);
        loginDto.setClientSecret(clientSecret);
        loginDto.setGrantType(grantType);
        Map<String, String> clientCredentialsResponse = objectMapper.readValue(authenticationService.auth(loginDto), Map.class);
        return clientCredentialsResponse.get("access_token");
    }

    private HttpHeaders getHttpHeaders() throws JsonProcessingException {
        String accessToken = getAdminCliAccessToken();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setBearerAuth(accessToken);
        return httpHeaders;
    }

}
