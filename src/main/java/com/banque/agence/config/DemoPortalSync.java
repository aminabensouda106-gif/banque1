package com.banque.agence.config;

import com.banque.agence.domain.entity.Client;
import com.banque.agence.repository.ClientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Active ou resynchronise l'accès portail pour les clients de démo connus.
 * Idempotent : peut s'exécuter à chaque démarrage sans dupliquer les données métier.
 */
@Component
@Profile("dev")
public class DemoPortalSync {

    private static final Logger log = LoggerFactory.getLogger(DemoPortalSync.class);

    public static final String DEMO_PASSWORD = "client123";

    public static final Map<String, String> DEMO_PORTAL_CLIENTS = Map.of(
            "MB654321", "Moncef Bensouda",
            "AI876543", "Abdellah Idrissi"
    );

    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoPortalSync(ClientRepository clientRepository, PasswordEncoder passwordEncoder) {
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void enablePortal(Client client, String rawPassword) {
        client.setPortalEnabled(true);
        client.setPasswordHash(passwordEncoder.encode(rawPassword));
        clientRepository.save(client);
    }

    /**
     * @return CIN des clients pour lesquels le portail vient d'être activé
     */
    public List<String> syncMissingPortalAccess() {
        return DEMO_PORTAL_CLIENTS.entrySet().stream()
                .filter(entry -> activateIfNeeded(entry.getKey(), entry.getValue()))
                .map(Map.Entry::getKey)
                .toList();
    }

    private boolean activateIfNeeded(String cin, String label) {
        return clientRepository.findByCin(cin)
                .filter(client -> !client.isPortalEnabled() || client.getPasswordHash() == null)
                .map(client -> {
                    enablePortal(client, DEMO_PASSWORD);
                    log.info("Portail activé pour {} (CIN {}) — mot de passe démo : {}",
                            label, cin, DEMO_PASSWORD);
                    return true;
                })
                .orElse(false);
    }
}
