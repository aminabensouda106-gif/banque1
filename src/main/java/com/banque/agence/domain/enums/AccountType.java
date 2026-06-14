package com.banque.agence.domain.enums;

public enum AccountType {
    COURANT,
    EPARGNE,
    PROFESSIONNEL;

    public String getLabel() {
        return switch (this) {
            case COURANT -> "Compte courant";
            case EPARGNE -> "Compte épargne";
            case PROFESSIONNEL -> "Compte professionnel";
        };
    }
}
