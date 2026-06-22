# Plan de présentation — soutenance PFA

**Support PowerPoint :**
- [`presentation/presentation-PFA-AmanaBank.pptx`](presentation/presentation-PFA-AmanaBank.pptx) (30 slides, format 16:9)
- Copie locale : `C:\Users\HP\Downloads\PFA.pptx`

**Structure imposée par l'encadrante** (20 min : 15 présentation + 5 questions) :

| # | Section |
|---|---------|
| 01 | Contexte du projet |
| 02 | Problématique |
| 03 | Objectifs |
| 04 | Méthodologie |
| 05 | Conception |
| 06 | Réalisation / démonstration |
| 07 | Résultats obtenus |
| 08 | Difficultés rencontrées |
| 09 | Conclusion et perspectives |

Pour régénérer : `python documentation/generate_presentation.py`

---

## Slide 1 — Titre

- **Banque Agence** — Système de gestion d'agence bancaire
- Nom, filière, encadrant, année universitaire
- Logo / capture écran login (personnel ou client)

## Slide 2 — Contexte et problématique

- Rôle d'une agence bancaire au quotidien
- Limites d'une gestion manuelle (erreurs, traçabilité, délais)
- Objectif : application web centralisée pour le personnel **et** suivi client en ligne

## Slide 3 — Cahier des charges (périmètre)

- Fonctionnalités §7 : clients, comptes, transactions, utilisateurs, reporting
- Extensions v1.1 : **paiement facture**, **commande chéquier**
- Extension v1.2 : **notifications** (centre in-app) et **alertes métier** sur le dashboard
- Extension v2 : **portail client** + notifications opérations
- Organisation en **4 pistes** : Noyau (1–8) → Extensions (10–11) → Canal client (13–14) → Clôture (12)
- Hors périmètre : crédit, cartes/GAB, microservices

## Slide 4 — Stack technique

- Java 21, Spring Boot 3, Thymeleaf, PostgreSQL, Flyway, Spring Security
- Architecture en couches (schéma simplifié)
- Seed dev modulaire : `DevUserInitializer`, `DevDemoDataInitializer`, `DemoPortalSync`
- Référence : `documentation/TECHNICAL.md`, `documentation/ROADMAP.md`

## Slide 5 — Conception (diagrammes)

Intégrer les SVG du dossier `documentation/` :

| Diagramme | Fichier |
|---|---|
| Cas d'utilisation | `uml/cas-utilisation/01-clients-comptes.svg`, `02-operations-supervision.svg`, `03-portail-client.svg` |
| Modèle de données | `modele-donnees/MCD.svg`, `MLD.svg` |
| Classes | `uml/diagramme-classes.svg` |
| Séquence auth | `uml/sequence/01-authentification.svg` (personnel + client) |
| Séquence métier | `06-paiement-facture.svg`, `07-commande-chequier.svg`, `08-notifications.svg`, `09-portail-client-notifications.svg` |

## Slide 6 — Règles de gestion clés

- Solde ≥ montant pour retrait / paiement
- Compte actif obligatoire pour les opérations
- Un seul chéquier **en attente** par compte
- Paiement facture → transaction ; chéquier → pas d'impact solde
- Journal d'audit sur les actions sensibles
- Portail client : lecture seule, notifications si `portal_enabled`

## Slide 7 — Sécurité

- Authentification formulaire unique `/login`, mots de passe BCrypt
- Personnel : rôles ADMIN / AGENT / CHEF_AGENCE
- Client : rôle CLIENT, routes `/portal/**` isolées
- Autorisation par rôle (`@PreAuthorize`, menu conditionnel)

## Slide 8 — Démo live

- Suivre [demo-script.md](demo-script.md)
- Préparer onglets : login agent, client Ahmed (CIN `CD789012`), opérations

## Slide 9 — Tests et qualité

- Tests d'intégration Spring (`mvn test` — **80 tests**)
- Migrations Flyway versionnées (**V1–V11**)
- Données de démo reproductibles (`demo-reset.sql` + seed modulaire)

## Slide 10 — Difficultés et solutions

Exemples à personnaliser :
- Port PostgreSQL 5433 vs 5432
- Cohérence soldes / transactions
- Workflow chéquier sans mouvement financier
- Portail client sur base existante → `DemoPortalSync` au démarrage

## Slide 11 — Conclusion

- Objectifs atteints (checklist cahier des charges + extensions)
- Perspectives : API REST, CI/CD, export PDF **relevé** (reçu PDF déjà livré)

## Slide 12 — Questions

- Remerciements

---

## Checklist rapport écrit

- [ ] Introduction + contexte
- [ ] Analyse du besoin (cahier des charges)
- [ ] Conception (UML + MCD/MLD) — inclure portail client et notifications
- [ ] Réalisation (architecture, modules, seed dev)
- [ ] Tests
- [ ] Manuel utilisateur (annexe ou référence `manuel-utilisateur.md`)
- [ ] Conclusion et bibliographie

## Checklist cahier des charges

| Section | Couvert |
|---|---|
| §7.1 Clients | ✓ |
| §7.2 Comptes | ✓ |
| §7.3 Transactions | ✓ |
| §7.4 Utilisateurs | ✓ |
| §7.5 Reporting | ✓ |
| §8 Règles de gestion | ✓ |
| §9 Sécurité | ✓ |
| §11 Modélisation | ✓ |
| §12 Scénarios | ✓ |
| Extension paiement facture | ✓ |
| Extension chéquier | ✓ |
| Extension notifications | ✓ |
| Extension portail client | ✓ |
