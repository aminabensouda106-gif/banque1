package com.banque.agence.service;

import com.banque.agence.audit.AuditService;
import com.banque.agence.domain.entity.Account;
import com.banque.agence.domain.entity.BillPayment;
import com.banque.agence.domain.entity.BillProvider;
import com.banque.agence.domain.entity.Transaction;
import com.banque.agence.domain.entity.User;
import com.banque.agence.domain.enums.AccountStatus;
import com.banque.agence.domain.enums.TransactionType;
import com.banque.agence.repository.AccountRepository;
import com.banque.agence.repository.BillPaymentRepository;
import com.banque.agence.repository.BillProviderRepository;
import com.banque.agence.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class BillPaymentService {

    private static final String ENTITY_TYPE = "BillPayment";

    private final BillProviderRepository billProviderRepository;
    private final BillPaymentRepository billPaymentRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;

    public BillPaymentService(BillProviderRepository billProviderRepository,
                              BillPaymentRepository billPaymentRepository,
                              AccountRepository accountRepository,
                              TransactionRepository transactionRepository,
                              AuditService auditService,
                              NotificationService notificationService) {
        this.billProviderRepository = billProviderRepository;
        this.billPaymentRepository = billPaymentRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<BillProvider> listActiveProviders() {
        return billProviderRepository.findAllByActiveTrueOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public Optional<BillPayment> findByTransactionId(Long transactionId) {
        return billPaymentRepository.findWithDetailsByTransactionId(transactionId);
    }

    @Transactional
    public TransactionResult payBill(Long accountId,
                                     Long providerId,
                                     String clientReference,
                                     BigDecimal amount,
                                     User user) {
        amount = normalizeAndValidateAmount(amount);
        String reference = normalizeReference(clientReference);

        Account account = loadOperableAccount(accountId);
        BillProvider provider = billProviderRepository.findByIdAndActiveTrue(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Facturier introuvable."));

        if (account.getBalance().compareTo(amount) < 0) {
            throw new BusinessRuleException("Solde insuffisant pour effectuer ce paiement.");
        }

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        Transaction tx = new Transaction();
        tx.setType(TransactionType.PAIEMENT_FACTURE);
        tx.setAmount(amount);
        tx.setSourceAccount(account);
        tx.setExecutedBy(user);
        tx.setDescription(provider.getName() + " — réf. " + reference);
        transactionRepository.save(tx);

        BillPayment payment = new BillPayment();
        payment.setAccount(account);
        payment.setBillProvider(provider);
        payment.setClientReference(reference);
        payment.setAmount(amount);
        payment.setTransaction(tx);
        billPaymentRepository.save(payment);

        auditService.log(user, "BILL_PAYMENT_CREATED", ENTITY_TYPE, payment.getId(),
                amount + " MAD — " + provider.getName() + " — réf. " + reference
                        + " — compte " + account.getAccountNumber());
        notificationService.notifyClientAboutBillPayment(tx, account, provider.getName(), reference);

        return new TransactionResult(tx.getId(), account.getBalance(), account.getAccountNumber());
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

    private String normalizeReference(String clientReference) {
        if (clientReference == null || clientReference.isBlank()) {
            throw new BusinessRuleException("La référence client est obligatoire.");
        }
        return clientReference.trim();
    }
}
