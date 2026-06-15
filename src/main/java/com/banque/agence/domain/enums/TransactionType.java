package com.banque.agence.domain.enums;

public enum TransactionType {
    DEPOT,
    RETRAIT,
    VIREMENT;

    public String getLabel() {
        return switch (this) {
            case DEPOT -> "Dépôt";
            case RETRAIT -> "Retrait";
            case VIREMENT -> "Virement";
        };
    }
}
