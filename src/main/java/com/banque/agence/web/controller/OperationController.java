package com.banque.agence.web.controller;

import com.banque.agence.domain.entity.Account;
import com.banque.agence.domain.entity.BillProvider;
import com.banque.agence.security.SecurityUser;
import com.banque.agence.service.BillPaymentService;
import com.banque.agence.service.BusinessRuleException;
import com.banque.agence.service.ResourceNotFoundException;
import com.banque.agence.service.TransactionService;
import com.banque.agence.web.dto.BillPaymentForm;
import com.banque.agence.web.dto.OperationForm;
import com.banque.agence.web.dto.TransferForm;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/operations")
public class OperationController {

    private final TransactionService transactionService;
    private final BillPaymentService billPaymentService;

    public OperationController(TransactionService transactionService,
                               BillPaymentService billPaymentService) {
        this.transactionService = transactionService;
        this.billPaymentService = billPaymentService;
    }

    @ModelAttribute("activeAccounts")
    public List<Account> activeAccounts() {
        return transactionService.listOperableAccounts();
    }

    @ModelAttribute("activeProviders")
    public List<BillProvider> activeProviders() {
        return billPaymentService.listActiveProviders();
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("pageTitle", "Opérations");
        model.addAttribute("activePage", "operations");
        return "operations/index";
    }

    @GetMapping("/deposit")
    public String depositForm(
            @RequestParam(value = "accountId", required = false) Long accountId,
            Model model) {
        OperationForm form = new OperationForm();
        form.setAccountId(accountId);
        return prepareOperationForm(model, form, "Dépôt", "Effectuer le dépôt", "operations/deposit");
    }

    @PostMapping("/deposit")
    public String deposit(
            @Valid @ModelAttribute("operationForm") OperationForm form,
            BindingResult bindingResult,
            @AuthenticationPrincipal SecurityUser securityUser,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return prepareOperationForm(model, form, "Dépôt", "Effectuer le dépôt", "operations/deposit");
        }
        try {
            var result = transactionService.deposit(
                    form.getAccountId(), form.getAmount(), securityUser.getUser(), form.getDescription());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Dépôt de " + form.getAmount() + " MAD effectué. Nouveau solde : "
                            + result.newBalance() + " MAD (" + result.accountNumber() + ").");
            return "redirect:/transactions";
        } catch (BusinessRuleException | ResourceNotFoundException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return prepareOperationForm(model, form, "Dépôt", "Effectuer le dépôt", "operations/deposit");
        }
    }

    @GetMapping("/withdraw")
    public String withdrawForm(
            @RequestParam(value = "accountId", required = false) Long accountId,
            Model model) {
        OperationForm form = new OperationForm();
        form.setAccountId(accountId);
        return prepareOperationForm(model, form, "Retrait", "Effectuer le retrait", "operations/withdraw");
    }

    @PostMapping("/withdraw")
    public String withdraw(
            @Valid @ModelAttribute("operationForm") OperationForm form,
            BindingResult bindingResult,
            @AuthenticationPrincipal SecurityUser securityUser,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return prepareOperationForm(model, form, "Retrait", "Effectuer le retrait", "operations/withdraw");
        }
        try {
            var result = transactionService.withdraw(
                    form.getAccountId(), form.getAmount(), securityUser.getUser(), form.getDescription());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Retrait de " + form.getAmount() + " MAD effectué. Nouveau solde : "
                            + result.newBalance() + " MAD (" + result.accountNumber() + ").");
            return "redirect:/transactions";
        } catch (BusinessRuleException | ResourceNotFoundException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return prepareOperationForm(model, form, "Retrait", "Effectuer le retrait", "operations/withdraw");
        }
    }

    @GetMapping("/transfer")
    public String transferForm(Model model) {
        model.addAttribute("pageTitle", "Virement");
        model.addAttribute("activePage", "operations");
        model.addAttribute("transferForm", new TransferForm());
        model.addAttribute("submitLabel", "Effectuer le virement");
        return "operations/transfer";
    }

    @PostMapping("/transfer")
    public String transfer(
            @Valid @ModelAttribute("transferForm") TransferForm form,
            BindingResult bindingResult,
            @AuthenticationPrincipal SecurityUser securityUser,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Virement");
            model.addAttribute("activePage", "operations");
            model.addAttribute("submitLabel", "Effectuer le virement");
            return "operations/transfer";
        }
        try {
            var result = transactionService.transfer(
                    form.getSourceAccountId(), form.getDestinationAccountId(),
                    form.getAmount(), securityUser.getUser(), form.getDescription());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Virement de " + form.getAmount() + " MAD effectué de "
                            + result.sourceAccountNumber() + " vers " + result.destinationAccountNumber()
                            + ". Nouveaux soldes : " + result.sourceBalance() + " / " + result.destinationBalance() + " MAD.");
            return "redirect:/transactions";
        } catch (BusinessRuleException | ResourceNotFoundException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("pageTitle", "Virement");
            model.addAttribute("activePage", "operations");
            model.addAttribute("submitLabel", "Effectuer le virement");
            return "operations/transfer";
        }
    }

    @GetMapping("/bill-payment")
    public String billPaymentForm(
            @RequestParam(value = "accountId", required = false) Long accountId,
            Model model) {
        BillPaymentForm form = new BillPaymentForm();
        form.setAccountId(accountId);
        return prepareBillPaymentForm(model, form);
    }

    @PostMapping("/bill-payment")
    public String billPayment(
            @Valid @ModelAttribute("billPaymentForm") BillPaymentForm form,
            BindingResult bindingResult,
            @AuthenticationPrincipal SecurityUser securityUser,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return prepareBillPaymentForm(model, form);
        }
        try {
            var result = billPaymentService.payBill(
                    form.getAccountId(),
                    form.getProviderId(),
                    form.getClientReference(),
                    form.getAmount(),
                    securityUser.getUser());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Paiement de " + form.getAmount() + " MAD effectué. Nouveau solde : "
                            + result.newBalance() + " MAD (" + result.accountNumber() + ").");
            return "redirect:/transactions/" + result.transactionId() + "/receipt";
        } catch (BusinessRuleException | ResourceNotFoundException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return prepareBillPaymentForm(model, form);
        }
    }

    private String prepareBillPaymentForm(Model model, BillPaymentForm form) {
        model.addAttribute("pageTitle", "Paiement de facture");
        model.addAttribute("activePage", "operations");
        model.addAttribute("billPaymentForm", form);
        model.addAttribute("submitLabel", "Effectuer le paiement");
        return "operations/bill-payment";
    }

    private String prepareOperationForm(Model model, OperationForm form, String title,
                                        String submitLabel, String view) {
        model.addAttribute("pageTitle", title);
        model.addAttribute("activePage", "operations");
        model.addAttribute("operationForm", form);
        model.addAttribute("submitLabel", submitLabel);
        return view;
    }
}
