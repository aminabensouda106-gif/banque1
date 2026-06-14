package com.banque.agence.web.controller;

import com.banque.agence.security.SecurityUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal SecurityUser securityUser, Model model) {
        var user = securityUser.getUser();
        model.addAttribute("pageTitle", "Tableau de bord");
        model.addAttribute("activePage", "dashboard");
        model.addAttribute("fullName", user.getFullName());
        model.addAttribute("roleLabel", user.getRole().getLabel());
        return "dashboard";
    }
}
