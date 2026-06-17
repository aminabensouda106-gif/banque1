package com.banque.agence.service;

import com.banque.agence.domain.entity.Transaction;
import com.banque.agence.domain.enums.AccountStatus;
import com.banque.agence.domain.enums.CheckbookOrderStatus;
import com.banque.agence.domain.enums.ClientStatus;
import com.banque.agence.repository.AccountRepository;
import com.banque.agence.repository.CheckbookOrderRepository;
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
    private final CheckbookOrderRepository checkbookOrderRepository;

    public DashboardService(ClientRepository clientRepository,
                            AccountRepository accountRepository,
                            TransactionRepository transactionRepository,
                            CheckbookOrderRepository checkbookOrderRepository) {
        this.clientRepository = clientRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.checkbookOrderRepository = checkbookOrderRepository;
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

        long pendingCheckbooks = checkbookOrderRepository.countByStatus(CheckbookOrderStatus.PENDING);
        long processingCheckbooks = checkbookOrderRepository.countByStatus(CheckbookOrderStatus.PROCESSING);

        return new DashboardStats(
                activeClients,
                activeAccounts,
                todayCount,
                todayAmount != null ? todayAmount : BigDecimal.ZERO,
                pendingCheckbooks,
                processingCheckbooks
        );
    }

    @Transactional(readOnly = true)
    public List<Transaction> getRecentTransactions() {
        return transactionRepository.findTop10ByOrderByExecutedAtDesc();
    }
}
