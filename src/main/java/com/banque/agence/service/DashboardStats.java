package com.banque.agence.service;

import java.math.BigDecimal;

public record DashboardStats(
        long activeClients,
        long activeAccounts,
        long todayTransactionCount,
        BigDecimal todayTransactionAmount
) {
}
