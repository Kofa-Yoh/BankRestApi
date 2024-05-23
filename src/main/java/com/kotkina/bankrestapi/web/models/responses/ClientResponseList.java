package com.kotkina.bankrestapi.web.models.responses;

import lombok.*;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientResponseList {
    private int elements;
    private int page;
    private int size;
    private int totalPages;
    private String sort;
    private List<ClientResponse> clients;
}
