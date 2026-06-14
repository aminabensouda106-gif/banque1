package com.banque.agence.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Banque Agence");
        model.addAttribute("message", "Application en construction — Phase 2 bootstrap OK");
        return "index";
    }
}
