package com.banque.agence.domain.enums;

public enum UserRole {
    ADMIN,
    AGENT,
    CHEF_AGENCE;

    public String getAuthority() {
        return "ROLE_" + name();
    }

    public String getLabel() {
        return switch (this) {
            case ADMIN -> "Administrateur";
            case AGENT -> "Agent bancaire";
            case CHEF_AGENCE -> "Chef d'agence";
        };
    }
}
