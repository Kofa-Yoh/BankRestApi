package com.kotkina.bankrestapi.web.controllers;

import com.kotkina.bankrestapi.AbstractPostgreSQLTest;
import com.kotkina.bankrestapi.entities.Account;
import com.kotkina.bankrestapi.entities.Client;
import com.kotkina.bankrestapi.entities.Transaction;
import com.kotkina.bankrestapi.entities.User;
import com.kotkina.bankrestapi.repositories.AccountRepository;
import com.kotkina.bankrestapi.repositories.ClientRepository;
import com.kotkina.bankrestapi.repositories.TransactionRepository;
import com.kotkina.bankrestapi.repositories.UserRepository;
import com.kotkina.bankrestapi.securily.SecurityService;
import net.javacrumbs.jsonunit.JsonAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import static java.math.RoundingMode.HALF_EVEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AccountControllerTest extends AbstractPostgreSQLTest {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @MockBean
    private SecurityService securityService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private String user1Login = "user1";
    private String user2Login = "user2";
    private Long user1Id;
    private Long account1Id;
    private Long account2Id;

    @BeforeEach
    void beforeEach() {
        Client client1 = clientRepository.save(new Client());

        Account newAccount1 = new Account();
        newAccount1.setBalance(new BigDecimal("1000"));
        newAccount1.setInitialDeposit(new BigDecimal("1000"));
        newAccount1.setClient(client1);
        Account account1 = accountRepository.save(newAccount1);
        account1Id = account1.getId();

        User newUser1 = new User(user1Login, "123");
        newUser1.setClient(client1);
        User user1 = userRepository.save(newUser1);
        user1Id = user1.getId();

        Client client2 = clientRepository.save(new Client());

        Account newAccount2 = new Account();
        newAccount2.setBalance(new BigDecimal("500"));
        newAccount2.setInitialDeposit(new BigDecimal("500"));
        newAccount2.setClient(client2);
        Account account2 = accountRepository.save(newAccount2);
        account2Id = account2.getId();

        User newUser2 = new User(user2Login, "123");
        newUser2.setClient(client2);
        userRepository.save(newUser2);
    }

    @AfterEach
    void delete() {
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();
        clientRepository.deleteAll();
    }

    @Test
    @WithMockUser("user1")
    void transferMoney_withSuccess() throws Exception {
        Account account1 = accountRepository.findById(account1Id).get();
        Account account2 = accountRepository.findById(account2Id).get();
        BigDecimal amount = new BigDecimal("1000");
        System.out.println(account1.getBalance());

        Mockito.when(securityService.getCurrentUserClientIdWithExceptionIfNull()).thenReturn(user1Id);

        MvcResult mvcResult = mockMvc.perform(post("/api/account/transfer")
                        .param("to", user2Login)
                        .param("amount", amount.toString()))
                .andExpect(request().asyncStarted())
                .andReturn();

        String actualResponse = mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setSuccess(true);
        String expectedResponse = objectMapper.writeValueAsString(transaction);

        JsonAssert.assertJsonEquals(actualResponse, expectedResponse, JsonAssert.whenIgnoringPaths("id", "fromAccount", "toAccount", "method", "created", "updated"));

        assertThat(accountRepository.findById(account1Id))
                .isPresent()
                .get()
                .extracting("balance")
                .isEqualTo(account1.getBalance().subtract(amount).setScale(2, HALF_EVEN));

        assertThat(accountRepository.findById(account2Id))
                .isPresent()
                .get()
                .extracting("balance")
                .isEqualTo(account2.getBalance().add(amount).setScale(2, HALF_EVEN));
    }

    @Test
    @WithMockUser("user1")
    void transferMoneyWithLowBalance_thenBadRequest() throws Exception {
        Account account1 = accountRepository.findById(account1Id).get();
        Account account2 = accountRepository.findById(account2Id).get();
        BigDecimal amount = new BigDecimal("2000");
        System.out.println(account1.getBalance());

        Mockito.when(securityService.getCurrentUserClientIdWithExceptionIfNull()).thenReturn(user1Id);

        MvcResult mvcResult = mockMvc.perform(post("/api/account/transfer")
                        .param("to", user2Login)
                        .param("amount", amount.toString()))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isBadRequest());

        assertThat(accountRepository.findById(account1Id))
                .isPresent()
                .get()
                .extracting("balance")
                .isEqualTo(account1.getBalance());

        assertThat(accountRepository.findById(account2Id))
                .isPresent()
                .get()
                .extracting("balance")
                .isEqualTo(account2.getBalance());
    }
}