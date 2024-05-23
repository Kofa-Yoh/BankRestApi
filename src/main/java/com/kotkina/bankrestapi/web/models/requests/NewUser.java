package com.kotkina.bankrestapi.web.models.requests;

import com.kotkina.bankrestapi.validation.NewUserValid;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@NewUserValid
public class NewUser {
    private String login;
    private String password;
    private String lastname;
    private String firstname;
    private String patronymic;
    private LocalDate birthdate;
    private String email;
    private String phone;
    private String deposit;
}
