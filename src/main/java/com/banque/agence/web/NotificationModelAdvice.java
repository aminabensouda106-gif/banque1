package com.banque.agence.web;

import com.banque.agence.security.SecurityClient;
import com.banque.agence.security.SecurityUser;
import com.banque.agence.service.NotificationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class NotificationModelAdvice {

    private final NotificationService notificationService;

    public NotificationModelAdvice(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @ModelAttribute("unreadNotificationCount")
    public long unreadNotificationCount(@AuthenticationPrincipal UserDetails principal) {
        if (principal instanceof SecurityUser securityUser) {
            return notificationService.countUnread(securityUser.getUser());
        }
        if (principal instanceof SecurityClient securityClient) {
            return notificationService.countUnread(securityClient.getClient());
        }
        return 0;
    }
}
