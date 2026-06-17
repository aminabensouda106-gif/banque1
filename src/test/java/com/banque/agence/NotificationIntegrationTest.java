package com.banque.agence;

import com.banque.agence.domain.enums.AccountType;
import com.banque.agence.domain.enums.CheckbookOrderStatus;
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
import com.banque.agence.service.DashboardService;
import com.banque.agence.service.NotificationService;
import com.banque.agence.web.dto.ClientForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class NotificationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClientService clientService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private CheckbookOrderService checkbookOrderService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

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

    @Autowired
    private ClientRepository clientRepository;

    private Long courantAccountId;

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
        form.setFirstName("Notif");
        form.setLastName("Test");
        form.setCin("NT999999");
        Long clientId = clientService.create(form, agent).getId();
        courantAccountId = accountService.openAccount(clientId, AccountType.COURANT, agent).getId();
    }

    @Test
    void newCheckbookOrderNotifiesChef() {
        var agent = userRepository.findByUsername("agent").orElseThrow();
        var chef = userRepository.findByUsername("chef").orElseThrow();

        checkbookOrderService.requestCheckbook(courantAccountId, 1, CheckbookSheetCount.FEUILLES_20, null, agent);

        assertThat(notificationService.countUnread(chef)).isEqualTo(1);
        assertThat(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(chef.getId(),
                org.springframework.data.domain.PageRequest.of(0, 10)).getContent())
                .first()
                .extracting(n -> n.getTitle())
                .isEqualTo("Nouvelle commande de chéquier");
    }

    @Test
    void statusChangeNotifiesRequester() {
        var agent = userRepository.findByUsername("agent").orElseThrow();
        var chef = userRepository.findByUsername("chef").orElseThrow();

        var order = checkbookOrderService.requestCheckbook(courantAccountId, 1, CheckbookSheetCount.FEUILLES_20, null, agent);
        notificationRepository.deleteAll();

        checkbookOrderService.updateStatus(order.getId(), CheckbookOrderStatus.PROCESSING, chef);

        assertThat(notificationService.countUnread(agent)).isEqualTo(1);
    }

    @Test
    @WithUserDetails("chef")
    void notificationsPageListsItems() throws Exception {
        var agent = userRepository.findByUsername("agent").orElseThrow();
        checkbookOrderService.requestCheckbook(courantAccountId, 1, CheckbookSheetCount.FEUILLES_20, null, agent);

        mockMvc.perform(get("/notifications"))
                .andExpect(status().isOk())
                .andExpect(view().name("notifications/list"))
                .andExpect(content().string(containsString("Nouvelle commande de chéquier")));
    }

    @Test
    @WithUserDetails("chef")
    void markNotificationAsRead() throws Exception {
        var agent = userRepository.findByUsername("agent").orElseThrow();
        var chef = userRepository.findByUsername("chef").orElseThrow();
        checkbookOrderService.requestCheckbook(courantAccountId, 1, CheckbookSheetCount.FEUILLES_20, null, agent);

        Long notificationId = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(chef.getId(),
                org.springframework.data.domain.PageRequest.of(0, 1)).getContent().get(0).getId();

        mockMvc.perform(post("/notifications/{id}/read", notificationId).with(csrf()))
                .andExpect(status().is3xxRedirection());

        assertThat(notificationService.countUnread(chef)).isZero();
    }

    @Test
    void dashboardIncludesCheckbookAlerts() {
        var agent = userRepository.findByUsername("agent").orElseThrow();
        checkbookOrderService.requestCheckbook(courantAccountId, 1, CheckbookSheetCount.FEUILLES_20, null, agent);

        var stats = dashboardService.getStats();
        assertThat(stats.pendingCheckbookOrders()).isEqualTo(1);
    }

    @Test
    @WithUserDetails("chef")
    void dashboardShowsAlertBanner() throws Exception {
        var agent = userRepository.findByUsername("agent").orElseThrow();
        checkbookOrderService.requestCheckbook(courantAccountId, 1, CheckbookSheetCount.FEUILLES_20, null, agent);

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Alertes métier")))
                .andExpect(content().string(containsString("en attente")));
    }
}
