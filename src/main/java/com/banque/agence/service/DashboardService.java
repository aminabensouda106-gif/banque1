package com.banque.agence.service;

import com.banque.agence.domain.entity.Transaction;
import com.banque.agence.domain.enums.AccountStatus;
import com.banque.agence.domain.enums.ClientStatus;
import com.banque.agence.repository.AccountRepository;
import com.banque.agence.repository.ClientRepository;
import com.banque.agence.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
public class DashboardService {

    private final ClientRepository clientRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public DashboardService(ClientRepository clientRepository,
                            AccountRepository accountRepository,
                            TransactionRepository transactionRepository) {
        this.clientRepository = clientRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public DashboardStats getStats() {
        LocalDate today = LocalDate.now();
        Instant from = today.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant to = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        long activeClients = clientRepository.countByStatus(ClientStatus.ACTIVE);
        long activeAccounts = accountRepository.countByStatus(AccountStatus.ACTIVE);
        long todayCount = transactionRepository.countByExecutedAtGreaterThanEqualAndExecutedAtLessThan(from, to);
        BigDecimal todayAmount = transactionRepository.sumAmountByExecutedAtGreaterThanEqualAndExecutedAtLessThan(from, to);

        return new DashboardStats(
                activeClients,
                activeAccounts,
                todayCount,
                todayAmount != null ? todayAmount : BigDecimal.ZERO
        );
    }

    @Transactional(readOnly = true)
    public List<Transaction> getRecentTransactions() {
        return transactionRepository.findTop10ByOrderByExecutedAtDesc();
    }
}
