package com.banque.agence.service;

import com.banque.agence.domain.entity.Account;
import com.banque.agence.domain.entity.CheckbookOrder;
import com.banque.agence.domain.entity.Client;
import com.banque.agence.domain.entity.Transaction;
import com.banque.agence.domain.enums.CheckbookOrderStatus;
import com.banque.agence.repository.AccountRepository;
import com.banque.agence.repository.CheckbookOrderRepository;
import com.banque.agence.service.NotificationService;
import com.banque.agence.service.TransactionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClientPortalService {

    private final AccountRepository accountRepository;
    private final TransactionService transactionService;
    private final CheckbookOrderRepository checkbookOrderRepository;
    private final NotificationService notificationService;

    public ClientPortalService(AccountRepository accountRepository,
                               TransactionService transactionService,
                               CheckbookOrderRepository checkbookOrderRepository,
                               NotificationService notificationService) {
        this.accountRepository = accountRepository;
        this.transactionService = transactionService;
        this.checkbookOrderRepository = checkbookOrderRepository;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<Account> listAccounts(Client client) {
        return accountRepository.findByClientIdOrderByOpenedAtDesc(client.getId());
    }

    @Transactional(readOnly = true)
    public Page<Transaction> listTransactions(Client client,
                                              com.banque.agence.domain.enums.TransactionType type,
                                              java.time.LocalDate fromDate,
                                              java.time.LocalDate toDate,
                                              Pageable pageable) {
        return transactionService.searchForClient(client.getId(), type, fromDate, toDate, pageable);
    }

    @Transactional(readOnly = true)
    public List<CheckbookOrder> listCheckbookOrders(Client client) {
        return checkbookOrderRepository.findByClientIdOrderByRequestedAtDesc(client.getId());
    }

    @Transactional(readOnly = true)
    public long countPendingCheckbooks(Client client) {
        return listCheckbookOrders(client).stream()
                .filter(o -> o.getStatus() == CheckbookOrderStatus.PENDING
                        || o.getStatus() == CheckbookOrderStatus.PROCESSING)
                .count();
    }

    @Transactional(readOnly = true)
    public Transaction getOwnedTransaction(Client client, Long transactionId) {
        Transaction transaction = transactionService.findById(transactionId);
        if (!transactionService.involvesClient(transaction, client.getId())) {
            throw new ResourceNotFoundException("Opération introuvable.");
        }
        return transaction;
    }

    @Transactional(readOnly = true)
    public long countUnreadNotifications(Client client) {
        return notificationService.countUnread(client);
    }
}
