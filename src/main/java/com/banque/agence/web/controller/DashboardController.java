package com.banque.agence.web.controller;

import com.banque.agence.security.SecurityUser;
import com.banque.agence.service.DashboardService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal SecurityUser securityUser, Model model) {
        var user = securityUser.getUser();
        model.addAttribute("pageTitle", "Tableau de bord");
        model.addAttribute("activePage", "dashboard");
        model.addAttribute("fullName", user.getFullName());
        model.addAttribute("roleLabel", user.getRole().getLabel());
        model.addAttribute("stats", dashboardService.getStats());
        model.addAttribute("recentTransactions", dashboardService.getRecentTransactions());
        return "dashboard";
    }
}
