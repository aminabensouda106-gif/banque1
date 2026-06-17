package com.banque.agence;

import com.banque.agence.domain.enums.AccountType;
import com.banque.agence.domain.enums.TransactionType;
import com.banque.agence.repository.AccountRepository;
import com.banque.agence.repository.AuditLogRepository;
import com.banque.agence.repository.BillPaymentRepository;
import com.banque.agence.repository.BillProviderRepository;
import com.banque.agence.repository.CheckbookOrderRepository;
import com.banque.agence.repository.ClientRepository;
import com.banque.agence.repository.TransactionRepository;
import com.banque.agence.repository.UserRepository;
import com.banque.agence.service.AccountService;
import com.banque.agence.service.BillPaymentService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BillPaymentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BillPaymentService billPaymentService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private ClientService clientService;

    @Autowired
    private BillProviderRepository billProviderRepository;

    @Autowired
    private BillPaymentRepository billPaymentRepository;

    @Autowired
    private CheckbookOrderRepository checkbookOrderRepository;

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

    private Long accountId;
    private Long lydecProviderId;

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();
        checkbookOrderRepository.deleteAll();
        billPaymentRepository.deleteAll();
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        clientRepository.deleteAll();

        lydecProviderId = billProviderRepository.findAll().stream()
                .filter(p -> "LYDEC".equals(p.getCode()))
                .findFirst()
                .orElseThrow()
                .getId();

        var user = userRepository.findByUsername("agent").orElseThrow();
        ClientForm form = new ClientForm();
        form.setFirstName("Bill");
        form.setLastName("Client");
        form.setCin("BL000001");
        Long clientId = clientService.create(form, user).getId();

        accountId = accountService.openAccount(clientId, AccountType.COURANT, user).getId();
        transactionService.deposit(accountId, new BigDecimal("500"), user, null);
    }

    @Test
    void billPaymentDebitsAccount() {
        var user = userRepository.findByUsername("agent").orElseThrow();
        var result = billPaymentService.payBill(
                accountId, lydecProviderId, "REF-12345", new BigDecimal("150"), user);

        assertThat(result.newBalance()).isEqualByComparingTo("350");
        assertThat(billPaymentService.findByTransactionId(result.transactionId())).isPresent();
    }

    @Test
    void billPaymentWithInsufficientBalanceFails() {
        var user = userRepository.findByUsername("agent").orElseThrow();

        assertThatThrownBy(() -> billPaymentService.payBill(
                accountId, lydecProviderId, "REF-999", new BigDecimal("600"), user))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Solde insuffisant");
    }

    @Test
    void billPaymentOnBlockedAccountFails() {
        var user = userRepository.findByUsername("agent").orElseThrow();
        accountService.block(accountId, user);

        assertThatThrownBy(() -> billPaymentService.payBill(
                accountId, lydecProviderId, "REF-001", new BigDecimal("100"), user))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("actif");
    }

    @Test
    void filterByBillPaymentType() {
        var user = userRepository.findByUsername("agent").orElseThrow();
        billPaymentService.payBill(accountId, lydecProviderId, "REF-FILTER", new BigDecimal("100"), user);

        var page = transactionService.search(
                TransactionType.PAIEMENT_FACTURE, null, null, null, null, PageRequest.of(0, 10));
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().getFirst().getType()).isEqualTo(TransactionType.PAIEMENT_FACTURE);
    }

    @Test
    @WithUserDetails("agent")
    void billPaymentViaHttpRedirectsToReceipt() throws Exception {
        mockMvc.perform(post("/operations/bill-payment")
                        .with(csrf())
                        .param("accountId", accountId.toString())
                        .param("providerId", lydecProviderId.toString())
                        .param("clientReference", "CTR-2026-001")
                        .param("amount", "200"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/transactions/*/receipt"));

        assertThat(accountService.findById(accountId).getBalance()).isEqualByComparingTo("300");
    }

    @Test
    @WithUserDetails("agent")
    void billPaymentReceiptShowsProviderAndReference() throws Exception {
        var user = userRepository.findByUsername("agent").orElseThrow();
        var result = billPaymentService.payBill(
                accountId, lydecProviderId, "REF-RECEIPT", new BigDecimal("50"), user);

        mockMvc.perform(get("/transactions/{id}/receipt", result.transactionId()))
                .andExpect(status().isOk())
                .andExpect(view().name("transactions/receipt"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("LYDEC")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("REF-RECEIPT")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Paiement facture")));
    }

    @Test
    @WithUserDetails("agent")
    void billPaymentFormAccessible() throws Exception {
        mockMvc.perform(get("/operations/bill-payment"))
                .andExpect(status().isOk())
                .andExpect(view().name("operations/bill-payment"));
    }
}
