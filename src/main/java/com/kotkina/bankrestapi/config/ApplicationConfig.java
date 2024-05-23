package com.kotkina.bankrestapi.config;

import com.kotkina.bankrestapi.services.AccountService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {
    @Bean
    public AccountService.LockAccountChanging lockAccountChanging() {
        return new AccountService.LockAccountChanging();
    }
}
