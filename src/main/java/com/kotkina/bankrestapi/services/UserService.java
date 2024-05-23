package com.kotkina.bankrestapi.services;

import com.kotkina.bankrestapi.entities.Client;
import com.kotkina.bankrestapi.entities.User;
import com.kotkina.bankrestapi.exceptions.EntityAlreadyExistsException;
import com.kotkina.bankrestapi.exceptions.EntityNotFoundException;
import com.kotkina.bankrestapi.repositories.UserRepository;
import com.kotkina.bankrestapi.web.models.requests.NewUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final ClientService clientService;

    public User getUserClientByLogin(String login) {
        return userRepository.findUserByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("Login not found."));
    }

    @Transactional
    public User createUserWithClient(NewUser newUser) {
        Client client = clientService.save(newUser);

        User user = new User();
        user.setLogin(newUser.getLogin());
        user.setPassword(passwordEncoder.encode(newUser.getPassword()));
        user.setClient(client);

        return userRepository.save(user);
    }

    public void checkLoginNotExists(String login) {
        if (userRepository.existsByLogin(login)) {
            throw new EntityAlreadyExistsException("User with the same login address already exists.");
        }
    }
}
