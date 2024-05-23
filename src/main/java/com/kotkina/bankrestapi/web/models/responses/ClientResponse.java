package com.kotkina.bankrestapi.web.models.responses;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ClientResponse {
    private Long id;
    private String lastname;
    private String firstname;
    private String patronymic;
    private String birthdate;
    private String email;
    private String phone;
    private AccountResponse account;
}
