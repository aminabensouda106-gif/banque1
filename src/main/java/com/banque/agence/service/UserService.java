package com.banque.agence.service;

import com.banque.agence.audit.AuditService;
import com.banque.agence.domain.entity.User;
import com.banque.agence.domain.enums.UserRole;
import com.banque.agence.repository.UserRepository;
import com.banque.agence.web.dto.UserCreateForm;
import com.banque.agence.web.dto.UserEditForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private static final String ENTITY_TYPE = "User";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public Page<User> listAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable."));
    }

    @Transactional
    public User create(UserCreateForm form, User performedBy) {
        String username = form.getUsername().trim();
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("Cet identifiant est déjà utilisé.");
        }

        User user = new User(
                username,
                passwordEncoder.encode(form.getPassword()),
                form.getFullName().trim(),
                normalizeEmail(form.getEmail()),
                form.getRole()
        );

        User saved = userRepository.save(user);
        auditService.log(performedBy, "USER_CREATED", ENTITY_TYPE, saved.getId(),
                saved.getUsername() + " — " + saved.getRole().getLabel());
        return saved;
    }

    @Transactional
    public User update(Long id, UserEditForm form, User performedBy) {
        User user = findById(id);
        ensureNotLastAdminDemotion(user, form.getRole());

        user.setFullName(form.getFullName().trim());
        user.setEmail(normalizeEmail(form.getEmail()));
        user.setRole(form.getRole());

        User saved = userRepository.save(user);
        auditService.log(performedBy, "USER_UPDATED", ENTITY_TYPE, saved.getId(),
                "Modification de " + saved.getUsername());
        return saved;
    }

    @Transactional
    public User enable(Long id, User performedBy) {
        User user = findById(id);
        if (user.isEnabled()) {
            return user;
        }
        user.setEnabled(true);
        User saved = userRepository.save(user);
        auditService.log(performedBy, "USER_ENABLED", ENTITY_TYPE, saved.getId(),
                "Activation de " + saved.getUsername());
        return saved;
    }

    @Transactional
    public User disable(Long id, User performedBy) {
        User user = findById(id);
        if (!user.isEnabled()) {
            return user;
        }
        ensureNotLastActiveAdmin(user);
        user.setEnabled(false);
        User saved = userRepository.save(user);
        auditService.log(performedBy, "USER_DISABLED", ENTITY_TYPE, saved.getId(),
                "Désactivation de " + saved.getUsername());
        return saved;
    }

    @Transactional
    public void changePassword(Long id, String rawPassword, User performedBy) {
        User user = findById(id);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        userRepository.save(user);
        auditService.log(performedBy, "USER_PASSWORD_CHANGED", ENTITY_TYPE, user.getId(),
                "Mot de passe modifié pour " + user.getUsername());
    }

    private void ensureNotLastActiveAdmin(User user) {
        if (user.getRole() == UserRole.ADMIN
                && userRepository.countByRoleAndEnabled(UserRole.ADMIN, true) <= 1) {
            throw new BusinessRuleException("Impossible de désactiver le dernier administrateur actif.");
        }
    }

    private void ensureNotLastAdminDemotion(User user, UserRole newRole) {
        if (user.getRole() == UserRole.ADMIN
                && newRole != UserRole.ADMIN
                && user.isEnabled()
                && userRepository.countByRoleAndEnabled(UserRole.ADMIN, true) <= 1) {
            throw new BusinessRuleException("Impossible de retirer le rôle administrateur au dernier compte admin actif.");
        }
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim();
    }
}
