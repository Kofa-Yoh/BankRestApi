package com.kotkina.bankrestapi.services;

import com.kotkina.bankrestapi.entities.User;
import com.kotkina.bankrestapi.web.models.requests.NewUser;
import com.kotkina.bankrestapi.web.models.responses.UserClientResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterService {
    private final UserService userService;
    private final ClientService clientService;

    public UserClientResponse createUserWithClient(NewUser newUser) {
        userService.checkLoginNotExists(newUser.getLogin());
        clientService.checkPhoneNotExists(newUser.getPhone());
        clientService.checkEmailNotExists(newUser.getEmail());

        User user = userService.createUserWithClient(newUser);

        return new UserClientResponse(user.getLogin(), clientService.findById(user.getClient().getId()));
    }
}
