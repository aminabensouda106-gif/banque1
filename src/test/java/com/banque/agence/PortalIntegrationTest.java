package com.banque.agence;

import com.banque.agence.domain.enums.AccountType;
import com.banque.agence.domain.enums.CheckbookSheetCount;
import com.banque.agence.repository.AccountRepository;
import com.banque.agence.repository.AuditLogRepository;
import com.banque.agence.repository.BillPaymentRepository;
import com.banque.agence.repository.CheckbookOrderRepository;
import com.banque.agence.repository.ClientRepository;
import com.banque.agence.repository.NotificationRepository;
import com.banque.agence.repository.TransactionRepository;
import com.banque.agence.repository.UserRepository;
import com.banque.agence.service.AccountService;
import com.banque.agence.service.CheckbookOrderService;
import com.banque.agence.service.ClientService;
import com.banque.agence.service.TransactionService;
import com.banque.agence.web.dto.ClientForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PortalIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClientService clientService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private CheckbookOrderService checkbookOrderService;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private CheckbookOrderRepository checkbookOrderRepository;

    @Autowired
    private BillPaymentRepository billPaymentRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    private String portalLogin;
    private Long accountId;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        auditLogRepository.deleteAll();
        checkbookOrderRepository.deleteAll();
        billPaymentRepository.deleteAll();
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        clientRepository.deleteAll();

        var agent = userRepository.findByUsername("agent").orElseThrow();
        ClientForm form = new ClientForm();
        form.setFirstName("Portal");
        form.setLastName("Client");
        form.setCin("PT888888");
        Long clientId = clientService.create(form, agent).getId();

        var client = clientRepository.findById(clientId).orElseThrow();
        client.setPortalEnabled(true);
        client.setPasswordHash(passwordEncoder.encode("client123"));
        clientRepository.save(client);
        portalLogin = client.getCin();

        accountId = accountService.openAccount(clientId, AccountType.COURANT, agent).getId();
    }

    @Test
    void depositNotifiesPortalClient() {
        var agent = userRepository.findByUsername("agent").orElseThrow();
        var client = clientRepository.findActivePortalClientByLogin(portalLogin).orElseThrow();

        transactionService.deposit(accountId, new BigDecimal("100"), agent, "Test dépôt");

        assertThat(notificationRepository.countByClientRecipientIdAndReadFalse(client.getId())).isEqualTo(1);
    }

    @Test
    void checkbookRequestNotifiesPortalClient() {
        var agent = userRepository.findByUsername("agent").orElseThrow();
        var client = clientRepository.findActivePortalClientByLogin(portalLogin).orElseThrow();

        checkbookOrderService.requestCheckbook(accountId, 1, CheckbookSheetCount.FEUILLES_20, null, agent);

        assertThat(notificationRepository.countByClientRecipientIdAndReadFalse(client.getId())).isGreaterThanOrEqualTo(1);
    }

    @Test
    void clientLoginRedirectsToPortalDashboard() throws Exception {
        mockMvc.perform(formLogin("/login").user(portalLogin).password("client123"))
                .andExpect(redirectedUrl("/portal/dashboard"));
    }

    @Test
    void clientCanAccessPortalDashboard() throws Exception {
        var loginResult = mockMvc.perform(formLogin("/login").user(portalLogin).password("client123"))
                .andExpect(redirectedUrl("/portal/dashboard"))
                .andReturn();

        mockMvc.perform(get("/portal/dashboard").session((org.springframework.mock.web.MockHttpSession) loginResult.getRequest().getSession()))
                .andExpect(status().isOk());
    }

    @Test
    void clientCannotAccessStaffClientsPage() throws Exception {
        var loginResult = mockMvc.perform(formLogin("/login").user(portalLogin).password("client123"))
                .andExpect(redirectedUrl("/portal/dashboard"))
                .andReturn();

        mockMvc.perform(get("/clients").session((org.springframework.mock.web.MockHttpSession) loginResult.getRequest().getSession()))
                .andExpect(status().isForbidden());
    }
}
