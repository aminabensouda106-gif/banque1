package com.banque.agence.domain.enums;

public enum AccountStatus {
    ACTIVE,
    BLOCKED,
    CLOSED;

    public String getLabel() {
        return switch (this) {
            case ACTIVE -> "Actif";
            case BLOCKED -> "Bloqué";
            case CLOSED -> "Clôturé";
        };
    }

    public String getBadgeClass() {
        return switch (this) {
            case ACTIVE -> "success";
            case BLOCKED -> "warning";
            case CLOSED -> "secondary";
        };
    }
}
