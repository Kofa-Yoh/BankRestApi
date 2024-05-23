package com.kotkina.bankrestapi.web.models.requests;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ContactsRequest {
    private String email;
    private String phone;
}
