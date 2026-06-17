package com.banque.agence.domain.enums;

public enum TransactionType {
    DEPOT,
    RETRAIT,
    VIREMENT,
    PAIEMENT_FACTURE;

    public String getLabel() {
        return switch (this) {
            case DEPOT -> "Dépôt";
            case RETRAIT -> "Retrait";
            case VIREMENT -> "Virement";
            case PAIEMENT_FACTURE -> "Paiement facture";
        };
    }
}
