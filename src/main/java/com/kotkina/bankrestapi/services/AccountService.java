package com.kotkina.bankrestapi.services;

import com.kotkina.bankrestapi.entities.Account;
import com.kotkina.bankrestapi.entities.Transaction;
import com.kotkina.bankrestapi.exceptions.EntityNotFoundException;
import com.kotkina.bankrestapi.mappers.TransactionMapper;
import com.kotkina.bankrestapi.repositories.AccountRepository;
import com.kotkina.bankrestapi.web.models.responses.TransactionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountChangingService accountChangingService;
    private final TransactionMapper transactionMapper;
    private final LockAccountChanging lockAccountChanging;

    @Async("transferMoneyTaskExecutor")
    public CompletableFuture<TransactionResponse> transferMoney(Long fromClientId, Long toClientId, BigDecimal amount) {
        Lock lock = lockAccountChanging.obtainLock(fromClientId);
        lock.lock();
        try {
            Account fromAccount = accountRepository.findAccountByClientId(fromClientId)
                    .orElseThrow(() -> new EntityNotFoundException("Sender account not found."));
            Account toAccount = accountRepository.findAccountByClientId(toClientId)
                    .orElseThrow(() -> new EntityNotFoundException("Recipient account not found."));

            if (fromAccount.getBalance().compareTo(amount) < 0) {
                throw new IllegalArgumentException("Insufficient balance on your account.");
            }

            Transaction transaction = accountChangingService.makeTransfer(fromAccount.getId(), toAccount.getId(), amount);

            return CompletableFuture.completedFuture(transactionMapper.transactionToResponse(transaction));
        } finally {
            lock.unlock();
            lockAccountChanging.releaseLock(fromClientId);
        }
    }

    public static class LockAccountChanging {
        private final ConcurrentHashMap<Long, Lock> lockMap = new ConcurrentHashMap<>();

        public Lock obtainLock(Long key) {
            return lockMap.computeIfAbsent(key, k -> new ReentrantLock());
        }

        public void releaseLock(Long key) {
            lockMap.remove(key);
        }
    }
}
