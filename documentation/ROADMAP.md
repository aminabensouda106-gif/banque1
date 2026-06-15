# ROADMAP — Agence bancaire PFA

> Plan par **phases** pour développement **solo**, projet **école**, sans sur-ingénierie.
> Stack : **Java + Spring Boot** uniquement (pas de Python).
> Détails techniques : [`TECHNICAL.md`](./TECHNICAL.md) · Exigences : [`cahier-charge-PFA.pdf`](./cahier-charge-PFA.pdf)

---

## Principes du projet

| Principe | Application |
|---|---|
| **Répondre au cahier des charges** | Chaque phase mappe une section du PDF (§7, règles de gestion, scénarios §12) |
| **Projet école** | Pas de Docker, pas de microservices, pas de CI complexe — PostgreSQL installé localement suffit |
| **Java uniquement** | Spring Boot 3 + Thymeleaf + PostgreSQL — pas de Python, pas de React séparé |
| **Tester après chaque phase** | L'app doit être lançable (ou les diagrammes validés en Phase 1) avant commit + push GitHub |
| **Un commit propre par phase** | Message clair, push sur `main` à la fin de chaque phase |

---

## Vue d'ensemble

```
Phase 1          Phase 2         Phase 3        Phase 4       Phase 5       Phase 6        Phase 7        Phase 8         Phase 9
Conception   →   Bootstrap   →   Auth      →   Clients   →   Comptes   →   Transactions → Utilisateurs → Reporting   →   Finalisation
(UML + BDD)      (Spring)        (login)        (§7.1)        (§7.2)        (§7.3)         (§7.4)         (§7.5)          (soutenance)
3–5 jours        1 jour          2 jours        2–3 jours     2 jours       3 jours        2 jours        2 jours         2 jours
```

**Durée estimée :** 3–4 semaines à rythme régulier.

---

## Rituel de fin de phase (à répéter à chaque fois)

### 1. Vérifier
- Cocher toutes les étapes de la phase dans ce fichier.
- Exécuter la section **« Comment tester »** de la phase.

### 2. Committer
```bash
git add .
git commit -m "<message indiqué dans la phase>"
```

### 3. Pousser sur GitHub
```bash
git push origin main
```

### 4. Mettre à jour la doc
- `TECHNICAL.md` §7 (checklist) et §13 (décisions) si du code a changé.

---

## Phase 1 — Conception & modélisation UML

**Objectif :** Analyser le cahier des charges et produire les diagrammes **avant tout code**, comme exigé en §11 du cahier.

**Livrable :** Dossier de conception complet dans `documentation/`.

**Référence cahier :** §6 (acteurs), §7 (besoins), §8 (règles de gestion), §11 (modélisation), §12 (scénarios).

### Étapes détaillées

#### Étape 1.1 — Analyse du besoin
- [x] Relire `cahier-charge-PFA.pdf` en entier
- [x] Lister les 3 acteurs internes : Administrateur, Agent bancaire, Chef d'agence
- [x] Lister les 8 scénarios principaux (§12)
- [x] Lister les 8 règles de gestion (§8)
- [x] Problématique + objectifs documentés dans `TECHNICAL.md` §1

#### Étape 1.2 — Diagramme de cas d'utilisation
- [x] Créer `documentation/uml/cas-utilisation/01-clients-comptes.puml` (+ SVG)
- [x] Créer `documentation/uml/cas-utilisation/02-operations-supervision.puml` (+ SVG)
- [x] Auth intégrée au diagramme clients/comptes (pas de diagramme séparé)
- [x] Relier chaque cas au bon acteur avec les bonnes permissions

#### Étape 1.3 — Diagramme de classes
- [x] Créer `documentation/uml/diagramme-classes.puml` (+ SVG)
- [x] Modéliser les classes principales :
  - `User`, `Client`, `Account`, `Transaction`, `AuditLog`
  - Énumérations : `UserRole`, `AccountType`, `AccountStatus`, `ClientStatus`, `TransactionType`
- [x] Afficher les relations : Client 1—* Account, User 1—* Transaction, etc.
- [x] Annoter les attributs clés (`balance: BigDecimal`, `passwordHash`, `version`)

#### Étape 1.4 — Diagrammes de séquence (5 scénarios — couverture cahier §12)
- [x] `01-authentification` — accès sécurisé selon le rôle
- [x] `02-creation-client-compte` — ajout client + ouverture compte
- [x] `03-operations-financieres` — dépôt, retrait, virement
- [x] `04-consultation-historique` — historique filtré, relevé, reçu
- [x] `05-gestion-utilisateurs` — créer, modifier, désactiver, rôles

#### Étape 1.5 — Modèle de données (MCD, MLD, schéma relationnel)
- [x] Créer `documentation/modele-donnees/MCD.puml` (+ SVG)
- [x] Créer `documentation/modele-donnees/MLD.puml` (+ SVG)
- [x] Créer `documentation/modele-donnees/schema-relationnel.sql`
- [x] Créer `documentation/modele-donnees/dictionnaire-donnees.md`

#### Étape 1.6 — Diagramme d'activité
- [x] Créer `documentation/uml/diagramme-activite-virement.puml` (+ SVG)

#### Étape 1.7 — Validation de la conception
- [x] Vérifier que tous les besoins §7.1 à §7.5 ont un cas d'utilisation correspondant
- [x] Vérifier que les 8 règles de gestion sont reflétées dans les diagrammes
- [x] Auto-revue : conception cohérente avec le cahier des charges

### Outils conseillés
- **StarUML** ou **PlantUML** (gratuit, suffisant pour un PFA)
- **MySQL Workbench** ou draw.io pour MCD/MLD
- Pas besoin d'autre outil

### Comment tester (Phase 1)
Pas d'application à lancer. Validation par checklist :

| Vérification | OK ? |
|---|---|
| Diagramme cas d'utilisation couvre les 3 rôles | ✓ |
| Diagramme de classes a les 5 entités | ✓ |
| 5 diagrammes de séquence couvrant les 8 scénarios du cahier | ✓ |
| MCD + MLD + dictionnaire présents | ✓ |
| Les règles de gestion (solde, comptes actifs, audit) sont visibles dans les séquences | ✓ |
| Diagramme d'activité virement présent | ✓ |

### Commit & push
```bash
git add documentation/
git commit -m "docs(conception): add UML diagrams, data model and requirements analysis"
git push origin main
```

### Critères de sortie
- Tous les fichiers UML et modèle de données sont dans `documentation/`
- Tu peux expliquer chaque diagramme à l'oral en soutenance
- **Aucun code applicatif** — on code à partir de la Phase 2

---

## Phase 2 — Bootstrap du projet Spring Boot

**Objectif :** Projet Java qui compile, démarre et se connecte à PostgreSQL local.

**Référence cahier :** §10 (contraintes techniques), §13.2 (code source, scripts SQL).

### Prérequis (installation locale, sans Docker)
- [x] Java 21+ installé — détecté : Java 23 (`java -version`)
- [x] Maven installé — détecté : Maven 3.9.9 (`mvn -version`)
- [x] PostgreSQL **18** installé — port **5433** (`postgresql-x64-18`)
- [x] Port **5432** réservé à Odoo — ce projet utilise **5433** uniquement
- [x] Base créée : `psql -U postgres -h localhost -p 5433 -f documentation/setup-postgresql.sql`

### Étapes détaillées

#### Étape 2.1 — Initialiser le projet
- [x] Générer projet Spring Boot 3 (Maven, Java 21)
- [x] Dependencies : Web, Thymeleaf, Security, JPA, Validation, PostgreSQL, Flyway, DevTools
- [x] Group : `com.banque.agence` · Artifact : `agence`

#### Étape 2.2 — Structure des packages
- [x] Packages créés : `config`, `domain.entity`, `domain.enums`, `repository`, `service`, `web.controller`, `security`, `audit`

#### Étape 2.3 — Configuration
- [x] `application.yml` + `application-dev.yml`
- [x] Flyway `V1__baseline.sql`

#### Étape 2.4 — Fichiers projet
- [x] `.gitignore`
- [x] `README.md` — prérequis, setup BDD, lancement
- [x] `documentation/setup-postgresql.sql`
- [x] Maven Wrapper (`mvnw`)

#### Étape 2.5 — Page de test
- [x] `GET /` → page Thymeleaf « Banque Agence — en construction »
- [x] `mvn clean compile` OK
- [x] **PostgreSQL 18** — port **5433**, base `banque_agence` créée
- [x] `mvn spring-boot:run` OK — connexion PostgreSQL 18.3 + Flyway v1
- [x] Page d'accueil sur http://localhost:8080
- [x] Connexion PostgreSQL OK

### Comment tester (Phase 2)
```bash
# Compiler
mvn clean compile

# Lancer (PostgreSQL doit tourner)
mvn spring-boot:run
```
| Vérification | OK ? |
|---|---|
| `mvn clean compile` sans erreur | ✓ |
| `mvn spring-boot:run` sans erreur | ✓ |
| Page d'accueil sur http://localhost:8080 | ✓ |
| Connexion PostgreSQL OK | ✓ |

### Commit & push
```bash
git add .
git commit -m "chore: initialize Spring Boot project with PostgreSQL and dev config"
git push origin main
```

### Critères de sortie
- Projet buildable et lançable
- README explique comment installer et lancer sans Docker

---

## Phase 3 — Authentification & interface de base

**Objectif :** Login sécurisé, rôles, layout commun — fondation pour tout le reste.

**Référence cahier :** §6 (acteurs), §7.4 (auth), §8 règle 7 (mots de passe chiffrés), scénario §12 « Authentification ».

### Étapes détaillées

#### Étape 3.1 — Base de données utilisateurs
- [x] Migration Flyway `V2__create_users.sql` :
  - Table `users` : id, username, password_hash, full_name, email, role, enabled, created_at
  - Enum PostgreSQL `user_role` : ADMIN, AGENT, CHEF_AGENCE
- [x] Données de démo (profil dev) via `DevUserInitializer` :
  - admin / admin123 (ADMIN)
  - agent / agent123 (AGENT)
  - chef / chef123 (CHEF_AGENCE)
  - Mots de passe hashés BCrypt

#### Étape 3.2 — Couche domaine
- [x] Entité `User` + enum `UserRole`
- [x] `UserRepository`

#### Étape 3.3 — Sécurité
- [x] `UserDetailsService` implémenté
- [x] `SecurityConfig` : form login, logout, BCrypt encoder
- [x] Routes protégées : tout sauf `/login`, `/css/**`, `/js/**`
- [x] Redirection après login → `/dashboard`

#### Étape 3.4 — Interface
- [x] `templates/layout/main.html` — Bootstrap 5, sidebar, navbar
- [x] `templates/login.html`
- [x] `templates/dashboard.html` — stub « Bienvenue, [nom] ([rôle]) »
- [x] Messages flash (succès / erreur)
- [x] Pages erreur 403 et 404 simples

#### Étape 3.5 — Contrôle d'accès par rôle (base)
- [x] Menu sidebar différent selon rôle (ex. lien « Utilisateurs » visible seulement pour ADMIN)
- [x] Règles URL pour `/admin/**` (rôle ADMIN)

### Comment tester (Phase 3)
```bash
mvn spring-boot:run
```
Ouvrir **http://localhost:8081** (le port 8080 est occupé par EnterpriseDB sur ce PC — voir `postgresql-local.md`).

| Vérification | OK ? |
|---|---|
| `http://localhost:8081` redirige vers login si non connecté | ✓ |
| Login `admin` / `admin123` → dashboard | ✓ |
| Login `agent` / `agent123` → dashboard, menu sans admin | ✓ |
| Mauvais mot de passe → message d'erreur | ✓ |
| Logout fonctionne | ✓ |
| `mvn test` passe (au moins 1 test login MockMvc si ajouté) | ✓ |

### Commit & push
```bash
git add .
git commit -m "feat(auth): add login, roles, layout and user seed data"
git push origin main
```

### Critères de sortie
- Les 3 rôles peuvent se connecter
- Layout prêt pour les modules suivants

---

## Phase 4 — Gestion des clients

**Objectif :** CRUD clients complet + recherche multicritère.

**Référence cahier :** §7.1, scénario §12 « Ajout d'un client ».

### Étapes détaillées

#### Étape 4.1 — Base de données
- [x] Migration `V3__create_clients.sql` :
  - client_number (unique), cin (unique), first_name, last_name, email, phone, address
  - professional_info, status (ACTIVE/SUSPENDED/INACTIVE), created_at, updated_at

#### Étape 4.2 — Backend
- [x] Entité `Client` + enum `ClientStatus`
- [x] `ClientRepository` avec méthodes de recherche
- [x] `ClientService` : create, update, findById, search, changeStatus
- [x] Génération auto `clientNumber` (ex. CLI-00001)

#### Étape 4.3 — Contrôleur & vues
- [x] `GET /clients` — liste paginée
- [x] `GET /clients/new` + `POST /clients` — création
- [x] `GET /clients/{id}` — fiche détail
- [x] `GET /clients/{id}/edit` + `POST /clients/{id}` — modification
- [x] `GET /clients/search?q=...` — recherche par nom, CIN, n° client, téléphone
- [x] Actions : activer / suspendre / désactiver

#### Étape 4.4 — Validation
- [x] `@NotBlank`, `@Email`, CIN unique (message d'erreur en français)
- [x] Formulaires Thymeleaf avec affichage des erreurs

#### Étape 4.5 — Audit (première utilisation)
- [x] Migration `V4__create_audit_logs.sql`
- [x] Entité `AuditLog` + service
- [x] Logger création et modification client

### Comment tester (Phase 4)
```bash
mvn spring-boot:run
```
Ouvrir **http://localhost:8081/clients** (login : agent / agent123).

| Vérification | OK ? |
|---|---|
| Créer un client avec toutes les infos | ✓ |
| Recherche par nom fonctionne | ✓ |
| Recherche par CIN fonctionne | ✓ |
| Doublon CIN refusé avec message clair | ✓ |
| Suspendre un client change son statut | ✓ |
| Fiche détail affiche toutes les infos | ✓ |
| Pagination sur la liste (si > 10 clients) | ✓ |
| `mvn test` passe | ✓ |

### Commit & push
```bash
git add .
git commit -m "feat(clients): add CRUD, search and status management"
git push origin main
```

### Critères de sortie
- Tous les points §7.1 couverts et démontrables

---

## Phase 5 — Gestion des comptes bancaires

**Objectif :** Ouvrir, consulter, bloquer, débloquer, clôturer des comptes.

**Référence cahier :** §7.2, règles §8 n°1-2 et 6, scénario §12 « Ouverture d'un compte ».

### Étapes détaillées

#### Étape 5.1 — Base de données
- [x] Migration `V5__create_accounts.sql` :
  - account_number (unique), type (COURANT/EPARGNE/PROFESSIONNEL)
  - status (ACTIVE/BLOCKED/CLOSED), balance NUMERIC(19,4) default 0
  - client_id FK, opened_at, closed_at, version (optimistic lock)

#### Étape 5.2 — Backend
- [x] Entité `Account` + enums `AccountType`, `AccountStatus`
- [x] `AccountRepository`, `AccountService`
- [x] `openAccount(clientId, type)` — génère numéro (ex. ACC-00001)
- [x] `block`, `unblock`, `close` avec règles métier
- [x] `listByClient(clientId)`

#### Étape 5.3 — Interface
- [x] Depuis fiche client : bouton « Ouvrir un compte » + formulaire type
- [x] `GET /accounts/{id}` — détail : solde, statut, type, client lié
- [x] `GET /accounts` — liste globale paginée (optionnel)
- [x] Boutons bloquer / débloquer / clôturer sur la fiche compte

#### Étape 5.4 — Règles métier
- [x] Compte clôturé → impossible de bloquer/débloquer
- [x] Un client peut avoir plusieurs comptes (affichage liste sur fiche client)

### Comment tester (Phase 5)
```bash
mvn spring-boot:run
```
Ouvrir **http://localhost:8081** → Clients → fiche client → **Ouvrir un compte**.

| Vérification | OK ? |
|---|---|
| Ouvrir compte courant pour un client existant | ✓ |
| Ouvrir 2e compte (épargne) pour le même client | ✓ |
| Solde initial = 0 | ✓ |
| Bloquer un compte → statut BLOCKED | ✓ |
| Débloquer → statut ACTIVE | ✓ |
| Clôturer → statut CLOSED, date de clôture renseignée | ✓ |
| Liste des comptes visible sur fiche client | ✓ |
| `mvn test` passe | ✓ |

### Commit & push
```bash
git add .
git commit -m "feat(accounts): add open, view, block and close account flows"
git push origin main
```

### Critères de sortie
- Tous les points §7.2 couverts
- Comptes liés correctement aux clients

---

## Phase 6 — Gestion des transactions

**Objectif :** Dépôt, retrait, virement + historique — cœur métier du projet.

**Référence cahier :** §7.3, règles §8 n°3-5, scénarios §12 dépôt/retrait/virement/historique.

### Étapes détaillées

#### Étape 6.1 — Base de données
- [x] Migration `V6__create_transactions.sql` :
  - type (DEPOT/RETRAIT/VIREMENT), amount, source_account_id, destination_account_id
  - executed_by FK users, executed_at, description

#### Étape 6.2 — Backend
- [x] Entité `Transaction` + enum `TransactionType`
- [x] `TransactionService` avec `@Transactional` :
  - **deposit(accountId, amount, user)** → crédite le compte
  - **withdraw(accountId, amount, user)** → vérifie solde ≥ montant, débite
  - **transfer(sourceId, destId, amount, user)** → vérifie les 2 comptes ACTIVE, débite/crédite
- [x] Refuser si compte BLOCKED ou CLOSED
- [x] Refuser si montant ≤ 0
- [x] Chaque opération → entrée `AuditLog`

#### Étape 6.3 — Interface
- [x] Menu « Opérations » avec 3 formulaires : Dépôt, Retrait, Virement
- [x] Sélection compte par numéro ou liste déroulante
- [x] Message succès avec nouveau solde
- [x] `GET /transactions` — historique paginé
- [x] Filtres : date début/fin, type, n° compte, utilisateur

#### Étape 6.4 — Tests unitaires (simples)
- [x] Test : retrait avec solde insuffisant → exception
- [x] Test : virement vers compte bloqué → exception
- [x] Test : dépôt augmente le solde

### Comment tester (Phase 6)
```bash
mvn test
mvn spring-boot:run
```
| Vérification | OK ? |
|---|---|
| Dépôt 1000 → solde = 1000 | ✓ |
| Retrait 300 → solde = 700 | ✓ |
| Retrait 800 → refusé (solde insuffisant) | ✓ |
| Virement 200 vers autre compte → soldes mis à jour | ✓ |
| Virement depuis compte bloqué → refusé | ✓ |
| Historique affiche date, montant, type, utilisateur | ✓ |
| Filtre par type DEPOT fonctionne | ✓ |
| `mvn test` passe | ✓ |

### Commit & push
```bash
git add .
git commit -m "feat(transactions): add deposit, withdraw, transfer and history"
git push origin main
```

### Critères de sortie
- Les 8 règles de gestion liées aux transactions sont respectées
- Scénarios dépôt, retrait, virement démontrables de bout en bout

---

## Phase 7 — Gestion des utilisateurs internes

**Objectif :** L'administrateur gère les comptes du personnel.

**Référence cahier :** §7.4, scénario §12 « Gestion utilisateurs ».

### Étapes détaillées

#### Étape 7.1 — Backend
- [x] `UserService` : create, update, enable, disable
- [x] Hash BCrypt à la création / changement de mot de passe
- [x] Interdire suppression du dernier admin

#### Étape 7.2 — Interface (ADMIN uniquement)
- [x] `GET /admin/users` — liste
- [x] `GET /admin/users/new` + `POST` — créer avec rôle
- [x] `GET /admin/users/{id}/edit` + `POST` — modifier nom, email, rôle
- [x] Activer / désactiver un compte
- [x] Formulaire définir mot de passe

#### Étape 7.3 — Sécurité
- [x] `@PreAuthorize("hasRole('ADMIN')")` sur tout `/admin/users/**`
- [x] Utilisateur désactivé → `enabled=false` → login impossible

### Comment tester (Phase 7)
```bash
mvn spring-boot:run
```
| Vérification | OK ? |
|---|---|
| Agent ne peut pas accéder à `/admin/users` (403) | ✓ |
| Admin crée un nouvel agent | ✓ |
| Nouvel agent peut se connecter | ✓ |
| Admin désactive un agent → login refusé | ✓ |
| Admin modifie le rôle d'un utilisateur | ✓ |
| `mvn test` passe | ✓ |

### Commit & push
```bash
git add .
git commit -m "feat(users): add admin user management with role assignment"
git push origin main
```

### Critères de sortie
- Tous les points §7.4 couverts

---

## Phase 8 — Tableau de bord & reporting

**Objectif :** Supervision, relevés, reçus — dernière couche fonctionnelle.

**Référence cahier :** §7.5.

### Étapes détaillées

#### Étape 8.1 — Tableau de bord
- [ ] Remplacer le stub dashboard par des vraies données :
  - Nombre total de clients actifs
  - Nombre de comptes actifs
  - Nombre et montant des transactions du jour
- [ ] Tableau des 10 dernières opérations
- [ ] Chef d'agence voit les mêmes stats (supervision)

#### Étape 8.2 — Relevé de compte
- [ ] `GET /accounts/{id}/statement` — liste des transactions du compte
- [ ] Filtre par période (date début / fin)
- [ ] Affichage solde courant en en-tête

#### Étape 8.3 — Reçu d'opération
- [ ] `GET /transactions/{id}/receipt` — page imprimable (HTML simple)
- [ ] Infos : type, montant, date, comptes, agent responsable

#### Étape 8.4 — Export PDF (si le temps le permet)
- [ ] Export PDF du relevé avec OpenPDF — **optionnel**, le HTML imprimable suffit pour le cahier

#### Étape 8.5 — Journal d'audit (consultation)
- [ ] `GET /admin/audit` — liste paginée des actions sensibles
- [ ] Accessible ADMIN et CHEF_AGENCE

### Comment tester (Phase 8)
```bash
mvn spring-boot:run
```
| Vérification | OK ? |
|---|---|
| Dashboard affiche des chiffres réels (pas des zéros fixes) | ☐ |
| 10 dernières opérations visibles | ☐ |
| Relevé d'un compte filtré par date | ☐ |
| Reçu imprimable depuis une transaction | ☐ |
| Journal d'audit liste les opérations passées | ☐ |

### Commit & push
```bash
git add .
git commit -m "feat(reporting): add dashboard, statements, receipts and audit log view"
git push origin main
```

### Critères de sortie
- Tous les points §7.5 couverts (PDF optionnel)

---

## Phase 9 — Finalisation & préparation soutenance

**Objectif :** Projet propre, documenté, prêt à présenter.

**Référence cahier :** §13 (livrables), §15 (conclusion).

### Étapes détaillées

#### Étape 9.1 — Données de démonstration
- [ ] Script seed ou SQL manuel : 5 clients, 8 comptes, 20 transactions variées
- [ ] Documenter les identifiants de test dans le README

#### Étape 9.2 — Qualité
- [ ] Relire tous les libellés UI en français
- [ ] Messages d'erreur explicites partout
- [ ] `mvn test` — tous les tests passent
- [ ] Parcours complet sans bug bloquant

#### Étape 9.3 — Documentation
- [ ] `documentation/manuel-utilisateur.md` — guide court avec captures
- [ ] `documentation/demo-script.md` — scénario 5–7 min pour la soutenance
- [ ] Mettre à jour `TECHNICAL.md` §7 checklist (tout en Done)
- [ ] README final : installation PostgreSQL, lancement, comptes de test

#### Étape 9.4 — Rapport & présentation
- [ ] Intégrer les diagrammes Phase 1 dans le rapport
- [ ] PowerPoint : contexte → conception → démo → conclusion
- [ ] Vérifier que le rapport cite le cahier des charges et les règles de gestion

#### Étape 9.5 — Revue cahier des charges
- [ ] Checklist finale — chaque section du cahier est couverte :

| Section cahier | Couvert par |
|---|---|
| §7.1 Clients | Phase 4 |
| §7.2 Comptes | Phase 5 |
| §7.3 Transactions | Phase 6 |
| §7.4 Utilisateurs | Phase 7 |
| §7.5 Reporting | Phase 8 |
| §8 Règles de gestion | Phases 5–6 + audit |
| §9 Sécurité NFR | Phase 3 |
| §11 Modélisation | Phase 1 |
| §12 Scénarios | Phases 3–8 |

### Comment tester (Phase 9)
```bash
mvn clean test
mvn spring-boot:run
# Suivre documentation/demo-script.md du début à la fin
```
| Vérification | OK ? |
|---|---|
| Démo complète en < 10 minutes sans erreur | ☐ |
| `mvn clean test` OK | ☐ |
| README à jour | ☐ |
| Manuel utilisateur rédigé | ☐ |
| GitHub à jour avec tout le code | ☐ |

### Commit & push
```bash
git add .
git commit -m "docs: add user manual, demo script and finalize project for presentation"
git push origin main
```

### Critères de sortie
- Projet défendable académiquement
- Dépôt GitHub complet et à jour

---

## Scénario de démo soutenance (5–7 min)

1. Login **agent** → créer client → rechercher par CIN
2. Ouvrir compte **courant** + compte **épargne**
3. **Dépôt** 10 000 MAD
4. **Retrait** 500 MAD → montrer refus si montant trop élevé
5. **Virement** 1 000 MAD courant → épargne
6. **Historique** filtré par compte
7. Login **chef d'agence** → **dashboard**
8. Login **admin** → gestion utilisateur + **journal d'audit**
9. Montrer **relevé** et **reçu** imprimable

---

## Ce qu'on ne fait PAS (volontairement)

- Docker / Kubernetes
- Python / Flask / Django
- React / Angular séparé
- API REST publique / Swagger (sauf si utile en interne — pas nécessaire)
- Microservices
- Portail client en ligne
- Cartes bancaires / GAB
- CI/CD GitHub Actions (optionnel, pas requis)
- Tests à 100 % de couverture

---

## Prochaine action

**Phase 7 terminée.** Commencer **Phase 8 — Tableau de bord & reporting**.

---

*Dernière mise à jour : 2026-06-15*
