package com.banque.agence.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Resynchronise l'accès portail pour les clients de démo déjà présents en base
 * (le seed métier complet ne s'exécute que si la table clients est vide).
 */
@Component
@Profile("dev")
@Order(3)
@ConditionalOnProperty(name = "banque.demo.portal-sync-enabled", havingValue = "true", matchIfMissing = true)
public class DevPortalAccessInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DevPortalAccessInitializer.class);

    private final DemoPortalSync demoPortalSync;

    public DevPortalAccessInitializer(DemoPortalSync demoPortalSync) {
        this.demoPortalSync = demoPortalSync;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<String> activated = demoPortalSync.syncMissingPortalAccess();
        if (!activated.isEmpty()) {
            log.info("Accès portail client resynchronisé pour les CIN : {}", activated);
        }
    }
}
