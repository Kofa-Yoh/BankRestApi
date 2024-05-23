package com.kotkina.bankrestapi.repositories;

import com.kotkina.bankrestapi.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findAccountByClientId(Long clientId);

    @Query("FROM Account a WHERE a.balance < a.initialDeposit * :maxLevel")
    List<Account> findAccountsForBalanceUpdate(@Param("maxLevel") Float maxLevel);

    @Transactional
    @Query(value = "UPDATE account a SET balance = CASE " +
            "WHEN (a.balance * :increaseLevel < a.initial_deposit * :maxLevel) " +
            "THEN (a.balance * :increaseLevel) ELSE a.initial_deposit * :maxLevel END " +
            "WHERE a.id = :id AND a.balance < a.initial_deposit * :maxLevel " +
            "RETURNING a.balance", nativeQuery = true)
    BigDecimal multiplyBalanceToMax(@Param("id") Long id,
                             @Param("increaseLevel") Float increaseLevel,
                             @Param("maxLevel") Float maxLevel);

    @Transactional
    @Modifying
    @Query("UPDATE Account a SET a.balance = a.balance + :amount WHERE a.id = :id AND a.balance + :amount >= 0")
    int transferMoney(@Param("id") Long id, @Param("amount") BigDecimal amount);
}
