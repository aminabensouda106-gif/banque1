package com.banque.agence.service;

import com.banque.agence.audit.AuditService;
import com.banque.agence.domain.entity.Account;
import com.banque.agence.domain.entity.CheckbookOrder;
import com.banque.agence.domain.entity.User;
import com.banque.agence.domain.enums.AccountStatus;
import com.banque.agence.domain.enums.AccountType;
import com.banque.agence.domain.enums.CheckbookOrderStatus;
import com.banque.agence.domain.enums.CheckbookSheetCount;
import com.banque.agence.repository.AccountRepository;
import com.banque.agence.repository.CheckbookOrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CheckbookOrderService {

    private static final String ENTITY_TYPE = "CheckbookOrder";

    private static final Map<CheckbookOrderStatus, Set<CheckbookOrderStatus>> ALLOWED_TRANSITIONS = Map.of(
            CheckbookOrderStatus.PENDING, EnumSet.of(CheckbookOrderStatus.PROCESSING, CheckbookOrderStatus.CANCELLED),
            CheckbookOrderStatus.PROCESSING, EnumSet.of(CheckbookOrderStatus.DELIVERED, CheckbookOrderStatus.CANCELLED)
    );

    private final CheckbookOrderRepository checkbookOrderRepository;
    private final AccountRepository accountRepository;
    private final AuditService auditService;

    public CheckbookOrderService(CheckbookOrderRepository checkbookOrderRepository,
                                 AccountRepository accountRepository,
                                 AuditService auditService) {
        this.checkbookOrderRepository = checkbookOrderRepository;
        this.accountRepository = accountRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public CheckbookOrder findById(Long id) {
        return checkbookOrderRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande de chéquier introuvable."));
    }

    @Transactional(readOnly = true)
    public Page<CheckbookOrder> list(CheckbookOrderStatus status, Pageable pageable) {
        if (status == null) {
            return checkbookOrderRepository.findAllByOrderByRequestedAtDesc(pageable);
        }
        return checkbookOrderRepository.findAllByStatusOrderByRequestedAtDesc(status, pageable);
    }

    @Transactional(readOnly = true)
    public List<CheckbookOrder> listByClient(Long clientId) {
        return checkbookOrderRepository.findByClientIdOrderByRequestedAtDesc(clientId);
    }

    @Transactional(readOnly = true)
    public List<CheckbookOrder> listByAccount(Long accountId) {
        return checkbookOrderRepository.findByAccountIdOrderByRequestedAtDesc(accountId);
    }

    @Transactional(readOnly = true)
    public List<Account> listEligibleAccountsForRequest() {
        List<Account> accounts = accountRepository.findByStatusAndTypeInWithClient(
                AccountStatus.ACTIVE,
                List.of(AccountType.COURANT, AccountType.PROFESSIONNEL));
        return accounts.stream()
                .filter(a -> !checkbookOrderRepository.existsByAccountIdAndStatus(
                        a.getId(), CheckbookOrderStatus.PENDING))
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean canRequestCheckbook(Long accountId) {
        Account account = loadAccount(accountId);
        return isEligibleAccount(account)
                && !checkbookOrderRepository.existsByAccountIdAndStatus(accountId, CheckbookOrderStatus.PENDING);
    }

    @Transactional
    public CheckbookOrder requestCheckbook(Long accountId, int quantity, CheckbookSheetCount sheetCount,
                                           String notes, User user) {
        if (quantity < 1 || quantity > 10) {
            throw new BusinessRuleException("La quantité doit être comprise entre 1 et 10.");
        }
        if (sheetCount == null) {
            throw new BusinessRuleException("Le format du chéquier (20 ou 40 feuillets) est obligatoire.");
        }

        Account account = loadAccount(accountId);
        validateEligibleAccount(account);

        if (checkbookOrderRepository.existsByAccountIdAndStatus(accountId, CheckbookOrderStatus.PENDING)) {
            throw new BusinessRuleException("Une commande de chéquier est déjà en attente pour ce compte.");
        }

        CheckbookOrder order = new CheckbookOrder();
        order.setOrderNumber(generateOrderNumber());
        order.setAccount(account);
        order.setClient(account.getClient());
        order.setQuantity(quantity);
        order.setSheetCount(sheetCount);
        order.setStatus(CheckbookOrderStatus.PENDING);
        order.setRequestedBy(user);
        if (notes != null && !notes.isBlank()) {
            order.setNotes(notes.trim());
        }

        CheckbookOrder saved = checkbookOrderRepository.save(order);
        auditService.log(user, "CHECKBOOK_ORDER_CREATED", ENTITY_TYPE, saved.getId(),
                saved.getOrderNumber() + " — compte " + account.getAccountNumber()
                        + " — " + quantity + " chéquier(s) " + sheetCount.getLabel());
        return saved;
    }

    @Transactional
    public CheckbookOrder updateStatus(Long orderId, CheckbookOrderStatus newStatus, User user) {
        CheckbookOrder order = findById(orderId);
        CheckbookOrderStatus current = order.getStatus();

        if (current == newStatus) {
            return order;
        }

        Set<CheckbookOrderStatus> allowed = ALLOWED_TRANSITIONS.get(current);
        if (allowed == null || !allowed.contains(newStatus)) {
            throw new BusinessRuleException("Transition de statut invalide : "
                    + current.getLabel() + " → " + newStatus.getLabel());
        }

        order.setStatus(newStatus);
        Instant now = Instant.now();
        if (newStatus == CheckbookOrderStatus.PROCESSING && order.getProcessedAt() == null) {
            order.setProcessedAt(now);
        }
        if (newStatus == CheckbookOrderStatus.DELIVERED) {
            if (order.getProcessedAt() == null) {
                order.setProcessedAt(now);
            }
            order.setDeliveredAt(now);
        }

        CheckbookOrder saved = checkbookOrderRepository.save(order);
        auditService.log(user, "CHECKBOOK_ORDER_STATUS_CHANGED", ENTITY_TYPE, saved.getId(),
                saved.getOrderNumber() + " : " + current.getLabel() + " → " + newStatus.getLabel());
        return saved;
    }

    private Account loadAccount(Long accountId) {
        return accountRepository.findByIdWithClient(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Compte introuvable."));
    }

    private void validateEligibleAccount(Account account) {
        if (!isEligibleAccount(account)) {
            throw new BusinessRuleException(
                    "Les chéquiers ne sont disponibles que pour les comptes courants ou professionnels actifs.");
        }
    }

    private boolean isEligibleAccount(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            return false;
        }
        AccountType type = account.getType();
        return type == AccountType.COURANT || type == AccountType.PROFESSIONNEL;
    }

    private String generateOrderNumber() {
        return checkbookOrderRepository.findTopByOrderByIdDesc()
                .map(last -> {
                    String numeric = last.getOrderNumber().replace("CHQ-", "");
                    long next = Long.parseLong(numeric) + 1;
                    return String.format("CHQ-%05d", next);
                })
                .orElse("CHQ-00001");
    }
}
