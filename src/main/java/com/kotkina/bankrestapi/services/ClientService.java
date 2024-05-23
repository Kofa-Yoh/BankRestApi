package com.kotkina.bankrestapi.services;

import com.kotkina.bankrestapi.entities.Account;
import com.kotkina.bankrestapi.entities.Client;
import com.kotkina.bankrestapi.exceptions.EntityAlreadyExistsException;
import com.kotkina.bankrestapi.exceptions.EntityNotFoundException;
import com.kotkina.bankrestapi.mappers.ClientMapper;
import com.kotkina.bankrestapi.repositories.AccountRepository;
import com.kotkina.bankrestapi.repositories.ClientRepository;
import com.kotkina.bankrestapi.repositories.ClientSpecification;
import com.kotkina.bankrestapi.web.models.requests.ClientFilter;
import com.kotkina.bankrestapi.web.models.requests.ContactsRequest;
import com.kotkina.bankrestapi.web.models.requests.NewUser;
import com.kotkina.bankrestapi.web.models.responses.ClientResponse;
import com.kotkina.bankrestapi.web.models.responses.ClientResponseList;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final AccountRepository accountRepository;
    private final ClientMapper clientMapper;

    private static final int CLIENT_LIST_PAGE = 0;
    private static final int CLIENT_LIST_SIZE = 3;
    private static final Sort CLIENT_LIST_SORT = Sort.unsorted();

    @Transactional
    public Client save(NewUser newUser) {
        Client client = clientRepository.save(clientMapper.newUserToClient(newUser));
        accountRepository.save(Account.create(client, newUser.getDeposit()));

        return getClientWithExceptionIfNotFound(client.getId());
    }

    public ClientResponse findById(Long id) {
        return clientMapper.clientToResponse(getClientWithExceptionIfNotFound(id));
    }

    public ClientResponseList filterBy(ClientFilter filter) {
        int page = filter.getPage() == null ? CLIENT_LIST_PAGE : filter.getPage();
        int size = filter.getSize() == null ? CLIENT_LIST_SIZE : filter.getSize();
        Sort sort = filter.getSort() == null ? CLIENT_LIST_SORT :
                switch (filter.getSort().toLowerCase()) {
                    case ("birthdateasc") -> Sort.by(Sort.Direction.ASC, "birthdate");
                    case ("birthdatedesc") -> Sort.by(Sort.Direction.DESC, "birthdate");
                    case ("fio") -> Sort.by(Sort.Direction.ASC, "lastname", "firstname", "patronymic");
                    default -> CLIENT_LIST_SORT;
                };

        Page<Client> clientPage = clientRepository.findAll(
                ClientSpecification.withFilter(filter), PageRequest.of(page, size, sort));
        return ClientResponseList.builder()
                .clients(clientMapper.clientListToResponse(clientPage.getContent()))
                .elements(clientPage.getNumberOfElements())
                .page(page)
                .size(size)
                .totalPages(clientPage.getTotalPages())
                .sort(sort.toString())
                .build();
    }

    public ClientResponse changeById(Long id, ContactsRequest contacts) {
        Client client = getClientWithExceptionIfNotFound(id);
        boolean isChanged = false;

        String email = contacts.getEmail();
        if (isNotEmpty(email) && !Objects.equals(email, client.getEmail())) {
            checkEmailNotExists(email);
            client.setEmail(email);
            isChanged = true;
        }

        String phone = contacts.getPhone();
        if (isNotEmpty(phone) && !Objects.equals(phone, client.getPhone())) {
            checkPhoneNotExists(phone);
            client.setPhone(phone);
            isChanged = true;
        }

        if (isChanged) {
            return clientMapper.clientToResponse(clientRepository.save(client));
        }

        return clientMapper.clientToResponse(client);
    }

    public ClientResponse clearContact(Long id, String field) {
        Client client = getClientWithExceptionIfNotFound(id);

        switch (field.toLowerCase()) {
            case "email" -> {
                if (client.getPhone() == null) {
                    throw new IllegalArgumentException("Client must have at least one contact.");
                }

                client.setEmail(null);
            }
            case "phone" -> {
                if (client.getEmail() == null) {
                    throw new IllegalArgumentException("Client must have at least one contact.");
                }

                client.setPhone(null);
            }
            default -> throw new IllegalArgumentException("Unknown contact type.");
        }

        return clientMapper.clientToResponse(clientRepository.save(client));
    }

    private Client getClientWithExceptionIfNotFound(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Client not found."));
    }

    public void checkEmailNotExists(String email) {
        if (email != null && clientRepository.existsByEmailIgnoreCase(email)) {
            throw new EntityAlreadyExistsException("You cannot use the specified email.");
        }
    }

    public void checkPhoneNotExists(String phone) {
        if (phone != null && clientRepository.existsByPhoneIgnoreCase(phone)) {
            throw new EntityAlreadyExistsException("You cannot use the specified phone number.");
        }
    }

    private boolean isNotEmpty(String string) {
        return !(string == null || string.trim().isEmpty());
    }
}
