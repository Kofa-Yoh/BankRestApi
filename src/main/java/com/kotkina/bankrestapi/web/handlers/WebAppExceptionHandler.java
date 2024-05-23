package com.kotkina.bankrestapi.web.handlers;

import com.kotkina.bankrestapi.exceptions.EntityAlreadyExistsException;
import com.kotkina.bankrestapi.exceptions.EntityNotFoundException;
import com.kotkina.bankrestapi.exceptions.TransferMoneyException;
import com.kotkina.bankrestapi.web.models.responses.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class WebAppExceptionHandler {

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, WebRequest webRequest) {
        BindingResult result = ex.getBindingResult();
        String allErrors = result.getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(" "));

        logging(webRequest, ex);
        return buildResponse(HttpStatus.BAD_REQUEST, allErrors, webRequest);
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, ConstraintViolationException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex, WebRequest webRequest) {
        logging(webRequest, ex);
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad request. Contact administrator.", webRequest);
    }

    @ExceptionHandler({EntityAlreadyExistsException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(Exception ex, WebRequest webRequest) {
        logging(webRequest, ex);
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), webRequest);
    }

    @ExceptionHandler(TransferMoneyException.class)
    public ResponseEntity<ErrorResponse> handleTransferMoneyException(TransferMoneyException ex, WebRequest webRequest) {
        logging(webRequest, ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), webRequest);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException ex, WebRequest webRequest) {
        logging(webRequest, ex);
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), webRequest);
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatusCode httpStatus, String message, WebRequest webRequest) {
        return ResponseEntity.status(httpStatus)
                .body(new ErrorResponse(message, webRequest.getDescription(false)));
    }

    private void logging(WebRequest webRequest, Exception ex) {
        log.error("Exception message - " + ex.getMessage());
        log.error("Request description - " + webRequest.getDescription(true));
        webRequest.getParameterMap().entrySet()
                .forEach(entry -> log.error("Parameter `{}` = {}", entry.getKey(), entry.getValue()));
    }
}
