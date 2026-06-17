package com.banque.agence.web.controller;

import com.banque.agence.security.SecurityUser;
import com.banque.agence.service.NotificationService;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private static final int PAGE_SIZE = 20;

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public String list(@AuthenticationPrincipal SecurityUser securityUser,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       Model model) {
        var user = securityUser.getUser();
        model.addAttribute("pageTitle", "Notifications");
        model.addAttribute("activePage", "notifications");
        model.addAttribute("notifications", notificationService.listForUser(user, PageRequest.of(page, PAGE_SIZE)));
        model.addAttribute("unreadCount", notificationService.countUnread(user));
        return "notifications/list";
    }

    @PostMapping("/{id}/read")
    public String markAsRead(@PathVariable Long id,
                             @AuthenticationPrincipal SecurityUser securityUser,
                             RedirectAttributes redirectAttributes) {
        notificationService.markAsRead(id, securityUser.getUser());
        redirectAttributes.addFlashAttribute("successMessage", "Notification marquée comme lue.");
        return "redirect:/notifications";
    }

    @PostMapping("/read-all")
    public String markAllAsRead(@AuthenticationPrincipal SecurityUser securityUser,
                                RedirectAttributes redirectAttributes) {
        notificationService.markAllAsRead(securityUser.getUser());
        redirectAttributes.addFlashAttribute("successMessage", "Toutes les notifications ont été marquées comme lues.");
        return "redirect:/notifications";
    }
}
