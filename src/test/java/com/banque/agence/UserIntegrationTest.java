package com.banque.agence;

import com.banque.agence.domain.enums.UserRole;
import com.banque.agence.repository.UserRepository;
import com.banque.agence.service.BusinessRuleException;
import com.banque.agence.service.UserService;
import com.banque.agence.web.dto.UserCreateForm;
import com.banque.agence.web.dto.UserEditForm;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @WithUserDetails("admin")
    void adminCanListUsers() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users/list"));
    }

    @Test
    @WithUserDetails("agent")
    void agentCannotAccessUserManagement() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCreatesUser() {
        var admin = userRepository.findByUsername("admin").orElseThrow();
        UserCreateForm form = new UserCreateForm();
        form.setUsername("newagent");
        form.setPassword("secret123");
        form.setFullName("Nouvel Agent");
        form.setEmail("newagent@banque.local");
        form.setRole(UserRole.AGENT);

        var created = userService.create(form, admin);
        assertThat(created.getUsername()).isEqualTo("newagent");
        assertThat(created.getRole()).isEqualTo(UserRole.AGENT);
        assertThat(created.isEnabled()).isTrue();
    }

    @Test
    @WithUserDetails("admin")
    void adminCreatesUserViaHttp() throws Exception {
        mockMvc.perform(post("/admin/users")
                        .with(csrf())
                        .param("username", "httpagent")
                        .param("password", "secret123")
                        .param("fullName", "Agent HTTP")
                        .param("email", "httpagent@banque.local")
                        .param("role", "AGENT"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        assertThat(userRepository.findByUsername("httpagent")).isPresent();
    }

    @Test
    void newUserCanLogin() throws Exception {
        var admin = userRepository.findByUsername("admin").orElseThrow();
        UserCreateForm form = new UserCreateForm();
        form.setUsername("loginagent");
        form.setPassword("login123");
        form.setFullName("Login Agent");
        form.setEmail("loginagent@banque.local");
        form.setRole(UserRole.AGENT);
        userService.create(form, admin);

        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "loginagent")
                        .param("password", "login123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    void disabledUserCannotLogin() throws Exception {
        var admin = userRepository.findByUsername("admin").orElseThrow();
        UserCreateForm form = new UserCreateForm();
        form.setUsername("disabledagent");
        form.setPassword("disabled123");
        form.setFullName("Disabled Agent");
        form.setRole(UserRole.AGENT);
        var user = userService.create(form, admin);

        userService.disable(user.getId(), admin);

        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "disabledagent")
                        .param("password", "disabled123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void adminUpdatesUserRole() {
        var admin = userRepository.findByUsername("admin").orElseThrow();
        UserCreateForm form = new UserCreateForm();
        form.setUsername("promoagent");
        form.setPassword("secret123");
        form.setFullName("Promo Agent");
        form.setRole(UserRole.AGENT);
        var user = userService.create(form, admin);

        UserEditForm editForm = new UserEditForm();
        editForm.setFullName(user.getFullName());
        editForm.setEmail(user.getEmail());
        editForm.setRole(UserRole.CHEF_AGENCE);

        var updated = userService.update(user.getId(), editForm, admin);
        assertThat(updated.getRole()).isEqualTo(UserRole.CHEF_AGENCE);
    }

    @Test
    void cannotDisableLastAdmin() {
        var admin = userRepository.findByUsername("admin").orElseThrow();
        assertThatThrownBy(() -> userService.disable(admin.getId(), admin))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("dernier administrateur");
    }

    @Test
    @WithUserDetails("admin")
    void adminCanOpenPasswordForm() throws Exception {
        var admin = userRepository.findByUsername("admin").orElseThrow();
        mockMvc.perform(get("/admin/users/" + admin.getId() + "/password"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users/password"));
    }
}
