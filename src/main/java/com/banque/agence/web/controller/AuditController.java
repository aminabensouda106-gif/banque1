package com.banque.agence.web.controller;

import com.banque.agence.audit.AuditService;
import com.banque.agence.domain.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/audit")
@PreAuthorize("hasAnyRole('ADMIN', 'CHEF_AGENCE')")
public class AuditController {

    private static final int PAGE_SIZE = 20;

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public String list(
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {
        Page<AuditLog> logs = auditService.list(
                PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "performedAt"))
        );
        model.addAttribute("pageTitle", "Journal d'audit");
        model.addAttribute("activePage", "audit");
        model.addAttribute("logs", logs);
        return "admin/audit/list";
    }
}
