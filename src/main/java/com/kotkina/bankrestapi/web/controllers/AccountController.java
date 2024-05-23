package com.kotkina.bankrestapi.web.controllers;

import com.kotkina.bankrestapi.entities.User;
import com.kotkina.bankrestapi.securily.SecurityService;
import com.kotkina.bankrestapi.services.AccountService;
import com.kotkina.bankrestapi.services.UserService;
import com.kotkina.bankrestapi.web.models.responses.AuthErrorResponse;
import com.kotkina.bankrestapi.web.models.responses.ErrorResponse;
import com.kotkina.bankrestapi.web.models.responses.TransactionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Money Transfer")
public class AccountController {

    private final AccountService accountService;
    private final UserService userService;
    private final SecurityService securityService;

    @Operation(summary = "Transfer money from current user")
    @SecurityRequirement(name = "Bearer Token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Transaction data",
                    content = {@Content(schema = @Schema(implementation = TransactionResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400",
                    description = "Bad request",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "401",
                    description = "Need to refresh authentication token",
                    content = {@Content(schema = @Schema(implementation = AuthErrorResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "404",
                    description = "Entity not found",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "500",
                    description = "Transaction failed",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class), mediaType = "application/json")})
    })
    @PostMapping("/transfer")
    public CompletableFuture<ResponseEntity<TransactionResponse>> transferMoney(@RequestParam("to") @NotBlank String toUserLogin,
                                                                                @RequestParam @Min(0) BigDecimal amount) {
        Long fromClientId = securityService.getCurrentUserClientIdWithExceptionIfNull();
        User toUser = userService.getUserClientByLogin(toUserLogin);

        CompletableFuture<TransactionResponse> transactionFuture = accountService.transferMoney(fromClientId, toUser.getClient().getId(), amount);

        return transactionFuture.thenApply(ResponseEntity::ok);
    }
}
