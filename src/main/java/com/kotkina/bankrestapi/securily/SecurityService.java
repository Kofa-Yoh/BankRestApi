package com.kotkina.bankrestapi.securily;

import com.kotkina.bankrestapi.entities.User;
import com.kotkina.bankrestapi.exceptions.AuthException;
import com.kotkina.bankrestapi.exceptions.EntityNotFoundException;
import com.kotkina.bankrestapi.repositories.UserRepository;
import com.kotkina.bankrestapi.securily.jwt.JwtUtils;
import com.kotkina.bankrestapi.services.TokenBlacklistService;
import com.kotkina.bankrestapi.web.models.requests.AuthRequest;
import com.kotkina.bankrestapi.web.models.responses.AuthResponse;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityService {

    private final AuthenticationManager authenticationManager;

    private final JwtUtils jwtUtils;

    private final UserRepository userRepository;

    private final TokenBlacklistService tokenBlacklistService;

    public AuthResponse authenticateUser(AuthRequest authRequest) {
        if (authRequest == null) {
            throw new EntityNotFoundException("User not found.");
        }

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                authRequest.getLogin(),
                authRequest.getPassword()
        ));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return AuthResponse.builder()
                .login(userDetails.getUsername())
                .token(jwtUtils.generateJwtToken(userDetails))
                .roles(roles)
                .build();
    }

    public void logout(HttpServletRequest request) {
        String jwtToken = getToken(request);

        if (jwtToken != null && jwtUtils.validate(jwtToken)) {
            addTokenInBlacklist(jwtToken);
        }

        SecurityContextHolder.clearContext();
    }

    public UserDetailsImpl getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            return (UserDetailsImpl) authentication.getPrincipal();
        } else {
            return null;
        }
    }

    public Long getCurrentUserClientId(){
        UserDetailsImpl currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new AuthException("Current user not defined.");
        }

        return currentUser.getClientId();
    }

    public Long getCurrentUserClientIdWithExceptionIfNull() {
        Long clientId = getCurrentUserClientId();
        if (clientId == null) {
            throw new AuthException("Something goes wrong. Try log in again and repeat the request.");
        }

        return clientId;
    }

    private String getToken(HttpServletRequest request) {
        String headerAuth = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }

    private void addTokenInBlacklist(String token) {
        if (token != null) {
            try {
                String login = jwtUtils.getUsernameFromToken(token);
                User user = userRepository.findUserByLogin(login)
                        .orElse(null);
                if (user == null) return;

                LocalDateTime expirationDate = jwtUtils.getExpirationFromToken(token)
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
                Long seconds = Duration.between(LocalDateTime.now(), expirationDate).getSeconds();

                tokenBlacklistService.addTokenInBlacklist(token, LocalDateTime.now(), seconds);

            } catch (ExpiredJwtException e) {
                log.warn(e.getMessage());
            }
        }
    }
}
