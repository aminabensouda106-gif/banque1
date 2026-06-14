package com.banque.agence.service;

import com.banque.agence.audit.AuditService;
import com.banque.agence.domain.entity.Account;
import com.banque.agence.domain.entity.Client;
import com.banque.agence.domain.entity.User;
import com.banque.agence.domain.enums.AccountStatus;
import com.banque.agence.domain.enums.AccountType;
import com.banque.agence.domain.enums.ClientStatus;
import com.banque.agence.repository.AccountRepository;
import com.banque.agence.repository.ClientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
public class AccountService {

    private static final String ENTITY_TYPE = "Account";

    private final AccountRepository accountRepository;
    private final ClientRepository clientRepository;
    private final AuditService auditService;

    public AccountService(AccountRepository accountRepository,
                          ClientRepository clientRepository,
                          AuditService auditService) {
        this.accountRepository = accountRepository;
        this.clientRepository = clientRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public Account findById(Long id) {
        return accountRepository.findByIdWithClient(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compte introuvable."));
    }

    @Transactional(readOnly = true)
    public List<Account> listByClient(Long clientId) {
        ensureClientExists(clientId);
        return accountRepository.findByClientIdOrderByOpenedAtDesc(clientId);
    }

    @Transactional(readOnly = true)
    public Page<Account> listAll(Pageable pageable) {
        return accountRepository.findAllByOrderByOpenedAtDesc(pageable);
    }

    @Transactional
    public Account openAccount(Long clientId, AccountType type, User performedBy) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client introuvable."));
        if (client.getStatus() != ClientStatus.ACTIVE) {
            throw new BusinessRuleException("Impossible d'ouvrir un compte pour un client non actif.");
        }

        Account account = new Account();
        account.setAccountNumber(generateAccountNumber());
        account.setClient(client);
        account.setType(type);
        account.setStatus(AccountStatus.ACTIVE);
        account.setBalance(BigDecimal.ZERO);

        Account saved = accountRepository.save(account);
        auditService.log(performedBy, "ACCOUNT_OPENED", ENTITY_TYPE, saved.getId(),
                saved.getAccountNumber() + " (" + type.getLabel() + ") pour " + client.getClientNumber());
        return saved;
    }

    @Transactional
    public Account block(Long id, User performedBy) {
        Account account = findById(id);
        ensureNotClosed(account, "bloquer");
        if (account.getStatus() == AccountStatus.BLOCKED) {
            throw new BusinessRuleException("Ce compte est déjà bloqué.");
        }
        account.setStatus(AccountStatus.BLOCKED);
        Account saved = accountRepository.save(account);
        auditService.log(performedBy, "ACCOUNT_BLOCKED", ENTITY_TYPE, saved.getId(),
                "Compte " + saved.getAccountNumber() + " bloqué");
        return saved;
    }

    @Transactional
    public Account unblock(Long id, User performedBy) {
        Account account = findById(id);
        ensureNotClosed(account, "débloquer");
        if (account.getStatus() != AccountStatus.BLOCKED) {
            throw new BusinessRuleException("Seul un compte bloqué peut être débloqué.");
        }
        account.setStatus(AccountStatus.ACTIVE);
        Account saved = accountRepository.save(account);
        auditService.log(performedBy, "ACCOUNT_UNBLOCKED", ENTITY_TYPE, saved.getId(),
                "Compte " + saved.getAccountNumber() + " débloqué");
        return saved;
    }

    @Transactional
    public Account close(Long id, User performedBy) {
        Account account = findById(id);
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BusinessRuleException("Ce compte est déjà clôturé.");
        }
        if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessRuleException("Impossible de clôturer un compte avec un solde restant.");
        }
        account.setStatus(AccountStatus.CLOSED);
        account.setClosedAt(Instant.now());
        Account saved = accountRepository.save(account);
        auditService.log(performedBy, "ACCOUNT_CLOSED", ENTITY_TYPE, saved.getId(),
                "Compte " + saved.getAccountNumber() + " clôturé");
        return saved;
    }

    private void ensureClientExists(Long clientId) {
        if (!clientRepository.existsById(clientId)) {
            throw new ResourceNotFoundException("Client introuvable.");
        }
    }

    private void ensureNotClosed(Account account, String action) {
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BusinessRuleException("Impossible de " + action + " un compte clôturé.");
        }
    }

    private String generateAccountNumber() {
        return accountRepository.findTopByOrderByIdDesc()
                .map(last -> {
                    String numeric = last.getAccountNumber().replace("ACC-", "");
                    long next = Long.parseLong(numeric) + 1;
                    return String.format("ACC-%05d", next);
                })
                .orElse("ACC-00001");
    }
}
