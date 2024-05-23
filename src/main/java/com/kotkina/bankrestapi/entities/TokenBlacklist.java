package com.kotkina.bankrestapi.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class TokenBlacklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String token;
    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime createDateTime;
    private Long expiration;
}
