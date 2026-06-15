package com.banque.agence.web.controller;

import com.banque.agence.domain.entity.User;
import com.banque.agence.security.SecurityUser;
import com.banque.agence.service.BusinessRuleException;
import com.banque.agence.service.DuplicateResourceException;
import com.banque.agence.service.ResourceNotFoundException;
import com.banque.agence.service.UserService;
import com.banque.agence.web.dto.PasswordForm;
import com.banque.agence.web.dto.UserCreateForm;
import com.banque.agence.web.dto.UserEditForm;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private static final int PAGE_SIZE = 10;

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @ModelAttribute("userRoles")
    public com.banque.agence.domain.enums.UserRole[] userRoles() {
        return com.banque.agence.domain.enums.UserRole.values();
    }

    @GetMapping
    public String list(
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {
        Page<User> users = userService.listAll(
                PageRequest.of(page, PAGE_SIZE, Sort.by("username"))
        );
        model.addAttribute("pageTitle", "Gestion des utilisateurs");
        model.addAttribute("activePage", "users");
        model.addAttribute("users", users);
        return "admin/users/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("pageTitle", "Nouvel utilisateur");
        model.addAttribute("activePage", "users");
        model.addAttribute("userCreateForm", new UserCreateForm());
        return "admin/users/form-create";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("userCreateForm") UserCreateForm form,
            BindingResult bindingResult,
            @AuthenticationPrincipal SecurityUser securityUser,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Nouvel utilisateur");
            model.addAttribute("activePage", "users");
            return "admin/users/form-create";
        }
        try {
            User user = userService.create(form, securityUser.getUser());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Utilisateur " + user.getUsername() + " créé avec succès.");
            return "redirect:/admin/users";
        } catch (DuplicateResourceException ex) {
            bindingResult.rejectValue("username", "duplicate.username", ex.getMessage());
            model.addAttribute("pageTitle", "Nouvel utilisateur");
            model.addAttribute("activePage", "users");
            return "admin/users/form-create";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        User user = userService.findById(id);
        model.addAttribute("pageTitle", "Modifier " + user.getUsername());
        model.addAttribute("activePage", "users");
        model.addAttribute("user", user);
        model.addAttribute("userEditForm", UserEditForm.fromUser(user));
        return "admin/users/form-edit";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("userEditForm") UserEditForm form,
            BindingResult bindingResult,
            @AuthenticationPrincipal SecurityUser securityUser,
            Model model,
            RedirectAttributes redirectAttributes) {
        User user = userService.findById(id);
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Modifier " + user.getUsername());
            model.addAttribute("activePage", "users");
            model.addAttribute("user", user);
            return "admin/users/form-edit";
        }
        try {
            userService.update(id, form, securityUser.getUser());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Utilisateur " + user.getUsername() + " mis à jour.");
            return "redirect:/admin/users";
        } catch (BusinessRuleException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("pageTitle", "Modifier " + user.getUsername());
            model.addAttribute("activePage", "users");
            model.addAttribute("user", user);
            return "admin/users/form-edit";
        }
    }

    @GetMapping("/{id}/password")
    public String passwordForm(@PathVariable Long id, Model model) {
        User user = userService.findById(id);
        model.addAttribute("pageTitle", "Mot de passe — " + user.getUsername());
        model.addAttribute("activePage", "users");
        model.addAttribute("user", user);
        model.addAttribute("passwordForm", new PasswordForm());
        return "admin/users/password";
    }

    @PostMapping("/{id}/password")
    public String changePassword(
            @PathVariable Long id,
            @Valid @ModelAttribute("passwordForm") PasswordForm form,
            BindingResult bindingResult,
            @AuthenticationPrincipal SecurityUser securityUser,
            Model model,
            RedirectAttributes redirectAttributes) {
        User user = userService.findById(id);
        if (!form.isMatching()) {
            bindingResult.rejectValue("confirmPassword", "password.mismatch",
                    "Les mots de passe ne correspondent pas.");
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Mot de passe — " + user.getUsername());
            model.addAttribute("activePage", "users");
            model.addAttribute("user", user);
            return "admin/users/password";
        }
        userService.changePassword(id, form.getPassword(), securityUser.getUser());
        redirectAttributes.addFlashAttribute("successMessage",
                "Mot de passe de " + user.getUsername() + " mis à jour.");
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/enable")
    public String enable(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser securityUser,
            RedirectAttributes redirectAttributes) {
        return changeStatus(id, securityUser, redirectAttributes, true);
    }

    @PostMapping("/{id}/disable")
    public String disable(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser securityUser,
            RedirectAttributes redirectAttributes) {
        return changeStatus(id, securityUser, redirectAttributes, false);
    }

    private String changeStatus(Long id, SecurityUser securityUser,
                                RedirectAttributes redirectAttributes, boolean enable) {
        try {
            User user = enable
                    ? userService.enable(id, securityUser.getUser())
                    : userService.disable(id, securityUser.getUser());
            redirectAttributes.addFlashAttribute("successMessage",
                    enable
                            ? "Utilisateur " + user.getUsername() + " activé."
                            : "Utilisateur " + user.getUsername() + " désactivé.");
        } catch (BusinessRuleException | ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/users";
    }
}
