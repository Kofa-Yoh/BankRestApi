package com.kotkina.bankrestapi.web.models.responses;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserClientResponse {
    private String login;
    private ClientResponse client;
}
