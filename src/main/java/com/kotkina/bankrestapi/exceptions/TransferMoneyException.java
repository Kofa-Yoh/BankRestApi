package com.kotkina.bankrestapi.exceptions;

public class TransferMoneyException extends RuntimeException {
    public TransferMoneyException(String message) {
        super(message);
    }
}
