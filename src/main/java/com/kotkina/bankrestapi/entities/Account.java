package com.kotkina.bankrestapi.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

import static java.math.RoundingMode.HALF_EVEN;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, columnDefinition = "numeric(38,2) DEFAULT 0 CHECK (initial_deposit >= 0)")
    private BigDecimal initialDeposit;

    @Column(nullable = false, columnDefinition = "numeric(38,2) DEFAULT 0 CHECK (balance >= 0)")
    private BigDecimal balance;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", referencedColumnName = "id")
    @JsonIgnore
    @ToString.Exclude
    private Client client;

    public static Account create(Client client, String deposit) {
        Account account = new Account();
        account.setClient(client);
        BigDecimal amount = new BigDecimal(deposit).setScale(2, HALF_EVEN);
        account.initialDeposit = amount;
        account.balance = amount;

        return account;
    }
}
