package com.banque.agence.service;

import java.math.BigDecimal;

public record TransferResult(
        Long transactionId,
        BigDecimal sourceBalance,
        BigDecimal destinationBalance,
        String sourceAccountNumber,
        String destinationAccountNumber
) {
}
