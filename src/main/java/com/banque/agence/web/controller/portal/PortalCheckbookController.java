package com.banque.agence.web.controller.portal;

import com.banque.agence.security.SecurityClient;
import com.banque.agence.service.ClientPortalService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/portal/checkbook-orders")
public class PortalCheckbookController {

    private final ClientPortalService clientPortalService;

    public PortalCheckbookController(ClientPortalService clientPortalService) {
        this.clientPortalService = clientPortalService;
    }

    @GetMapping
    public String list(@AuthenticationPrincipal SecurityClient securityClient, Model model) {
        model.addAttribute("pageTitle", "Mes commandes de chéquier");
        model.addAttribute("activePage", "checkbook-orders");
        model.addAttribute("orders", clientPortalService.listCheckbookOrders(securityClient.getClient()));
        return "portal/checkbook-orders";
    }
}
