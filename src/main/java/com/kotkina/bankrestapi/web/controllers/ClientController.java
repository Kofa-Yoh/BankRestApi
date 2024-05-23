package com.kotkina.bankrestapi.web.controllers;

import com.kotkina.bankrestapi.securily.SecurityService;
import com.kotkina.bankrestapi.services.ClientService;
import com.kotkina.bankrestapi.web.models.requests.ClientFilter;
import com.kotkina.bankrestapi.web.models.requests.ContactsRequest;
import com.kotkina.bankrestapi.web.models.responses.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/client")
@Validated
@RequiredArgsConstructor
@Tag(name = "Clients")
public class ClientController {

    private final ClientService clientService;

    private final SecurityService securityService;

    @Operation(summary = "See current user data")
    @SecurityRequirement(name = "Bearer Token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Current user data",
                    content = {@Content(schema = @Schema(implementation = ClientResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "401",
                    description = "Need to refresh authentication token",
                    content = {@Content(schema = @Schema(implementation = AuthErrorResponse.class), mediaType = "application/json")})
    })
    @GetMapping
    public ResponseEntity<ClientResponse> find() {
        Long clientId = securityService.getCurrentUserClientIdWithExceptionIfNull();
        return ResponseEntity.ok(clientService.findById(clientId));
    }

    @Operation(summary = "Find users by filter")
    @SecurityRequirement(name = "Bearer Token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Users data",
                    content = {@Content(schema = @Schema(implementation = ClientResponseList.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400",
                    description = "Bad request",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "401",
                    description = "Need to refresh authentication token",
                    content = {@Content(schema = @Schema(implementation = AuthErrorResponse.class), mediaType = "application/json")})
    })
    @GetMapping({"/filter"})
    public ResponseEntity<ClientResponseList> filterBy(ClientFilter filter) {
        return ResponseEntity.ok(clientService.filterBy(filter));
    }

    @Operation(summary = "Change email and phone")
    @SecurityRequirement(name = "Bearer Token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Changed user data",
                    content = {@Content(schema = @Schema(implementation = ClientResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400",
                    description = "Bad request",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "401",
                    description = "Need to refresh authentication token",
                    content = {@Content(schema = @Schema(implementation = AuthErrorResponse.class), mediaType = "application/json")})
    })
    @PutMapping("/contacts")
    public ResponseEntity<ClientResponse> changeContacts(@RequestBody ContactsRequest contacts) {
        Long clientId = securityService.getCurrentUserClientIdWithExceptionIfNull();
        return ResponseEntity.ok(clientService.changeById(clientId, contacts));
    }

    @Operation(summary = "Delete email or phone")
    @SecurityRequirement(name = "Bearer Token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Changed user data",
                    content = {@Content(schema = @Schema(implementation = ClientResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "400",
                    description = "Bad request",
                    content = {@Content(schema = @Schema(implementation = ErrorResponse.class), mediaType = "application/json")}),
            @ApiResponse(responseCode = "401",
                    description = "Need to refresh authentication token",
                    content = {@Content(schema = @Schema(implementation = AuthErrorResponse.class), mediaType = "application/json")})
    })
    @DeleteMapping("/contacts")
    public ResponseEntity<ClientResponse> clearContact(@RequestParam @NotBlank String type) {
        Long clientId = securityService.getCurrentUserClientIdWithExceptionIfNull();
        return ResponseEntity.ok(clientService.clearContact(clientId, type));
    }
}
