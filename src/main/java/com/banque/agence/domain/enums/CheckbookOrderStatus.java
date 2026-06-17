package com.banque.agence.domain.enums;

public enum CheckbookOrderStatus {
    PENDING,
    PROCESSING,
    DELIVERED,
    CANCELLED;

    public String getLabel() {
        return switch (this) {
            case PENDING -> "En attente";
            case PROCESSING -> "En cours";
            case DELIVERED -> "Livrée";
            case CANCELLED -> "Annulée";
        };
    }

    public String getBadgeClass() {
        return switch (this) {
            case PENDING -> "warning";
            case PROCESSING -> "info";
            case DELIVERED -> "success";
            case CANCELLED -> "secondary";
        };
    }
}
