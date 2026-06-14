package com.banque.agence;

import com.banque.agence.domain.enums.ClientStatus;
import com.banque.agence.repository.AuditLogRepository;
import com.banque.agence.repository.ClientRepository;
import com.banque.agence.repository.UserRepository;
import com.banque.agence.service.ClientService;
import com.banque.agence.service.DuplicateResourceException;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ClientIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClientService clientService;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanClients() {
        auditLogRepository.deleteAll();
        clientRepository.deleteAll();
    }

    @Test
    @WithUserDetails("agent")
    void clientsListIsAccessible() throws Exception {
        mockMvc.perform(get("/clients"))
                .andExpect(status().isOk())
                .andExpect(view().name("clients/list"));
    }

    @Test
    @WithUserDetails("agent")
    void createClientAndSearchByCin() throws Exception {
        mockMvc.perform(post("/clients")
                        .with(csrf())
                        .param("firstName", "Karim")
                        .param("lastName", "Alaoui")
                        .param("cin", "AB123456")
                        .param("email", "karim@example.com")
                        .param("phone", "0612345678")
                        .param("address", "Casablanca"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/clients/*"));

        assertThat(clientRepository.findAll()).hasSize(1);
        assertThat(auditLogRepository.count()).isEqualTo(1);

        mockMvc.perform(get("/clients").param("q", "AB123456"))
                .andExpect(status().isOk())
                .andExpect(view().name("clients/list"));
    }

    @Test
    void duplicateCinIsRejected() {
        var user = userRepository.findByUsername("agent").orElseThrow();
        ClientForm form = new ClientForm();
        form.setFirstName("Sara");
        form.setLastName("Benali");
        form.setCin("CD999888");

        clientService.create(form, user);

        assertThatThrownBy(() -> clientService.create(form, user))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("CIN");
    }

    @Test
    void changeStatusUpdatesClient() {
        var user = userRepository.findByUsername("agent").orElseThrow();
        ClientForm form = new ClientForm();
        form.setFirstName("Youssef");
        form.setLastName("Idrissi");
        form.setCin("EF111222");

        var client = clientService.create(form, user);
        var updated = clientService.changeStatus(client.getId(), ClientStatus.SUSPENDED, user);

        assertThat(updated.getStatus()).isEqualTo(ClientStatus.SUSPENDED);
        assertThat(auditLogRepository.count()).isEqualTo(2);
    }

    @Test
    @WithUserDetails("agent")
    void searchByNameReturnsMatchingClient() throws Exception {
        var user = userRepository.findByUsername("agent").orElseThrow();
        ClientForm form = new ClientForm();
        form.setFirstName("Fatima");
        form.setLastName("Zahraoui");
        form.setCin("XZ555666");
        clientService.create(form, user);

        mockMvc.perform(get("/clients").param("q", "Zahraoui"))
                .andExpect(status().isOk())
                .andExpect(view().name("clients/list"))
                .andExpect(model().attributeExists("clients"));
    }

    @Test
    @WithUserDetails("agent")
    void detailPageShowsClientInfo() throws Exception {
        var user = userRepository.findByUsername("agent").orElseThrow();
        ClientForm form = new ClientForm();
        form.setFirstName("Omar");
        form.setLastName("Berrada");
        form.setCin("IJ777888");
        form.setEmail("omar@example.com");
        form.setPhone("0699887766");
        form.setAddress("Rabat");
        form.setProfessionalInfo("Commerçant");
        var client = clientService.create(form, user);

        mockMvc.perform(get("/clients/" + client.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("clients/detail"))
                .andExpect(model().attribute("client", client))
                .andExpect(content().string(containsString("Omar Berrada")))
                .andExpect(content().string(containsString("IJ777888")))
                .andExpect(content().string(containsString("omar@example.com")));
    }

    @Test
    @WithUserDetails("agent")
    void paginationWorksWhenMoreThanTenClients() throws Exception {
        var user = userRepository.findByUsername("agent").orElseThrow();
        for (int i = 1; i <= 11; i++) {
            ClientForm form = new ClientForm();
            form.setFirstName("Client");
            form.setLastName("Num" + i);
            form.setCin(String.format("PG%06d", i));
            clientService.create(form, user);
        }

        mockMvc.perform(get("/clients").param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("clients", org.hamcrest.Matchers.hasProperty("totalPages", org.hamcrest.Matchers.is(2))));

        mockMvc.perform(get("/clients").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("clients/list"));
    }

    @Test
    @WithUserDetails("agent")
    void createClientWithDuplicateCinShowsError() throws Exception {
        var user = userRepository.findByUsername("agent").orElseThrow();
        ClientForm form = new ClientForm();
        form.setFirstName("Nadia");
        form.setLastName("Tazi");
        form.setCin("GH333444");
        clientService.create(form, user);

        mockMvc.perform(post("/clients")
                        .with(csrf())
                        .param("firstName", "Autre")
                        .param("lastName", "Client")
                        .param("cin", "GH333444"))
                .andExpect(status().isOk())
                .andExpect(view().name("clients/form"))
                .andExpect(content().string(containsString("CIN")));
    }

    @Test
    @WithUserDetails("agent")
    void suspendClientViaHttp() throws Exception {
        var user = userRepository.findByUsername("agent").orElseThrow();
        ClientForm form = new ClientForm();
        form.setFirstName("Laila");
        form.setLastName("Amrani");
        form.setCin("KL121212");
        var client = clientService.create(form, user);

        mockMvc.perform(post("/clients/" + client.getId() + "/status")
                        .with(csrf())
                        .param("status", "SUSPENDED"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clients/" + client.getId()));

        assertThat(clientRepository.findById(client.getId()).orElseThrow().getStatus())
                .isEqualTo(ClientStatus.SUSPENDED);
    }
}
