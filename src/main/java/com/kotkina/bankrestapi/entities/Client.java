package com.kotkina.bankrestapi.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@NamedEntityGraph(name = "client-with-account",
        attributeNodes = {
                @NamedAttributeNode(value = "account")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String lastname;

    private String firstname;

    private String patronymic;

    private LocalDate birthdate;

    private String email;

    private String phone;

    @OneToOne(mappedBy = "client", fetch = FetchType.LAZY)
    @JsonIgnore
    private Account account;

    @OneToOne(mappedBy = "client", fetch = FetchType.LAZY)
    @Transient
    @JsonIgnore
    @ToString.Exclude
    private User user;
}
