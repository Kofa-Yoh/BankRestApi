package com.kotkina.bankrestapi.securily.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kotkina.bankrestapi.web.models.responses.AuthErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        log.error("Unauthorized error: {}", authException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        AuthErrorResponse body = new AuthErrorResponse();
        body.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        body.setError(authException.getMessage());
        body.setMessage("You do not have permission to complete this request.");
        body.setPath(request.getServletPath());

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
