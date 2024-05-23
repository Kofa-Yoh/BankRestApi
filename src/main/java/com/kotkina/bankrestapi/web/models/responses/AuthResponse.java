package com.kotkina.bankrestapi.web.models.responses;

import lombok.*;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String login;
    private String token;
    private List<String> roles;
}
