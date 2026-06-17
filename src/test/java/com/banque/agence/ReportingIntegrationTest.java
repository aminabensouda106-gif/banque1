package com.banque.agence;

import com.banque.agence.domain.enums.AccountType;
import com.banque.agence.repository.AccountRepository;
import com.banque.agence.repository.AuditLogRepository;
import com.banque.agence.repository.BillPaymentRepository;
import com.banque.agence.repository.CheckbookOrderRepository;
import com.banque.agence.repository.ClientRepository;
import com.banque.agence.repository.TransactionRepository;
import com.banque.agence.repository.UserRepository;
import com.banque.agence.service.AccountService;
import com.banque.agence.service.ClientService;
import com.banque.agence.service.DashboardService;
import com.banque.agence.service.TransactionService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReportingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DashboardService dashboardService;

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
    private CheckbookOrderRepository checkbookOrderRepository;

    @Autowired
    private BillPaymentRepository billPaymentRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UserRepository userRepository;

    private Long accountId;
    private Long transactionId;

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();
        checkbookOrderRepository.deleteAll();
        billPaymentRepository.deleteAll();
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        clientRepository.deleteAll();

        var user = userRepository.findByUsername("agent").orElseThrow();
        ClientForm form = new ClientForm();
        form.setFirstName("Report");
        form.setLastName("Client");
        form.setCin("RP000001");
        Long clientId = clientService.create(form, user).getId();

        accountId = accountService.openAccount(clientId, AccountType.COURANT, user).getId();
        transactionId = transactionService.deposit(accountId, new BigDecimal("500"), user, "Test reporting").transactionId();
    }

    @Test
    void dashboardStatsReflectRealData() {
        var stats = dashboardService.getStats();
        assertThat(stats.activeClients()).isGreaterThanOrEqualTo(1);
        assertThat(stats.activeAccounts()).isGreaterThanOrEqualTo(1);
        assertThat(stats.todayTransactionCount()).isGreaterThanOrEqualTo(1);
        assertThat(stats.todayTransactionAmount()).isGreaterThanOrEqualTo(new BigDecimal("500"));
        assertThat(dashboardService.getRecentTransactions()).isNotEmpty();
    }

    @Test
    @WithUserDetails("agent")
    void agentCanViewDashboard() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Clients actifs")));
    }

    @Test
    @WithUserDetails("chef")
    void chefCanViewDashboard() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"));
    }

    @Test
    @WithUserDetails("agent")
    void agentCanViewAccountStatement() throws Exception {
        mockMvc.perform(get("/accounts/{id}/statement", accountId))
                .andExpect(status().isOk())
                .andExpect(view().name("accounts/statement"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Solde courant")));
    }

    @Test
    @WithUserDetails("agent")
    void agentCanViewTransactionReceipt() throws Exception {
        mockMvc.perform(get("/transactions/{id}/receipt", transactionId))
                .andExpect(status().isOk())
                .andExpect(view().name("transactions/receipt"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Reçu d'opération")));
    }

    @Test
    @WithUserDetails("admin")
    void adminCanViewAuditLog() throws Exception {
        mockMvc.perform(get("/admin/audit"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/audit/list"));
    }

    @Test
    @WithUserDetails("chef")
    void chefCanViewAuditLog() throws Exception {
        mockMvc.perform(get("/admin/audit"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/audit/list"));
    }

    @Test
    @WithUserDetails("agent")
    void agentCannotViewAuditLog() throws Exception {
        mockMvc.perform(get("/admin/audit"))
                .andExpect(status().isForbidden());
    }
}
