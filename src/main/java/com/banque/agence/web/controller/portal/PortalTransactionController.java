package com.banque.agence.web.controller.portal;

import com.banque.agence.domain.enums.TransactionType;
import com.banque.agence.security.SecurityClient;
import com.banque.agence.service.BillPaymentService;
import com.banque.agence.service.ClientPortalService;
import com.banque.agence.web.dto.TransactionFilterForm;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/portal/transactions")
public class PortalTransactionController {

    private static final int PAGE_SIZE = 15;

    private final ClientPortalService clientPortalService;
    private final BillPaymentService billPaymentService;

    public PortalTransactionController(ClientPortalService clientPortalService,
                                       BillPaymentService billPaymentService) {
        this.clientPortalService = clientPortalService;
        this.billPaymentService = billPaymentService;
    }

    @GetMapping
    public String list(
            @AuthenticationPrincipal SecurityClient securityClient,
            @ModelAttribute TransactionFilterForm filter,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {
        var client = securityClient.getClient();
        var transactions = clientPortalService.listTransactions(
                client,
                filter.getType(),
                filter.getFromDate(),
                filter.getToDate(),
                PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "executedAt"))
        );

        model.addAttribute("pageTitle", "Historique des opérations");
        model.addAttribute("activePage", "transactions");
        model.addAttribute("transactions", transactions);
        model.addAttribute("filter", filter);
        model.addAttribute("transactionTypes", TransactionType.values());
        return "portal/transactions";
    }

    @GetMapping("/{id}/receipt")
    public String receipt(@AuthenticationPrincipal SecurityClient securityClient,
                          @PathVariable Long id,
                          Model model) {
        var transaction = clientPortalService.getOwnedTransaction(securityClient.getClient(), id);
        model.addAttribute("transaction", transaction);
        billPaymentService.findByTransactionId(id).ifPresent(bp -> model.addAttribute("billPayment", bp));
        return "transactions/receipt";
    }
}
