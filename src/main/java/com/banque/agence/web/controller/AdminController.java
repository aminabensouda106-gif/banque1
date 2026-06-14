package com.banque.agence.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("pageTitle", "Gestion des utilisateurs");
        model.addAttribute("activePage", "users");
        return "admin/users";
    }
}
