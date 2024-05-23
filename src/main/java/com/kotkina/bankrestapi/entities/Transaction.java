package com.kotkina.bankrestapi.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(nullable = false)
    private Long fromAccount;
    @Column(nullable = false)
    private Long toAccount;
    @Enumerated(EnumType.STRING)
    private TransactionType method;
    @Column(nullable = false)
    private BigDecimal amount;
    @Column(nullable = false)
    private Boolean success;
    @CreationTimestamp
    @JsonIgnore
    private Instant created;
    @UpdateTimestamp
    @JsonIgnore
    private Instant updated;
}
