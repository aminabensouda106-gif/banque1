package com.banque.agence;

import com.banque.agence.domain.enums.AccountStatus;
import com.banque.agence.domain.enums.AccountType;
import com.banque.agence.domain.enums.ClientStatus;
import com.banque.agence.repository.AccountRepository;
import com.banque.agence.repository.AuditLogRepository;
import com.banque.agence.repository.TransactionRepository;
import com.banque.agence.repository.ClientRepository;
import com.banque.agence.repository.UserRepository;
import com.banque.agence.service.AccountService;
import com.banque.agence.service.BusinessRuleException;
import com.banque.agence.service.ClientService;
import com.banque.agence.web.dto.ClientForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AccountIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountService accountService;

    @Autowired
    private ClientService clientService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UserRepository userRepository;

    private Long clientId;

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        clientRepository.deleteAll();

        var user = userRepository.findByUsername("agent").orElseThrow();
        ClientForm form = new ClientForm();
        form.setFirstName("Client");
        form.setLastName("Test");
        form.setCin("ACCTEST01");
        clientId = clientService.create(form, user).getId();
    }

    @Test
    void openCourantAndEpargneForSameClient() {
        var user = userRepository.findByUsername("agent").orElseThrow();
        var courant = accountService.openAccount(clientId, AccountType.COURANT, user);
        var epargne = accountService.openAccount(clientId, AccountType.EPARGNE, user);

        assertThat(courant.getAccountNumber()).isEqualTo("ACC-00001");
        assertThat(epargne.getAccountNumber()).isEqualTo("ACC-00002");
        assertThat(accountService.listByClient(clientId)).hasSize(2);
    }

    @Test
    void initialBalanceIsZero() {
        var user = userRepository.findByUsername("agent").orElseThrow();
        var account = accountService.openAccount(clientId, AccountType.COURANT, user);
        assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void blockAndUnblockAccount() {
        var user = userRepository.findByUsername("agent").orElseThrow();
        var account = accountService.openAccount(clientId, AccountType.COURANT, user);

        accountService.block(account.getId(), user);
        assertThat(accountService.findById(account.getId()).getStatus()).isEqualTo(AccountStatus.BLOCKED);

        accountService.unblock(account.getId(), user);
        assertThat(accountService.findById(account.getId()).getStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    void closeAccountSetsStatusAndDate() {
        var user = userRepository.findByUsername("agent").orElseThrow();
        var account = accountService.openAccount(clientId, AccountType.COURANT, user);

        var closed = accountService.close(account.getId(), user);
        assertThat(closed.getStatus()).isEqualTo(AccountStatus.CLOSED);
        assertThat(closed.getClosedAt()).isNotNull();
    }

    @Test
    void cannotBlockClosedAccount() {
        var user = userRepository.findByUsername("agent").orElseThrow();
        var account = accountService.openAccount(clientId, AccountType.COURANT, user);
        accountService.close(account.getId(), user);

        assertThatThrownBy(() -> accountService.block(account.getId(), user))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("clôturé");
    }

    @Test
    @WithUserDetails("agent")
    void clientDetailShowsAccountsList() throws Exception {
        var user = userRepository.findByUsername("agent").orElseThrow();
        accountService.openAccount(clientId, AccountType.COURANT, user);

        mockMvc.perform(get("/clients/" + clientId))
                .andExpect(status().isOk())
                .andExpect(view().name("clients/detail"))
                .andExpect(content().string(containsString("ACC-00001")))
                .andExpect(content().string(containsString("Comptes bancaires")));
    }

    @Test
    @WithUserDetails("agent")
    void openAccountViaHttp() throws Exception {
        mockMvc.perform(post("/accounts/client/" + clientId)
                        .with(csrf())
                        .param("type", "COURANT"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/accounts/*"));
    }

    @Test
    @WithUserDetails("agent")
    void accountsListPageAccessible() throws Exception {
        mockMvc.perform(get("/accounts"))
                .andExpect(status().isOk())
                .andExpect(view().name("accounts/list"));
    }

    @Test
    @WithUserDetails("agent")
    void accountDetailPageShowsBalanceAndClient() throws Exception {
        var user = userRepository.findByUsername("agent").orElseThrow();
        var account = accountService.openAccount(clientId, AccountType.EPARGNE, user);

        mockMvc.perform(get("/accounts/" + account.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("accounts/detail"))
                .andExpect(content().string(containsString("ACC-00001")))
                .andExpect(content().string(containsString("Compte épargne")))
                .andExpect(content().string(containsString("Client Test")))
                .andExpect(content().string(containsString("0.00 MAD")));
    }

    @Test
    @WithUserDetails("agent")
    void blockUnblockAndCloseViaHttp() throws Exception {
        var user = userRepository.findByUsername("agent").orElseThrow();
        var account = accountService.openAccount(clientId, AccountType.COURANT, user);
        Long accountId = account.getId();

        mockMvc.perform(post("/accounts/" + accountId + "/block").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/accounts/" + accountId));
        assertThat(accountService.findById(accountId).getStatus()).isEqualTo(AccountStatus.BLOCKED);

        mockMvc.perform(post("/accounts/" + accountId + "/unblock").with(csrf()))
                .andExpect(status().is3xxRedirection());
        assertThat(accountService.findById(accountId).getStatus()).isEqualTo(AccountStatus.ACTIVE);

        mockMvc.perform(post("/accounts/" + accountId + "/close").with(csrf()))
                .andExpect(status().is3xxRedirection());
        var closed = accountService.findById(accountId);
        assertThat(closed.getStatus()).isEqualTo(AccountStatus.CLOSED);
        assertThat(closed.getClosedAt()).isNotNull();

        mockMvc.perform(get("/accounts/" + accountId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Clôturé")));
    }

    @Test
    void cannotOpenAccountForInactiveClient() {
        var user = userRepository.findByUsername("agent").orElseThrow();
        clientService.changeStatus(clientId, ClientStatus.SUSPENDED, user);

        assertThatThrownBy(() -> accountService.openAccount(clientId, AccountType.COURANT, user))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("non actif");
    }
}
