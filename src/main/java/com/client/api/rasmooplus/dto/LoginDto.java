package com.client.api.rasmooplus.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginDto {

    private String username;

    private String password;

    @NotBlank(message = "atributo obrigatório")
    private String clientId;

    @NotBlank(message = "atributo obrigatório")
    private String clientSecret;

    @NotBlank(message = "atributo obrigatório")
    private String grantType;

    private String refreshToken;

}
