package com.kotkina.bankrestapi.web.controllers;

import com.kotkina.bankrestapi.securily.SecurityService;
import com.kotkina.bankrestapi.services.RegisterService;
import com.kotkina.bankrestapi.web.models.requests.AuthRequest;
import com.kotkina.bankrestapi.web.models.requests.NewUser;
import com.kotkina.bankrestapi.web.models.responses.AuthErrorResponse;
import com.kotkina.bankrestapi.web.models.responses.AuthResponse;
import com.kotkina.bankrestapi.web.models.responses.ErrorResponse;
import com.kotkina.bankrestapi.web.models.responses.UserClientResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@Validated
@RequiredArgsConstructor
@Tag(name = "Registration and Authentication")
public class UserController {

    private final RegisterService registerService;
    private final SecurityService securityService;

    @Operation(summary = "New user",
            description = "Create new user, client and account entities")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "User is created",
                    content = {@Content(schema = @Schema(implementation = UserClientResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400",
                    description = "Error creating user",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class), mediaType = "application/json")})
    })
    @PostMapping("/new")
    public ResponseEntity<UserClientResponse> register(@RequestBody @Valid NewUser newUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registerService.createUserWithClient(newUser));
    }

    @Operation(summary = "Log in")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Get token",
                    content = {@Content(schema = @Schema(implementation = AuthResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "401",
                    description = "Bad credentials",
                    content = {@Content(schema = @Schema(implementation = AuthErrorResponse.class), mediaType = "application/json")})
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> logIn(@RequestBody @Valid AuthRequest authRequest) {
        return ResponseEntity.ok(securityService.authenticateUser(authRequest));
    }

    @Operation(summary = "Log out")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Delete token",
                    content = {@Content(schema = @Schema(implementation = UserClientResponse.class), mediaType = "application/json")})
    })
    @PostMapping("/user_logout")
    public ResponseEntity<String> logOut(@AuthenticationPrincipal UserDetails userDetails,
                                         HttpServletRequest request) {
        securityService.logout(request);

        return ResponseEntity.ok("Your token is unavailable.");
    }
}
