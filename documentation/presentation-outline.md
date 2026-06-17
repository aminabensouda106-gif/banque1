# Plan de présentation — soutenance PFA

Structure suggérée pour un support PowerPoint (10 à 12 slides, ~15 min dont 5–7 min de démo live).

---

## Slide 1 — Titre

- **Banque Agence** — Système de gestion d'agence bancaire
- Nom, filière, encadrant, année universitaire
- Logo / capture écran login

## Slide 2 — Contexte et problématique

- Rôle d'une agence bancaire au quotidien
- Limites d'une gestion manuelle (erreurs, traçabilité, délais)
- Objectif : application web centralisée pour le personnel

## Slide 3 — Cahier des charges (périmètre)

- Fonctionnalités §7 : clients, comptes, transactions, utilisateurs, reporting
- Extensions v1.1 : **paiement facture**, **commande chéquier**
- Hors périmètre : crédit, portail client, cartes/GAB

## Slide 4 — Stack technique

- Java 21, Spring Boot 3, Thymeleaf, PostgreSQL, Flyway, Spring Security
- Architecture en couches (schéma simplifié)
- Référence : `documentation/TECHNICAL.md`

## Slide 5 — Conception (diagrammes)

Intégrer les SVG du dossier `documentation/` :

| Diagramme | Fichier |
|---|---|
| Cas d'utilisation | `uml/cas-utilisation/01-clients-comptes.svg`, `02-operations-supervision.svg` |
| Modèle de données | `modele-donnees/MCD.svg`, `MLD.svg` |
| Classes | `uml/diagramme-classes.svg` |
| Séquence (exemple) | `uml/sequence/06-paiement-facture.svg` ou `07-commande-chequier.svg` |

## Slide 6 — Règles de gestion clés

- Solde ≥ montant pour retrait / paiement
- Compte actif obligatoire pour les opérations
- Un seul chéquier **en attente** par compte
- Paiement facture → transaction ; chéquier → pas d'impact solde
- Journal d'audit sur les actions sensibles

## Slide 7 — Sécurité

- Authentification formulaire, mots de passe BCrypt
- Autorisation par rôle (`@PreAuthorize`, menu conditionnel)
- Pas de stockage en clair des mots de passe

## Slide 8 — Démo live

- Suivre [demo-script.md](demo-script.md)
- Préparer onglets : login agent, client Ahmed (CIN CD789012), opérations

## Slide 9 — Tests et qualité

- Tests d'intégration Spring (`mvn test` — 69 tests)
- Migrations Flyway versionnées (V1–V9)
- Données de démo reproductibles

## Slide 10 — Difficultés et solutions

Exemples à personnaliser :
- Port PostgreSQL 5433 vs 5432
- Cohérence soldes / transactions
- Workflow chéquier sans mouvement financier

## Slide 11 — Conclusion

- Objectifs atteints (checklist cahier des charges)
- Perspectives : API REST, portail client, CI/CD (hors scope actuel)

## Slide 12 — Questions

- Remerciements

---

## Checklist rapport écrit

- [ ] Introduction + contexte
- [ ] Analyse du besoin (cahier des charges)
- [ ] Conception (UML + MCD/MLD)
- [ ] Réalisation (architecture, modules)
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
| Extension paiement facture | ✓ |
| Extension chéquier | ✓ |
