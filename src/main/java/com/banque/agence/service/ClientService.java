package com.banque.agence.service;

import com.banque.agence.audit.AuditService;
import com.banque.agence.domain.entity.Client;
import com.banque.agence.domain.entity.User;
import com.banque.agence.domain.enums.ClientStatus;
import com.banque.agence.repository.ClientRepository;
import com.banque.agence.web.dto.ClientForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClientService {

    private static final String ENTITY_TYPE = "Client";
    public static final String DEFAULT_PORTAL_PASSWORD = "client123";

    private final ClientRepository clientRepository;
    private final AuditService auditService;
    private final PasswordEncoder passwordEncoder;

    public ClientService(ClientRepository clientRepository,
                         AuditService auditService,
                         PasswordEncoder passwordEncoder) {
        this.clientRepository = clientRepository;
        this.auditService = auditService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Page<Client> search(String query, Pageable pageable) {
        String q = query == null ? "" : query.trim();
        return clientRepository.search(q.isEmpty() ? null : q, pageable);
    }

    @Transactional(readOnly = true)
    public Client findById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client introuvable."));
    }

    @Transactional
    public Client create(ClientForm form, User performedBy) {
        validateUniqueCin(form.getCin(), null);

        Client client = new Client();
        client.setClientNumber(generateClientNumber());
        applyForm(client, form);
        client.setStatus(ClientStatus.ACTIVE);
        enablePortalAccess(client);

        Client saved = clientRepository.save(client);
        auditService.log(performedBy, "CLIENT_CREATED", ENTITY_TYPE, saved.getId(),
                "Client " + saved.getClientNumber() + " — " + saved.getFullName());
        return saved;
    }

    @Transactional
    public Client update(Long id, ClientForm form, User performedBy) {
        Client client = findById(id);
        validateUniqueCin(form.getCin(), id);
        applyForm(client, form);

        Client saved = clientRepository.save(client);
        auditService.log(performedBy, "CLIENT_UPDATED", ENTITY_TYPE, saved.getId(),
                "Modification fiche " + saved.getClientNumber());
        return saved;
    }

    @Transactional
    public Client changeStatus(Long id, ClientStatus status, User performedBy) {
        Client client = findById(id);
        ClientStatus previous = client.getStatus();
        client.setStatus(status);

        Client saved = clientRepository.save(client);
        auditService.log(performedBy, "CLIENT_STATUS_CHANGED", ENTITY_TYPE, saved.getId(),
                previous.getLabel() + " → " + status.getLabel());
        return saved;
    }

    private void applyForm(Client client, ClientForm form) {
        client.setFirstName(form.getFirstName().trim());
        client.setLastName(form.getLastName().trim());
        client.setCin(form.getCin().trim().toUpperCase());
        client.setEmail(blankToNull(form.getEmail()));
        client.setPhone(blankToNull(form.getPhone()));
        client.setAddress(blankToNull(form.getAddress()));
        client.setProfessionalInfo(blankToNull(form.getProfessionalInfo()));
    }

    private void validateUniqueCin(String cin, Long excludeId) {
        String normalized = cin.trim().toUpperCase();
        boolean duplicate = excludeId == null
                ? clientRepository.existsByCin(normalized)
                : clientRepository.existsByCinAndIdNot(normalized, excludeId);
        if (duplicate) {
            throw new DuplicateResourceException("Un client avec ce CIN existe déjà.");
        }
    }

    private String generateClientNumber() {
        return clientRepository.findTopByOrderByIdDesc()
                .map(last -> {
                    long next = parseSequenceSuffix(last.getClientNumber()) + 1;
                    return String.format("CLI-%05d", next);
                })
                .orElse("CLI-00001");
    }

    private static long parseSequenceSuffix(String reference) {
        int separator = reference.lastIndexOf('-');
        if (separator >= 0 && separator < reference.length() - 1) {
            String suffix = reference.substring(separator + 1);
            if (!suffix.isEmpty() && suffix.chars().allMatch(Character::isDigit)) {
                return Long.parseLong(suffix);
            }
        }
        throw new IllegalStateException("Format de numéro client invalide : " + reference);
    }

    private void enablePortalAccess(Client client) {
        client.setPortalEnabled(true);
        client.setPasswordHash(passwordEncoder.encode(DEFAULT_PORTAL_PASSWORD));
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
