package com.kotkina.bankrestapi.services;

import com.kotkina.bankrestapi.repositories.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class AccountScheduledService {
    @Value("${app.multiply.balance.max.level}")
    private float multiplyBalanceMaxLevel;

    private final AccountRepository accountRepository;
    private final AccountChangingService moneyChangingService;

    @Scheduled(fixedRate = 60000)
    public void scheduledUpdateBalances() {
        List<CompletableFuture<Void>> futures = accountRepository.findAccountsForBalanceUpdate(multiplyBalanceMaxLevel)
                .stream()
                .map(account -> moneyChangingService.multiplyBalance(account.getId()))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }
}
