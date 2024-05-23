package com.kotkina.bankrestapi.services;

import com.kotkina.bankrestapi.AbstractPostgreSQLTest;
import com.kotkina.bankrestapi.entities.Account;
import com.kotkina.bankrestapi.entities.Transaction;
import com.kotkina.bankrestapi.exceptions.TransferMoneyException;
import com.kotkina.bankrestapi.repositories.AccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import static java.math.RoundingMode.HALF_EVEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class AccountChangingServiceTest extends AbstractPostgreSQLTest {

    @Value("${app.multiply.balance.increase.level}")
    private float multiplyBalanceIncreaseLevel;

    @Value("${app.multiply.balance.max.level}")
    private float multiplyBalanceMaxLevel;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountChangingService accountChangingService;

    private Account account1;
    private Account account2;
    private Account account3;

    @BeforeEach
    void beforeEach() {
        Account newAccount1 = new Account();
        newAccount1.setBalance(new BigDecimal("1000"));
        newAccount1.setInitialDeposit(new BigDecimal("1000"));
        account1 = accountRepository.save(newAccount1);

        Account newAccount2 = new Account();
        newAccount2.setBalance(new BigDecimal("100"));
        newAccount2.setInitialDeposit(new BigDecimal("10"));
        account2 = accountRepository.save(newAccount2);

        Account newAccount3 = new Account();
        newAccount3.setBalance(new BigDecimal("1000"));
        newAccount3.setInitialDeposit(new BigDecimal("500"));
        account3 = accountRepository.save(newAccount3);
    }

    @AfterEach
    void delete() {
        accountRepository.deleteAll();
    }

    @Test
    void makeSuccessfulTransfer() {
        BigDecimal amount = new BigDecimal("1000");

        Transaction transaction = accountChangingService.makeTransfer(account1.getId(), account2.getId(), amount);

        assertEquals(account1.getId(), transaction.getFromAccount());
        assertEquals(account2.getId(), transaction.getToAccount());
        assertEquals(0, amount.compareTo(transaction.getAmount()));
        assertTrue(transaction.getSuccess());

        assertThat(accountRepository.findById(account1.getId()))
                .isPresent()
                .get()
                .extracting("balance")
                .isEqualTo(new BigDecimal("0.00"));

        assertThat(accountRepository.findById(account2.getId()))
                .isPresent()
                .get()
                .extracting("balance")
                .isEqualTo(new BigDecimal("1100.00"));
    }

    @Test
    void makeFailTransfer_thenThrowException() {
        BigDecimal amount = new BigDecimal("2000");

        assertThrows(TransferMoneyException.class, () ->
                accountChangingService.makeTransfer(account1.getId(), account2.getId(), amount));

        assertThat(accountRepository.findById(account1.getId()))
                .isPresent()
                .get()
                .extracting("balance")
                .isEqualTo(new BigDecimal("1000.00"));

        assertThat(accountRepository.findById(account2.getId()))
                .isPresent()
                .get()
                .extracting("balance")
                .isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    void multiplyBalanceLessThanMaxLevel_thenMultiplyOnIncreaseLevel() {
        BigDecimal beginBalance = account1.getBalance();
        CompletableFuture<Void> future = accountChangingService.multiplyBalance(account1.getId());

        assertThat(future).succeedsWithin(Duration.ofMillis(100));
        assertThat(accountRepository.findById(account1.getId()))
                .isPresent()
                .get()
                .extracting("balance")
                .isEqualTo(beginBalance.multiply(new BigDecimal(multiplyBalanceIncreaseLevel)).setScale(2, HALF_EVEN));
    }

    @Test
    void balanceAlreadyGreaterThanMaxLevel_thenNotMultiply() {
        BigDecimal beginBalance = account2.getBalance();
        CompletableFuture<Void> future = accountChangingService.multiplyBalance(account2.getId());

        assertThat(future).succeedsWithin(Duration.ofMillis(100));
        assertThat(accountRepository.findById(account2.getId()))
                .isPresent()
                .get()
                .extracting("balance")
                .isEqualTo(beginBalance.setScale(2, HALF_EVEN));
    }

    @Test
    void multiplyBalanceGreaterThanMaxLevel_thenIncreaseToMaxLevel() {
        BigDecimal beginInitialDeposit = account3.getInitialDeposit();
        CompletableFuture<Void> future = accountChangingService.multiplyBalance(account3.getId());

        assertThat(future).succeedsWithin(Duration.ofMillis(100));
        assertThat(accountRepository.findById(account3.getId()))
                .isPresent()
                .get()
                .extracting("balance")
                .isEqualTo(beginInitialDeposit.multiply(new BigDecimal(multiplyBalanceMaxLevel)).setScale(2, HALF_EVEN));
    }
}