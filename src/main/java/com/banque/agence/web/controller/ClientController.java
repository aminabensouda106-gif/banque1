package com.banque.agence.web.controller;

import com.banque.agence.domain.entity.Client;
import com.banque.agence.domain.enums.ClientStatus;
import com.banque.agence.security.SecurityUser;
import com.banque.agence.service.AccountService;
import com.banque.agence.service.ClientService;
import com.banque.agence.service.DuplicateResourceException;
import com.banque.agence.service.ResourceNotFoundException;
import com.banque.agence.web.dto.ClientForm;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Controller
@RequestMapping("/clients")
public class ClientController {

    private static final int PAGE_SIZE = 10;

    private final ClientService clientService;
    private final AccountService accountService;

    public ClientController(ClientService clientService, AccountService accountService) {
        this.clientService = clientService;
        this.accountService = accountService;
    }

    @GetMapping
    public String list(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {
        Page<Client> clients = clientService.search(
                query,
                PageRequest.of(page, PAGE_SIZE, Sort.by("lastName", "firstName"))
        );
        model.addAttribute("pageTitle", "Clients");
        model.addAttribute("activePage", "clients");
        model.addAttribute("clients", clients);
        model.addAttribute("query", query == null ? "" : query);
        return "clients/list";
    }

    @GetMapping("/search")
    public String search(@RequestParam(value = "q", required = false) String query) {
        return "redirect:/clients?q=" + (query == null ? "" : query);
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("pageTitle", "Nouveau client");
        model.addAttribute("activePage", "clients");
        model.addAttribute("clientForm", new ClientForm());
        model.addAttribute("formAction", "/clients");
        model.addAttribute("submitLabel", "Créer le client");
        return "clients/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("clientForm") ClientForm clientForm,
            BindingResult bindingResult,
            @AuthenticationPrincipal SecurityUser securityUser,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return prepareForm(model, clientForm, "/clients", "Créer le client", "Nouveau client");
        }
        try {
            Client client = clientService.create(clientForm, securityUser.getUser());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Client " + client.getClientNumber() + " créé avec succès.");
            return "redirect:/clients/" + client.getId();
        } catch (DuplicateResourceException ex) {
            bindingResult.rejectValue("cin", "duplicate.cin", ex.getMessage());
            return prepareForm(model, clientForm, "/clients", "Créer le client", "Nouveau client");
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Client client = clientService.findById(id);
        model.addAttribute("pageTitle", client.getFullName());
        model.addAttribute("activePage", "clients");
        model.addAttribute("client", client);
        model.addAttribute("accounts", accountService.listByClient(id));
        return "clients/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Client client = clientService.findById(id);
        model.addAttribute("pageTitle", "Modifier — " + client.getFullName());
        model.addAttribute("activePage", "clients");
        model.addAttribute("clientForm", ClientForm.fromClient(client));
        model.addAttribute("formAction", "/clients/" + id);
        model.addAttribute("submitLabel", "Enregistrer");
        model.addAttribute("clientId", id);
        return "clients/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("clientForm") ClientForm clientForm,
            BindingResult bindingResult,
            @AuthenticationPrincipal SecurityUser securityUser,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return prepareForm(model, clientForm, "/clients/" + id, "Enregistrer",
                    "Modifier le client", id);
        }
        try {
            Client client = clientService.update(id, clientForm, securityUser.getUser());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Client " + client.getClientNumber() + " mis à jour.");
            return "redirect:/clients/" + id;
        } catch (DuplicateResourceException ex) {
            bindingResult.rejectValue("cin", "duplicate.cin", ex.getMessage());
            return prepareForm(model, clientForm, "/clients/" + id, "Enregistrer",
                    "Modifier le client", id);
        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/clients";
        }
    }

    @PostMapping("/{id}/status")
    public String changeStatus(
            @PathVariable Long id,
            @RequestParam ClientStatus status,
            @AuthenticationPrincipal SecurityUser securityUser,
            RedirectAttributes redirectAttributes) {
        try {
            Client client = clientService.changeStatus(id, status, securityUser.getUser());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Statut mis à jour : " + client.getStatus().getLabel() + ".");
        } catch (ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/clients";
        }
        return "redirect:/clients/" + id;
    }

    private String prepareForm(Model model, ClientForm form, String action, String submitLabel, String title) {
        return prepareForm(model, form, action, submitLabel, title, null);
    }

    private String prepareForm(Model model, ClientForm form, String action, String submitLabel,
                               String title, Long clientId) {
        model.addAttribute("pageTitle", title);
        model.addAttribute("activePage", "clients");
        model.addAttribute("clientForm", form);
        model.addAttribute("formAction", action);
        model.addAttribute("submitLabel", submitLabel);
        if (clientId != null) {
            model.addAttribute("clientId", clientId);
        }
        return "clients/form";
    }
}
