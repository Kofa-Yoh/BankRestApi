package com.kotkina.bankrestapi.web.models.responses;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class AuthErrorResponse {
    private int status;
    private String error;
    private String message;
    private String path;
}
