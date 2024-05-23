package com.kotkina.bankrestapi.web.models.requests;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class ClientFilter {
    private String lastname;
    private String firstname;
    private String patronymic;
    private LocalDate birthdate;
    private String email;
    private String phone;
    private Integer page;
    private Integer size;
    private String sort;
}
