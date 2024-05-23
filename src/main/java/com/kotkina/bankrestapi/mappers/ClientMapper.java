package com.kotkina.bankrestapi.mappers;

import com.kotkina.bankrestapi.entities.Client;
import com.kotkina.bankrestapi.web.models.requests.NewUser;
import com.kotkina.bankrestapi.web.models.responses.ClientResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {AccountMapper.class, DateMapper.class})
public interface ClientMapper {
    Client newUserToClient(NewUser newUser);

    ClientResponse clientToResponse(Client client);

    default List<ClientResponse> clientListToResponse(List<Client> clients) {
        return clients == null ? null : clients.stream()
                .map(this::clientToResponse)
                .toList();
    }
}
