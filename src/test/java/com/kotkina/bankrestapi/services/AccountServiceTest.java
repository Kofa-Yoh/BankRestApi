package com.kotkina.bankrestapi.services;

import com.kotkina.bankrestapi.entities.Account;
import com.kotkina.bankrestapi.entities.Transaction;
import com.kotkina.bankrestapi.exceptions.TransferMoneyException;
import com.kotkina.bankrestapi.mappers.TransactionMapper;
import com.kotkina.bankrestapi.repositories.AccountRepository;
import com.kotkina.bankrestapi.web.models.responses.TransactionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@EnableAsync
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountChangingService accountChangingService;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private AccountService.LockAccountChanging lockAccountChanging;

    @InjectMocks
    private AccountService accountService;

    private Long accountId1;
    private Long accountId2;
    private Long accountId3;
    private Transaction transaction;
    private TransactionResponse transactionResponse;


    @BeforeEach
    public void setUp() {
        accountId1 = 1L;
        accountId2 = 2L;
        accountId3 = 3L;

        Account account1 = new Account();
        account1.setId(accountId1);
        account1.setBalance(new BigDecimal("1000.00"));

        Account account2 = new Account();
        account2.setId(accountId2);
        account2.setBalance(new BigDecimal("500.00"));

        Account account3 = new Account();
        account3.setId(accountId3);
        account3.setBalance(new BigDecimal("500.00"));

        transaction = new Transaction();
        transaction.setFromAccount(account1.getId());
        transaction.setToAccount(account2.getId());
        transaction.setAmount(new BigDecimal("100.00"));

        transactionResponse = new TransactionResponse();
        transactionResponse.setId(transaction.getId());

        when(lockAccountChanging.obtainLock(accountId1)).thenReturn(new ReentrantLock());
        when(accountRepository.findAccountByClientId(accountId1)).thenReturn(Optional.of(account1));
        when(accountRepository.findAccountByClientId(accountId2)).thenReturn(Optional.of(account2));
        when(accountRepository.findAccountByClientId(accountId3)).thenReturn(Optional.of(account3));
        when(accountChangingService.makeTransfer(anyLong(), anyLong(), any(BigDecimal.class))).thenReturn(transaction);
        when(transactionMapper.transactionToResponse(any(Transaction.class))).thenReturn(transactionResponse);
    }

    @Test
    void testTransferMoney() throws ExecutionException, InterruptedException {
        CompletableFuture<TransactionResponse> future = accountService.transferMoney(accountId1, accountId2, new BigDecimal("100.00"));
        TransactionResponse response = future.get();

        verify(lockAccountChanging, times(1)).obtainLock(accountId1);
        verify(accountRepository, times(1)).findAccountByClientId(accountId1);
        verify(accountRepository, times(1)).findAccountByClientId(accountId2);
        verify(accountChangingService, times(1)).makeTransfer(accountId1, accountId2, new BigDecimal("100.00"));
        verify(transactionMapper, times(1)).transactionToResponse(transaction);
        verify(lockAccountChanging, times(1)).releaseLock(accountId1);
    }

    @Test
    void testDoubleTransferMoneyWithSameFromUserId() {
        Lock lock = spy(new ReentrantLock());
        when(lockAccountChanging.obtainLock(1L)).thenReturn(lock);

        CompletableFuture<TransactionResponse> future1 = accountService.transferMoney(1L, 2L, new BigDecimal("100.00"));
        CompletableFuture<TransactionResponse> future2 = accountService.transferMoney(1L, 3L, new BigDecimal("50.00"));

        CompletableFuture.allOf(future1, future2).join();

        verify(lockAccountChanging, times(2)).obtainLock(1L);
        verify(lock, times(2)).lock();
        verify(lock, times(2)).unlock();
        verify(lockAccountChanging, times(2)).releaseLock(1L);
    }

    @Test
    void testTransferMoneyWithTransferMoneyException() {
        Lock lock = spy(new ReentrantLock());
        when(lockAccountChanging.obtainLock(1L)).thenReturn(lock);
        when(accountChangingService.makeTransfer(anyLong(), anyLong(), any(BigDecimal.class))).thenThrow(new TransferMoneyException("Transaction failed."));

        assertThrows(TransferMoneyException.class, () -> {
            CompletableFuture<TransactionResponse> future = accountService.transferMoney(1L, 2L, new BigDecimal("100.00"));
            future.join();
        });

        verify(lockAccountChanging, times(1)).obtainLock(1L);
        verify(lock, times(1)).lock();
        verify(lock, times(1)).unlock();
        verify(lockAccountChanging, times(1)).releaseLock(1L);
    }

    @Test
    void testDoubleTransferMoneyWithOneTransferMoneyException() {
        Lock lock = spy(new ReentrantLock());
        when(lockAccountChanging.obtainLock(1L)).thenReturn(lock);
        when(accountChangingService.makeTransfer(eq(1L), eq(2L), any(BigDecimal.class))).thenThrow(new TransferMoneyException("Transaction failed."));
        when(accountChangingService.makeTransfer(eq(1L), eq(3L), any(BigDecimal.class))).thenReturn(transaction);

        assertThrows(TransferMoneyException.class, () -> {
            CompletableFuture<TransactionResponse> future1 = accountService.transferMoney(1L, 2L, new BigDecimal("100.00"));
            future1.join();
        });
        CompletableFuture<TransactionResponse> future2 = accountService.transferMoney(1L, 3L, new BigDecimal("100.00"));
        future2.join();

        verify(lockAccountChanging, times(2)).obtainLock(1L);
        verify(lock, times(2)).lock();
        verify(lock, times(2)).unlock();
        verify(lockAccountChanging, times(2)).releaseLock(1L);
    }
}