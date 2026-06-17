package com.banque.agence.web.controller.portal;

import com.banque.agence.security.SecurityClient;
import com.banque.agence.service.ClientPortalService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/portal")
public class PortalDashboardController {

    private final ClientPortalService clientPortalService;

    public PortalDashboardController(ClientPortalService clientPortalService) {
        this.clientPortalService = clientPortalService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal SecurityClient securityClient, Model model) {
        var client = securityClient.getClient();
        var accounts = clientPortalService.listAccounts(client);
        var transactions = clientPortalService.listTransactions(
                client, null, null, null,
                org.springframework.data.domain.PageRequest.of(0, 5,
                        org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "executedAt"))
        );

        model.addAttribute("pageTitle", "Mon espace client");
        model.addAttribute("activePage", "dashboard");
        model.addAttribute("client", client);
        model.addAttribute("accounts", accounts);
        model.addAttribute("recentTransactions", transactions.getContent());
        model.addAttribute("pendingCheckbooks", clientPortalService.countPendingCheckbooks(client));
        model.addAttribute("unreadNotifications", clientPortalService.countUnreadNotifications(client));
        return "portal/dashboard";
    }
}
