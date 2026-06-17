package com.banque.agence.service;

import com.banque.agence.audit.AuditService;
import com.banque.agence.domain.entity.Account;
import com.banque.agence.domain.entity.Transaction;
import com.banque.agence.domain.entity.User;
import com.banque.agence.domain.enums.AccountStatus;
import com.banque.agence.domain.enums.TransactionType;
import com.banque.agence.repository.AccountRepository;
import com.banque.agence.repository.TransactionRepository;
import com.banque.agence.repository.TransactionSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
public class TransactionService {

    private static final String ENTITY_TYPE = "Transaction";

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;

    public TransactionService(TransactionRepository transactionRepository,
                              AccountRepository accountRepository,
                              AuditService auditService,
                              NotificationService notificationService) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public Page<Transaction> search(TransactionType type,
                                    String accountNumber,
                                    Long userId,
                                    LocalDate fromDate,
                                    LocalDate toDate,
                                    Pageable pageable) {
        Instant from = fromDate == null ? null : fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant to = toDate == null ? null : toDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        String accountFilter = accountNumber == null ? "" : accountNumber.trim();
        return transactionRepository.findAll(
                TransactionSpecifications.withFilters(
                        type,
                        accountFilter.isEmpty() ? null : accountFilter,
                        userId,
                        from,
                        to
                ),
                pageable
        );
    }

    @Transactional(readOnly = true)
    public Transaction findById(Long id) {
        return transactionRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Opération introuvable."));
    }

    @Transactional(readOnly = true)
    public List<Account> listOperableAccounts() {
        return accountRepository.findAllByStatusWithClient(AccountStatus.ACTIVE);
    }

    @Transactional
    public TransactionResult deposit(Long accountId, BigDecimal amount, User user, String description) {
        amount = normalizeAndValidateAmount(amount);
        Account account = loadOperableAccount(accountId);
        MonetaryLimits.validateBalanceAfterCredit(account.getBalance(), amount);
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        Transaction tx = persistTransaction(TransactionType.DEPOT, amount, null, account, user, description);
        auditService.log(user, "TRANSACTION_DEPOT", ENTITY_TYPE, tx.getId(),
                amount + " MAD sur " + account.getAccountNumber());
        notificationService.notifyClientAboutDeposit(tx, account);
        return new TransactionResult(tx.getId(), account.getBalance(), account.getAccountNumber());
    }

    @Transactional
    public TransactionResult withdraw(Long accountId, BigDecimal amount, User user, String description) {
        amount = normalizeAndValidateAmount(amount);
        Account account = loadOperableAccount(accountId);
        if (account.getBalance().compareTo(amount) < 0) {
            throw new BusinessRuleException("Solde insuffisant pour effectuer ce retrait.");
        }
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        Transaction tx = persistTransaction(TransactionType.RETRAIT, amount, account, null, user, description);
        auditService.log(user, "TRANSACTION_RETRAIT", ENTITY_TYPE, tx.getId(),
                amount + " MAD depuis " + account.getAccountNumber());
        notificationService.notifyClientAboutWithdrawal(tx, account);
        return new TransactionResult(tx.getId(), account.getBalance(), account.getAccountNumber());
    }

    @Transactional
    public TransferResult transfer(Long sourceId, Long destinationId, BigDecimal amount, User user, String description) {
        amount = normalizeAndValidateAmount(amount);
        if (sourceId.equals(destinationId)) {
            throw new BusinessRuleException("Les comptes source et destination doivent être différents.");
        }

        Account source = loadOperableAccount(sourceId);
        Account destination = loadOperableAccount(destinationId);

        if (source.getBalance().compareTo(amount) < 0) {
            throw new BusinessRuleException("Solde insuffisant pour effectuer ce virement.");
        }

        MonetaryLimits.validateBalanceAfterCredit(destination.getBalance(), amount);
        source.setBalance(source.getBalance().subtract(amount));
        destination.setBalance(destination.getBalance().add(amount));
        accountRepository.save(source);
        accountRepository.save(destination);

        Transaction tx = persistTransaction(TransactionType.VIREMENT, amount, source, destination, user, description);
        auditService.log(user, "TRANSACTION_VIREMENT", ENTITY_TYPE, tx.getId(),
                amount + " MAD de " + source.getAccountNumber() + " vers " + destination.getAccountNumber());
        notificationService.notifyClientAboutTransferSent(tx, source, destination);
        notificationService.notifyClientAboutTransferReceived(tx, source, destination);

        return new TransferResult(
                tx.getId(),
                source.getBalance(),
                destination.getBalance(),
                source.getAccountNumber(),
                destination.getAccountNumber()
        );
    }

    @Transactional(readOnly = true)
    public Page<Transaction> searchForClient(Long clientId,
                                             TransactionType type,
                                             LocalDate fromDate,
                                             LocalDate toDate,
                                             Pageable pageable) {
        Instant from = fromDate == null ? null : fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant to = toDate == null ? null : toDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        return transactionRepository.findAll(
                TransactionSpecifications.forClient(clientId, type, from, to),
                pageable
        );
    }

    @Transactional(readOnly = true)
    public boolean involvesClient(Transaction transaction, Long clientId) {
        if (transaction.getSourceAccount() != null
                && transaction.getSourceAccount().getClient().getId().equals(clientId)) {
            return true;
        }
        return transaction.getDestinationAccount() != null
                && transaction.getDestinationAccount().getClient().getId().equals(clientId);
    }

    private Transaction persistTransaction(TransactionType type,
                                           BigDecimal amount,
                                           Account source,
                                           Account destination,
                                           User user,
                                           String description) {
        Transaction tx = new Transaction();
        tx.setType(type);
        tx.setAmount(MonetaryLimits.normalize(amount));
        tx.setSourceAccount(source);
        tx.setDestinationAccount(destination);
        tx.setExecutedBy(user);
        if (description != null && !description.isBlank()) {
            tx.setDescription(description.trim());
        }
        return transactionRepository.save(tx);
    }

    private Account loadOperableAccount(Long accountId) {
        Account account = accountRepository.findByIdWithClient(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Compte introuvable."));
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessRuleException("Les opérations ne sont autorisées que sur un compte actif.");
        }
        return account;
    }

    private BigDecimal normalizeAndValidateAmount(BigDecimal amount) {
        MonetaryLimits.validatePositiveAmount(amount);
        return MonetaryLimits.normalize(amount);
    }
}
