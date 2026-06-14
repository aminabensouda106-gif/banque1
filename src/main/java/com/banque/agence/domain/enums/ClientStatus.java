package com.banque.agence.domain.enums;

public enum ClientStatus {
    ACTIVE,
    SUSPENDED,
    INACTIVE;

    public String getLabel() {
        return switch (this) {
            case ACTIVE -> "Actif";
            case SUSPENDED -> "Suspendu";
            case INACTIVE -> "Inactif";
        };
    }

    public String getBadgeClass() {
        return switch (this) {
            case ACTIVE -> "success";
            case SUSPENDED -> "warning";
            case INACTIVE -> "secondary";
        };
    }
}
