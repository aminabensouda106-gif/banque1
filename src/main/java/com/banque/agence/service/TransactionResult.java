package com.banque.agence.service;

import java.math.BigDecimal;

public record TransactionResult(Long transactionId, BigDecimal newBalance, String accountNumber) {
}
