package com.banque.agence.web.controller.portal;

import com.banque.agence.security.SecurityClient;
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
@RequestMapping("/portal/notifications")
public class PortalNotificationController {

    private static final int PAGE_SIZE = 20;

    private final NotificationService notificationService;

    public PortalNotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public String list(
            @AuthenticationPrincipal SecurityClient securityClient,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {
        var client = securityClient.getClient();
        model.addAttribute("pageTitle", "Mes notifications");
        model.addAttribute("activePage", "notifications");
        model.addAttribute("notifications", notificationService.listForClient(client, PageRequest.of(page, PAGE_SIZE)));
        model.addAttribute("unreadCount", notificationService.countUnread(client));
        return "portal/notifications";
    }

    @PostMapping("/{id}/read")
    public String markRead(@AuthenticationPrincipal SecurityClient securityClient,
                           @PathVariable Long id,
                           RedirectAttributes redirectAttributes) {
        notificationService.markAsRead(id, securityClient.getClient());
        redirectAttributes.addFlashAttribute("successMessage", "Notification marquée comme lue.");
        return "redirect:/portal/notifications";
    }

    @PostMapping("/read-all")
    public String markAllRead(@AuthenticationPrincipal SecurityClient securityClient,
                              RedirectAttributes redirectAttributes) {
        notificationService.markAllAsRead(securityClient.getClient());
        redirectAttributes.addFlashAttribute("successMessage", "Toutes les notifications ont été marquées comme lues.");
        return "redirect:/portal/notifications";
    }
}
