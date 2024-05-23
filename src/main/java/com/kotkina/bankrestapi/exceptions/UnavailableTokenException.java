package com.kotkina.bankrestapi.exceptions;

public class UnavailableTokenException extends RuntimeException {
    public UnavailableTokenException(String message) {
        super(message);
    }
}
