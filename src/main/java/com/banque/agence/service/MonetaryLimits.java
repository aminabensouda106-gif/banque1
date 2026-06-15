package com.banque.agence.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MonetaryLimits {

    /** Matches PostgreSQL NUMERIC(19, 4): max 15 digits before the decimal point. */
    public static final BigDecimal MAX_VALUE = new BigDecimal("999999999999999.9999");

    private MonetaryLimits() {
    }

    public static BigDecimal normalize(BigDecimal amount) {
        return amount.setScale(4, RoundingMode.HALF_UP);
    }

    public static void validatePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Le montant doit être strictement positif.");
        }
        if (amount.compareTo(MAX_VALUE) > 0) {
            throw new BusinessRuleException(
                    "Le montant dépasse la limite autorisée (999 999 999 999 999,9999 MAD maximum).");
        }
    }

    public static void validateBalanceAfterCredit(BigDecimal currentBalance, BigDecimal amount) {
        BigDecimal newBalance = currentBalance.add(amount);
        if (newBalance.compareTo(MAX_VALUE) > 0) {
            throw new BusinessRuleException("Le solde du compte dépasserait la limite autorisée après cette opération.");
        }
    }
}
