package com.kotkina.bankrestapi.repositories;

import com.kotkina.bankrestapi.entities.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {
    TokenBlacklist findTokenBlacklistByToken(String token);
}
