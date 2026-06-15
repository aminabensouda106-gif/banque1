package com.banque.agence;

import com.banque.agence.domain.enums.AccountStatus;
import com.banque.agence.domain.enums.AccountType;
import com.banque.agence.domain.enums.TransactionType;
import com.banque.agence.repository.AccountRepository;
import com.banque.agence.repository.AuditLogRepository;
import com.banque.agence.repository.ClientRepository;
import com.banque.agence.repository.TransactionRepository;
import com.banque.agence.repository.UserRepository;
import com.banque.agence.service.AccountService;
import com.banque.agence.service.BusinessRuleException;
import com.banque.agence.service.ClientService;
import com.banque.agence.service.TransactionService;
import com.banque.agence.web.dto.ClientForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TransactionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private ClientService clientService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UserRepository userRepository;

    private Long accountId1;
    private Long accountId2;

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        clientRepository.deleteAll();

        var user = userRepository.findByUsername("agent").orElseThrow();
        ClientForm form = new ClientForm();
        form.setFirstName("Tx");
        form.setLastName("Client");
        form.setCin("TX000001");
        Long clientId = clientService.create(form, user).getId();

        accountId1 = accountService.openAccount(clientId, AccountType.COURANT, user).getId();
        accountId2 = accountService.openAccount(clientId, AccountType.EPARGNE, user).getId();
    }

    @Test
    void depositIncreasesBalance() {
        var user = userRepository.findByUsername("agent").orElseThrow();
        var result = transactionService.deposit(accountId1, new BigDecimal("1000"), user, "Test dépôt");
        assertThat(result.newBalance()).isEqualByComparingTo("1000");
    }

    @Test
    void withdrawWithInsufficientBalanceFails() {
        var user = userRepository.findByUsername("agent").orElseThrow();
        transactionService.deposit(accountId1, new BigDecimal("1000"), user, null);

        assertThatThrownBy(() -> transactionService.withdraw(accountId1, new BigDecimal("1500"), user, null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Solde insuffisant");
    }

    @Test
    void withdrawAndDepositFlow() {
        var user = userRepository.findByUsername("agent").orElseThrow();
        transactionService.deposit(accountId1, new BigDecimal("1000"), user, null);
        transactionService.withdraw(accountId1, new BigDecimal("300"), user, null);
        assertThat(accountService.findById(accountId1).getBalance()).isEqualByComparingTo("700");
    }

    @Test
    void transferUpdatesBothBalances() {
        var user = userRepository.findByUsername("agent").orElseThrow();
        transactionService.deposit(accountId1, new BigDecimal("1000"), user, null);
        var result = transactionService.transfer(accountId1, accountId2, new BigDecimal("200"), user, null);

        assertThat(result.sourceBalance()).isEqualByComparingTo("800");
        assertThat(result.destinationBalance()).isEqualByComparingTo("200");
    }

    @Test
    void transferFromBlockedAccountFails() {
        var user = userRepository.findByUsername("agent").orElseThrow();
        transactionService.deposit(accountId1, new BigDecimal("1000"), user, null);
        accountService.block(accountId1, user);

        assertThatThrownBy(() -> transactionService.transfer(accountId1, accountId2, new BigDecimal("100"), user, null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("actif");
    }

    @Test
    void transferToBlockedAccountFails() {
        var user = userRepository.findByUsername("agent").orElseThrow();
        transactionService.deposit(accountId1, new BigDecimal("1000"), user, null);
        accountService.block(accountId2, user);

        assertThatThrownBy(() -> transactionService.transfer(accountId1, accountId2, new BigDecimal("100"), user, null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("actif");
    }

    @Test
    void filterByDepotType() {
        var user = userRepository.findByUsername("agent").orElseThrow();
        transactionService.deposit(accountId1, new BigDecimal("500"), user, null);
        transactionService.withdraw(accountId1, new BigDecimal("100"), user, null);

        var page = transactionService.search(TransactionType.DEPOT, null, null, null, null, PageRequest.of(0, 10));
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().getFirst().getType()).isEqualTo(TransactionType.DEPOT);
    }

    @Test
    @WithUserDetails("agent")
    void depositViaHttp() throws Exception {
        mockMvc.perform(post("/operations/deposit")
                        .with(csrf())
                        .param("accountId", accountId1.toString())
                        .param("amount", "1000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/transactions"));

        assertThat(accountService.findById(accountId1).getBalance()).isEqualByComparingTo("1000");
    }

    @Test
    void filterByDepotTypeAndAccountNumber() {
        var user = userRepository.findByUsername("agent").orElseThrow();
        transactionService.deposit(accountId1, new BigDecimal("500"), user, null);
        String accountNumber = accountService.findById(accountId1).getAccountNumber();

        var page = transactionService.search(
                TransactionType.DEPOT, accountNumber, null, null, null, PageRequest.of(0, 10));
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().getFirst().getType()).isEqualTo(TransactionType.DEPOT);
    }

    @Test
    void depositWithExcessiveAmountFails() {
        var user = userRepository.findByUsername("agent").orElseThrow();
        assertThatThrownBy(() -> transactionService.deposit(
                accountId1, new BigDecimal("1000000000000000"), user, null))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("limite");
    }

    @Test
    @WithUserDetails("agent")
    void depositWithExcessiveAmountViaHttpShowsError() throws Exception {
        mockMvc.perform(post("/operations/deposit")
                        .with(csrf())
                        .param("accountId", accountId1.toString())
                        .param("amount", "1000000000000000"))
                .andExpect(status().isOk())
                .andExpect(view().name("operations/deposit"));
    }

    @Test
    @WithUserDetails("agent")
    void operationsAndHistoryPagesAccessible() throws Exception {
        mockMvc.perform(get("/operations")).andExpect(status().isOk()).andExpect(view().name("operations/index"));
        mockMvc.perform(get("/transactions")).andExpect(status().isOk()).andExpect(view().name("transactions/list"));
    }
}
