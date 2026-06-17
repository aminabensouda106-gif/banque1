package com.banque.agence;



import com.banque.agence.domain.enums.AccountType;

import com.banque.agence.domain.enums.CheckbookOrderStatus;

import com.banque.agence.domain.enums.CheckbookSheetCount;

import com.banque.agence.repository.AccountRepository;

import com.banque.agence.repository.AuditLogRepository;

import com.banque.agence.repository.BillPaymentRepository;

import com.banque.agence.repository.CheckbookOrderRepository;

import com.banque.agence.repository.ClientRepository;

import com.banque.agence.repository.TransactionRepository;

import com.banque.agence.repository.UserRepository;

import com.banque.agence.service.AccountService;

import com.banque.agence.service.BusinessRuleException;

import com.banque.agence.service.CheckbookOrderService;

import com.banque.agence.service.ClientService;

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



import static org.assertj.core.api.Assertions.assertThat;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;



@SpringBootTest

@AutoConfigureMockMvc

@Transactional

class CheckbookOrderIntegrationTest {



    @Autowired

    private MockMvc mockMvc;



    @Autowired

    private CheckbookOrderService checkbookOrderService;



    @Autowired

    private AccountService accountService;



    @Autowired

    private ClientService clientService;



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



    @Autowired

    private AuditLogRepository auditLogRepository;



    @Autowired

    private UserRepository userRepository;



    private Long courantAccountId;

    private Long epargneAccountId;



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

        form.setFirstName("Chq");

        form.setLastName("Client");

        form.setCin("CHQ00001");

        Long clientId = clientService.create(form, user).getId();



        courantAccountId = accountService.openAccount(clientId, AccountType.COURANT, user).getId();

        epargneAccountId = accountService.openAccount(clientId, AccountType.EPARGNE, user).getId();

    }



    @Test

    void requestOnCourantAccountCreatesPendingOrder() {

        var user = userRepository.findByUsername("agent").orElseThrow();

        var order = checkbookOrderService.requestCheckbook(

                courantAccountId, 1, CheckbookSheetCount.FEUILLES_20, null, user);



        assertThat(order.getOrderNumber()).startsWith("CHQ-");

        assertThat(order.getStatus()).isEqualTo(CheckbookOrderStatus.PENDING);

        assertThat(order.getSheetCount()).isEqualTo(CheckbookSheetCount.FEUILLES_20);

        assertThat(accountService.findById(courantAccountId).getBalance()).isZero();

    }



    @Test

    void requestWithFortySheets() {

        var user = userRepository.findByUsername("agent").orElseThrow();

        var order = checkbookOrderService.requestCheckbook(

                courantAccountId, 1, CheckbookSheetCount.FEUILLES_40, null, user);



        assertThat(order.getSheetCount()).isEqualTo(CheckbookSheetCount.FEUILLES_40);

        assertThat(order.getSheetCount().getSheetCount()).isEqualTo(40);

    }



    @Test

    void requestOnEpargneAccountFails() {

        var user = userRepository.findByUsername("agent").orElseThrow();



        assertThatThrownBy(() -> checkbookOrderService.requestCheckbook(

                epargneAccountId, 1, CheckbookSheetCount.FEUILLES_20, null, user))

                .isInstanceOf(BusinessRuleException.class)

                .hasMessageContaining("courants ou professionnels");

    }



    @Test

    void secondPendingOrderOnSameAccountFails() {

        var user = userRepository.findByUsername("agent").orElseThrow();

        checkbookOrderService.requestCheckbook(

                courantAccountId, 1, CheckbookSheetCount.FEUILLES_20, null, user);



        assertThatThrownBy(() -> checkbookOrderService.requestCheckbook(

                courantAccountId, 1, CheckbookSheetCount.FEUILLES_40, null, user))

                .isInstanceOf(BusinessRuleException.class)

                .hasMessageContaining("déjà en attente");

    }



    @Test

    void workflowPendingToDelivered() {

        var user = userRepository.findByUsername("agent").orElseThrow();

        var order = checkbookOrderService.requestCheckbook(

                courantAccountId, 2, CheckbookSheetCount.FEUILLES_40, "Urgent", user);



        checkbookOrderService.updateStatus(order.getId(), CheckbookOrderStatus.PROCESSING, user);

        var delivered = checkbookOrderService.updateStatus(order.getId(), CheckbookOrderStatus.DELIVERED, user);



        assertThat(delivered.getStatus()).isEqualTo(CheckbookOrderStatus.DELIVERED);

        assertThat(delivered.getSheetCount()).isEqualTo(CheckbookSheetCount.FEUILLES_40);

        assertThat(delivered.getProcessedAt()).isNotNull();

        assertThat(delivered.getDeliveredAt()).isNotNull();

    }



    @Test

    void filterByPendingStatus() {

        var user = userRepository.findByUsername("agent").orElseThrow();

        checkbookOrderService.requestCheckbook(

                courantAccountId, 1, CheckbookSheetCount.FEUILLES_20, null, user);



        var page = checkbookOrderService.list(CheckbookOrderStatus.PENDING, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(1);

    }



    @Test

    @WithUserDetails("agent")

    void requestViaHttp() throws Exception {

        mockMvc.perform(post("/checkbook-orders")

                        .with(csrf())

                        .param("accountId", courantAccountId.toString())

                        .param("quantity", "1")

                        .param("sheetCount", "FEUILLES_40"))

                .andExpect(status().is3xxRedirection())

                .andExpect(redirectedUrlPattern("/checkbook-orders/*"));

    }



    @Test

    @WithUserDetails("agent")

    void listAndRequestPagesAccessible() throws Exception {

        mockMvc.perform(get("/checkbook-orders"))

                .andExpect(status().isOk())

                .andExpect(view().name("checkbook-orders/list"));

        mockMvc.perform(get("/checkbook-orders/new"))

                .andExpect(status().isOk())

                .andExpect(view().name("checkbook-orders/request"));

    }

}


