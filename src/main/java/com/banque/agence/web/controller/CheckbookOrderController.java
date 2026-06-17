package com.banque.agence.web.controller;

import com.banque.agence.domain.entity.Account;
import com.banque.agence.domain.entity.CheckbookOrder;
import com.banque.agence.domain.enums.CheckbookOrderStatus;
import com.banque.agence.domain.enums.CheckbookSheetCount;
import com.banque.agence.security.SecurityUser;
import com.banque.agence.service.BusinessRuleException;
import com.banque.agence.service.CheckbookOrderService;
import com.banque.agence.service.ResourceNotFoundException;
import com.banque.agence.web.dto.CheckbookOrderFilterForm;
import com.banque.agence.web.dto.CheckbookOrderForm;
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

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/checkbook-orders")
public class CheckbookOrderController {

    private static final int PAGE_SIZE = 15;

    private final CheckbookOrderService checkbookOrderService;

    public CheckbookOrderController(CheckbookOrderService checkbookOrderService) {
        this.checkbookOrderService = checkbookOrderService;
    }

    @GetMapping
    public String list(
            @ModelAttribute CheckbookOrderFilterForm filter,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {
        Page<CheckbookOrder> orders = checkbookOrderService.list(
                filter.getStatus(),
                PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "requestedAt"))
        );
        model.addAttribute("pageTitle", "Commandes de chéquier");
        model.addAttribute("activePage", "checkbook-orders");
        model.addAttribute("orders", orders);
        model.addAttribute("filter", filter);
        model.addAttribute("orderStatuses", CheckbookOrderStatus.values());
        return "checkbook-orders/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        CheckbookOrder order = checkbookOrderService.findById(id);
        model.addAttribute("pageTitle", order.getOrderNumber());
        model.addAttribute("activePage", "checkbook-orders");
        model.addAttribute("order", order);
        model.addAttribute("nextStatuses", nextStatuses(order.getStatus()));
        return "checkbook-orders/detail";
    }

    @GetMapping("/new")
    public String requestForm(
            @RequestParam(value = "accountId", required = false) Long accountId,
            Model model) {
        CheckbookOrderForm form = new CheckbookOrderForm();
        form.setAccountId(accountId);
        return prepareRequestForm(model, form);
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("checkbookOrderForm") CheckbookOrderForm form,
            BindingResult bindingResult,
            @AuthenticationPrincipal SecurityUser securityUser,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return prepareRequestForm(model, form);
        }
        try {
            CheckbookOrder order = checkbookOrderService.requestCheckbook(
                    form.getAccountId(), form.getQuantity(), form.getSheetCount(),
                    form.getNotes(), securityUser.getUser());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Commande " + order.getOrderNumber() + " enregistrée (statut : "
                            + order.getStatus().getLabel() + ").");
            return "redirect:/checkbook-orders/" + order.getId();
        } catch (BusinessRuleException | ResourceNotFoundException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return prepareRequestForm(model, form);
        }
    }

    @PostMapping("/{id}/status")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam CheckbookOrderStatus status,
            @AuthenticationPrincipal SecurityUser securityUser,
            RedirectAttributes redirectAttributes) {
        try {
            CheckbookOrder order = checkbookOrderService.updateStatus(id, status, securityUser.getUser());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Commande " + order.getOrderNumber() + " : statut mis à jour (« "
                            + order.getStatus().getLabel() + " »).");
        } catch (BusinessRuleException | ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/checkbook-orders/" + id;
    }

    private String prepareRequestForm(Model model, CheckbookOrderForm form) {
        List<Account> eligibleAccounts = checkbookOrderService.listEligibleAccountsForRequest();
        model.addAttribute("pageTitle", "Commander un chéquier");
        model.addAttribute("activePage", "checkbook-orders");
        model.addAttribute("checkbookOrderForm", form);
        model.addAttribute("eligibleAccounts", eligibleAccounts);
        model.addAttribute("sheetCountOptions", CheckbookSheetCount.values());
        model.addAttribute("submitLabel", "Enregistrer la demande");
        return "checkbook-orders/request";
    }

    private Set<CheckbookOrderStatus> nextStatuses(CheckbookOrderStatus current) {
        return switch (current) {
            case PENDING -> EnumSet.of(CheckbookOrderStatus.PROCESSING, CheckbookOrderStatus.CANCELLED);
            case PROCESSING -> EnumSet.of(CheckbookOrderStatus.DELIVERED, CheckbookOrderStatus.CANCELLED);
            case DELIVERED, CANCELLED -> EnumSet.noneOf(CheckbookOrderStatus.class);
        };
    }
}
