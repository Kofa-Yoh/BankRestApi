package com.kotkina.bankrestapi.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kotkina.bankrestapi.entities.Account;
import com.kotkina.bankrestapi.entities.Transaction;
import com.kotkina.bankrestapi.entities.TransactionType;
import com.kotkina.bankrestapi.exceptions.TransferMoneyException;
import com.kotkina.bankrestapi.repositories.AccountRepository;
import com.kotkina.bankrestapi.repositories.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountChangingService {
    @Value("${app.multiply.balance.increase.level}")
    private float multiplyBalanceIncreaseLevel;

    @Value("${app.multiply.balance.max.level}")
    private float multiplyBalanceMaxLevel;

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public Transaction makeTransfer(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        log.info("Start transaction fromAccountId: {}, toAccountId: {}, amount: {} ...", fromAccountId, toAccountId, amount);
        Transaction transaction = new Transaction();
        transaction.setFromAccount(fromAccountId);
        transaction.setToAccount(toAccountId);
        transaction.setMethod(TransactionType.ACCOUNT2ACCOUNT);
        transaction.setAmount(amount);
        transaction.setSuccess(false);
        Transaction savedTransaction;

        try {
            int isSent = accountRepository.transferMoney(fromAccountId, amount.negate());
            int isReceived = accountRepository.transferMoney(toAccountId, amount);
            if (isSent == 0 || isReceived == 0) {
                throw new TransferMoneyException("Transaction failed. Please repeat the request.");
            }

            transaction.setSuccess(true);
        } catch (Exception ex) {
            throw new TransferMoneyException("Transaction failed. Please repeat the request.");
        } finally {
            log.info("Success status: {}", transaction.getSuccess());
            log.info("End transaction fromAccountId: {}, toAccountId: {}, amount: {} ...", fromAccountId, toAccountId, amount);
            savedTransaction = transactionRepository.save(transaction);
        }

        return savedTransaction;
    }

    @Async("scheduledBalanceTaskExecutor")
    @Transactional
    public CompletableFuture<Void> multiplyBalance(Long accountId) {
        log.info("Multiply balance accountId: {}, multiplyBalanceIncreaseLevel: {}, multiplyBalanceMaxLevel: {}", accountId, multiplyBalanceIncreaseLevel, multiplyBalanceMaxLevel);
        Optional<Account> maybeAccount = accountRepository.findById(accountId);
        if (maybeAccount.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        BigDecimal newBalance = accountRepository.multiplyBalanceToMax(accountId, multiplyBalanceIncreaseLevel, multiplyBalanceMaxLevel);
        if (newBalance == null) {
            return CompletableFuture.completedFuture(null);
        }

        Transaction transaction = new Transaction();
        transaction.setFromAccount(accountId);
        transaction.setToAccount(accountId);
        transaction.setMethod(TransactionType.MULTIPLY_BALANCE);
        transaction.setAmount(newBalance.subtract(maybeAccount.get().getBalance()));
        transaction.setSuccess(true);
        transactionRepository.save(transaction);
        try {
            log.info("Transaction completed: {}", objectMapper.writeValueAsString(transaction));
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
        return CompletableFuture.completedFuture(null);
    }
}
