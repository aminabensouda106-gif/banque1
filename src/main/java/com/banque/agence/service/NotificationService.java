package com.banque.agence.service;

import com.banque.agence.domain.entity.Account;
import com.banque.agence.domain.entity.CheckbookOrder;
import com.banque.agence.domain.entity.Client;
import com.banque.agence.domain.entity.Notification;
import com.banque.agence.domain.entity.Transaction;
import com.banque.agence.domain.entity.User;
import com.banque.agence.domain.enums.CheckbookOrderStatus;
import com.banque.agence.domain.enums.ClientStatus;
import com.banque.agence.domain.enums.NotificationType;
import com.banque.agence.domain.enums.UserRole;
import com.banque.agence.repository.NotificationRepository;
import com.banque.agence.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public long countUnread(User user) {
        return notificationRepository.countByRecipientIdAndReadFalse(user.getId());
    }

    @Transactional(readOnly = true)
    public long countUnread(Client client) {
        return notificationRepository.countByClientRecipientIdAndReadFalse(client.getId());
    }

    @Transactional(readOnly = true)
    public Page<Notification> listForUser(User user, Pageable pageable) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(user.getId(), pageable);
    }

    @Transactional(readOnly = true)
    public Page<Notification> listForClient(Client client, Pageable pageable) {
        return notificationRepository.findByClientRecipientIdOrderByCreatedAtDesc(client.getId(), pageable);
    }

    @Transactional
    public void markAsRead(Long notificationId, User user) {
        Notification notification = notificationRepository.findByIdAndRecipientId(notificationId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Notification introuvable."));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAsRead(Long notificationId, Client client) {
        Notification notification = notificationRepository.findByIdAndClientRecipientId(notificationId, client.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Notification introuvable."));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(User user) {
        notificationRepository.markAllAsReadForUser(user.getId());
    }

    @Transactional
    public void markAllAsRead(Client client) {
        notificationRepository.markAllAsReadForClient(client.getId());
    }

    @Transactional
    public void notifyChefsAboutNewCheckbookOrder(CheckbookOrder order) {
        String link = "/checkbook-orders/" + order.getId();
        String message = order.getOrderNumber() + " — compte " + order.getAccount().getAccountNumber()
                + " (" + order.getSheetCount().getLabel() + ")";
        List<User> chefs = userRepository.findByRoleAndEnabledTrue(UserRole.CHEF_AGENCE);
        for (User chef : chefs) {
            createForUser(chef, NotificationType.CHECKBOOK_ORDER_CREATED,
                    "Nouvelle commande de chéquier", message, link);
        }
        notifyClientAboutCheckbookRequested(order);
    }

    @Transactional
    public void notifyRequesterAboutStatusChange(CheckbookOrder order,
                                                 CheckbookOrderStatus previousStatus,
                                                 User actor) {
        User requester = order.getRequestedBy();
        if (!requester.getId().equals(actor.getId())) {
            String link = "/checkbook-orders/" + order.getId();
            String message = order.getOrderNumber() + " : " + previousStatus.getLabel()
                    + " → " + order.getStatus().getLabel();
            createForUser(requester, NotificationType.CHECKBOOK_ORDER_STATUS_CHANGED,
                    "Commande chéquier mise à jour", message, link);
        }
        notifyClientAboutCheckbookStatusChange(order, previousStatus);
    }

    @Transactional
    public void notifyClientAboutDeposit(Transaction tx, Account account) {
        notifyClient(account.getClient(), NotificationType.CLIENT_DEPOSIT,
                "Dépôt enregistré",
                formatAmount(tx.getAmount()) + " MAD crédités sur " + account.getAccountNumber(),
                "/portal/transactions/" + tx.getId() + "/receipt");
    }

    @Transactional
    public void notifyClientAboutWithdrawal(Transaction tx, Account account) {
        notifyClient(account.getClient(), NotificationType.CLIENT_WITHDRAWAL,
                "Retrait effectué",
                formatAmount(tx.getAmount()) + " MAD débités sur " + account.getAccountNumber(),
                "/portal/transactions/" + tx.getId() + "/receipt");
    }

    @Transactional
    public void notifyClientAboutTransferSent(Transaction tx, Account source, Account destination) {
        notifyClient(source.getClient(), NotificationType.CLIENT_TRANSFER_SENT,
                "Virement émis",
                formatAmount(tx.getAmount()) + " MAD vers " + destination.getAccountNumber(),
                "/portal/transactions/" + tx.getId() + "/receipt");
    }

    @Transactional
    public void notifyClientAboutTransferReceived(Transaction tx, Account source, Account destination) {
        if (!source.getClient().getId().equals(destination.getClient().getId())) {
            notifyClient(destination.getClient(), NotificationType.CLIENT_TRANSFER_RECEIVED,
                    "Virement reçu",
                    formatAmount(tx.getAmount()) + " MAD depuis " + source.getAccountNumber(),
                    "/portal/transactions/" + tx.getId() + "/receipt");
        }
    }

    @Transactional
    public void notifyClientAboutBillPayment(Transaction tx, Account account, String providerName, String reference) {
        notifyClient(account.getClient(), NotificationType.CLIENT_BILL_PAYMENT,
                "Facture payée avec succès",
                providerName + " — réf. " + reference + " — " + formatAmount(tx.getAmount()) + " MAD",
                "/portal/transactions/" + tx.getId() + "/receipt");
    }

    private void notifyClientAboutCheckbookRequested(CheckbookOrder order) {
        notifyClient(order.getClient(), NotificationType.CLIENT_CHECKBOOK_REQUESTED,
                "Commande de chéquier enregistrée",
                order.getOrderNumber() + " — compte " + order.getAccount().getAccountNumber()
                        + " — en attente de traitement",
                "/portal/checkbook-orders");
    }

    private void notifyClientAboutCheckbookStatusChange(CheckbookOrder order, CheckbookOrderStatus previousStatus) {
        String detail = switch (order.getStatus()) {
            case PROCESSING -> "Votre commande est en cours de traitement.";
            case DELIVERED -> "Votre chéquier est disponible au retrait en agence.";
            case CANCELLED -> "Votre commande a été annulée.";
            default -> order.getStatus().getLabel();
        };
        notifyClient(order.getClient(), NotificationType.CLIENT_CHECKBOOK_STATUS,
                "Mise à jour commande chéquier",
                order.getOrderNumber() + " : " + previousStatus.getLabel() + " → " + order.getStatus().getLabel()
                        + " — " + detail,
                "/portal/checkbook-orders");
    }

    private void notifyClient(Client client, NotificationType type, String title, String message, String link) {
        if (!canNotifyClient(client)) {
            return;
        }
        createForClient(client, type, title, message, link);
    }

    private boolean canNotifyClient(Client client) {
        return client != null
                && client.isPortalEnabled()
                && client.getStatus() == ClientStatus.ACTIVE
                && client.getPasswordHash() != null;
    }

    private void createForUser(User recipient, NotificationType type, String title, String message, String link) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setLink(link);
        notificationRepository.save(notification);
    }

    private void createForClient(Client client, NotificationType type, String title, String message, String link) {
        Notification notification = new Notification();
        notification.setClientRecipient(client);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setLink(link);
        notificationRepository.save(notification);
    }

    private String formatAmount(BigDecimal amount) {
        return amount.stripTrailingZeros().toPlainString();
    }
}
