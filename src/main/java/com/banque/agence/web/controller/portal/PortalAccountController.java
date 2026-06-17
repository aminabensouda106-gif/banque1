package com.banque.agence.web.controller.portal;

import com.banque.agence.security.SecurityClient;
import com.banque.agence.service.ClientPortalService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/portal/accounts")
public class PortalAccountController {

    private final ClientPortalService clientPortalService;

    public PortalAccountController(ClientPortalService clientPortalService) {
        this.clientPortalService = clientPortalService;
    }

    @GetMapping
    public String list(@AuthenticationPrincipal SecurityClient securityClient, Model model) {
        model.addAttribute("pageTitle", "Mes comptes");
        model.addAttribute("activePage", "accounts");
        model.addAttribute("accounts", clientPortalService.listAccounts(securityClient.getClient()));
        return "portal/accounts";
    }
}
