package com.banque.agence.web.controller;

import com.banque.agence.domain.entity.Account;
import com.banque.agence.domain.entity.Client;
import com.banque.agence.domain.enums.AccountType;
import com.banque.agence.security.SecurityUser;
import com.banque.agence.service.AccountService;
import com.banque.agence.service.BusinessRuleException;
import com.banque.agence.service.ClientService;
import com.banque.agence.service.ResourceNotFoundException;
import com.banque.agence.web.dto.OpenAccountForm;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
@RequestMapping("/accounts")
public class AccountController {

    private static final int PAGE_SIZE = 10;

    private final AccountService accountService;
    private final ClientService clientService;

    public AccountController(AccountService accountService, ClientService clientService) {
        this.accountService = accountService;
        this.clientService = clientService;
    }

    @GetMapping
    public String list(
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {
        Page<Account> accounts = accountService.listAll(
                PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "openedAt"))
        );
        model.addAttribute("pageTitle", "Comptes");
        model.addAttribute("activePage", "accounts");
        model.addAttribute("accounts", accounts);
        return "accounts/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Account account = accountService.findById(id);
        model.addAttribute("pageTitle", account.getAccountNumber());
        model.addAttribute("activePage", "accounts");
        model.addAttribute("account", account);
        return "accounts/detail";
    }

    @GetMapping("/client/{clientId}/new")
    public String openForm(@PathVariable Long clientId, Model model) {
        Client client = clientService.findById(clientId);
        model.addAttribute("pageTitle", "Ouvrir un compte");
        model.addAttribute("activePage", "clients");
        model.addAttribute("client", client);
        model.addAttribute("openAccountForm", new OpenAccountForm());
        model.addAttribute("accountTypes", AccountType.values());
        return "accounts/open";
    }

    @PostMapping("/client/{clientId}")
    public String openAccount(
            @PathVariable Long clientId,
            @Valid @ModelAttribute("openAccountForm") OpenAccountForm form,
            BindingResult bindingResult,
            @AuthenticationPrincipal SecurityUser securityUser,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            Client client = clientService.findById(clientId);
            model.addAttribute("pageTitle", "Ouvrir un compte");
            model.addAttribute("activePage", "clients");
            model.addAttribute("client", client);
            model.addAttribute("accountTypes", AccountType.values());
            return "accounts/open";
        }
        try {
            Account account = accountService.openAccount(clientId, form.getType(), securityUser.getUser());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Compte " + account.getAccountNumber() + " ouvert avec succès.");
            return "redirect:/accounts/" + account.getId();
        } catch (BusinessRuleException | ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/clients/" + clientId;
        }
    }

    @PostMapping("/{id}/block")
    public String block(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser securityUser,
            RedirectAttributes redirectAttributes) {
        return changeStatus(id, securityUser, redirectAttributes,
                () -> accountService.block(id, securityUser.getUser()),
                "Compte bloqué.");
    }

    @PostMapping("/{id}/unblock")
    public String unblock(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser securityUser,
            RedirectAttributes redirectAttributes) {
        return changeStatus(id, securityUser, redirectAttributes,
                () -> accountService.unblock(id, securityUser.getUser()),
                "Compte débloqué.");
    }

    @PostMapping("/{id}/close")
    public String close(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser securityUser,
            RedirectAttributes redirectAttributes) {
        return changeStatus(id, securityUser, redirectAttributes,
                () -> accountService.close(id, securityUser.getUser()),
                "Compte clôturé.");
    }

    private String changeStatus(Long id, SecurityUser securityUser, RedirectAttributes redirectAttributes,
                                java.util.function.Supplier<Account> action, String successMessage) {
        try {
            action.get();
            redirectAttributes.addFlashAttribute("successMessage", successMessage);
        } catch (BusinessRuleException | ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/accounts/" + id;
    }
}
