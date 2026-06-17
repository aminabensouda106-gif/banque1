package com.banque.agence.domain.enums;

public enum NotificationType {
    CHECKBOOK_ORDER_CREATED,
    CHECKBOOK_ORDER_STATUS_CHANGED,
    CLIENT_DEPOSIT,
    CLIENT_WITHDRAWAL,
    CLIENT_TRANSFER_SENT,
    CLIENT_TRANSFER_RECEIVED,
    CLIENT_BILL_PAYMENT,
    CLIENT_CHECKBOOK_REQUESTED,
    CLIENT_CHECKBOOK_STATUS;

    public String getLabel() {
        return switch (this) {
            case CHECKBOOK_ORDER_CREATED -> "Nouvelle commande de chéquier";
            case CHECKBOOK_ORDER_STATUS_CHANGED -> "Mise à jour commande chéquier";
            case CLIENT_DEPOSIT -> "Dépôt sur votre compte";
            case CLIENT_WITHDRAWAL -> "Retrait sur votre compte";
            case CLIENT_TRANSFER_SENT -> "Virement émis";
            case CLIENT_TRANSFER_RECEIVED -> "Virement reçu";
            case CLIENT_BILL_PAYMENT -> "Paiement de facture";
            case CLIENT_CHECKBOOK_REQUESTED -> "Commande de chéquier";
            case CLIENT_CHECKBOOK_STATUS -> "Suivi chéquier";
        };
    }
}
