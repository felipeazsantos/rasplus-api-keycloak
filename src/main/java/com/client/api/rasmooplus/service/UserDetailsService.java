package com.client.api.rasmooplus.service;

import com.client.api.rasmooplus.dto.UserDetailsDto;
import com.client.api.rasmooplus.dto.oauth.UserRepresentationDto;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface UserDetailsService {

    void sendRecoveryCode(String email);

    boolean recoveryCodeIsValid(String recoveryCode, String email);

    void updatePasswordByRecoveryCode(UserDetailsDto userDetailsDto);

    void createAuthUser(UserRepresentationDto userRepresentationDto);

    void updateAuthUser(UserRepresentationDto userRepresentationDto, String email);
}
