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

## Organisation en pistes (flexibilité)

Les **numéros de phase** sont conservés pour l'historique Git. L'**ordre logique** recommandé pour développer ou étendre le projet :

| Ordre logique | Phase | Piste | Contenu |
|---|---|---|---|
| 1 → 8 | **1–8** | **Noyau** | Cahier des charges (§7.1–§7.5) — implémenté |
| 9 → 10 | **10–11** | **Extensions agence** | Paiement facture, commande chéquier (v1.1) |
| 11 → 12 | **13–14** | **Canal client** | Notifications personnel + portail client (v1.2 → v2) |
| **13 (dernier)** | **12** | **Clôture** | Démo, doc, soutenance — **après toutes les fonctionnalités** |

> **Phase 9** : numéro non utilisé (réservé). Les extensions ont été numérotées 10–11 pour laisser le noyau 1–8 intact.

### Seed de démo modulaire (profil `dev`)

Chaque brique peut évoluer indépendamment sans réinitialiser toute la base :

| Ordre | Composant | Déclenchement | Rôle |
|---|---|---|---|
| 1 | `DevUserInitializer` | si `users` vide | admin, agent, chef |
| 2 | `DevDemoDataInitializer` | si `clients` vide | clients, comptes, transactions, factures, chéquiers |
| 3 | `DemoPortalSync` | à chaque démarrage (`portal-sync-enabled`) | active le portail pour Ahmed / Youssef si manquant |

Configuration : `application-dev.yml` → `banque.demo.seed-enabled`, `banque.demo.portal-sync-enabled`.

```
Piste Noyau          Piste Extensions       Piste Canal client        Piste Clôture
Phase 1 … 8    →     Phase 10, 11    →     Phase 13, 14       →     Phase 12
(conception→report)  (facture, chéquier)   (notifs, portail)        (démo, soutenance)
```

**Durée estimée :** 3–4 semaines (cahier) + **4–6 jours** pour les extensions.

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
- [x] Remplacer le stub dashboard par des vraies données :
  - Nombre total de clients actifs
  - Nombre de comptes actifs
  - Nombre et montant des transactions du jour
- [x] Tableau des 10 dernières opérations
- [x] Chef d'agence voit les mêmes stats (supervision)

#### Étape 8.2 — Relevé de compte
- [x] `GET /accounts/{id}/statement` — liste des transactions du compte
- [x] Filtre par période (date début / fin)
- [x] Affichage solde courant en en-tête

#### Étape 8.3 — Reçu d'opération
- [x] `GET /transactions/{id}/receipt` — page imprimable (HTML simple)
- [x] Infos : type, montant, date, comptes, agent responsable

#### Étape 8.4 — Export PDF (si le temps le permet)
- [ ] Export PDF du relevé avec OpenPDF — **optionnel**, le HTML imprimable suffit pour le cahier

#### Étape 8.5 — Journal d'audit (consultation)
- [x] `GET /admin/audit` — liste paginée des actions sensibles
- [x] Accessible ADMIN et CHEF_AGENCE

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

## Phase 10 — Paiement de facture + reçu

**Objectif :** Permettre à l'agent de régler une facture (eau, électricité, télécom…) depuis un compte client, avec débit du solde et **reçu imprimable** (réutilisation du mécanisme Phase 8).

**Référence :** Extension **v1.1** — hors périmètre strict du cahier §7, mais cohérent avec §7.3 (opérations financières) et §7.5 (reçus). Réalisé **côté agence** (l'agent saisit pour le client), pas de portail client.

**Conception :** `documentation/uml/sequence/06-paiement-facture.puml`, MCD/MLD `bill_providers` + `bill_payments`, `TransactionType.PAIEMENT_FACTURE`.

### Étapes détaillées

#### Étape 10.1 — Base de données
- [x] Migration `V7__create_bill_payments.sql` :
  - Table `bill_providers` : id, code (unique), name, category, active
  - Données seed : LYDEC, ONEE, IAM, Orange, Inwi (exemples)
  - Enum PostgreSQL `transaction_type` étendu : `PAIEMENT_FACTURE`
  - Table `bill_payments` : id, account_id FK, bill_provider_id FK, client_reference, amount, transaction_id FK (unique), created_at
- [x] Index : `bill_payments(account_id)`, `bill_payments(transaction_id)`

#### Étape 10.2 — Backend
- [x] Entité `BillProvider` + `BillPayment`
- [x] Étendre `TransactionType` avec `PAIEMENT_FACTURE`
- [x] `BillPaymentService` avec `@Transactional` :
  - **payBill(accountId, providerId, clientReference, amount, user)**
  - Vérifier compte **ACTIVE**, montant > 0, référence non vide
  - Vérifier solde ≥ montant (règle R3 étendue)
  - Créer `Transaction` (source = compte client) + ligne `bill_payment`
  - Entrée `AuditLog` : `BILL_PAYMENT_CREATED`
- [x] `BillProviderRepository` (liste facturiers actifs)

#### Étape 10.3 — Interface
- [x] Menu « Opérations » → **Paiement de facture**
- [x] Formulaire : sélection compte, facturier (liste), référence client, montant
- [x] Message succès avec lien **Voir le reçu** → `/transactions/{id}/receipt`
- [x] Adapter le reçu HTML : libellé « Paiement facture », nom du facturier, référence
- [x] Historique transactions : filtre type `PAIEMENT_FACTURE`

#### Étape 10.4 — Tests
- [x] Test : paiement avec solde insuffisant → exception
- [x] Test : compte bloqué → refus
- [x] Test : paiement OK → solde débité + transaction + bill_payment créés

### Règles métier (extension)

| # | Règle |
|---|---|
| R9 | Paiement facture : compte ACTIVE, solde suffisant, montant > 0 |
| R10 | Référence facture (contrat / facture) obligatoire |
| R11 | Chaque paiement génère une transaction tracée + audit |

### Comment tester (Phase 10)
```bash
mvn test
mvn spring-boot:run
```
| Vérification | OK ? |
|---|---|
| Dépôt 500 MAD sur compte courant actif | ✓ |
| Paiement LYDEC 150 MAD avec référence → solde = 350 | ✓ |
| Paiement 400 MAD → refusé (solde insuffisant) | ✓ |
| Reçu affiche facturier + référence + montant | ✓ |
| Historique filtre `PAIEMENT_FACTURE` | ✓ |
| Journal d'audit contient l'opération | ✓ |
| `mvn test` passe | ✓ |

### Commit & push
```bash
git add .
git commit -m "feat(bills): add bill payment with receipt and provider catalog"
git push origin main
```

### Critères de sortie
- Paiement facture démontrable de bout en bout
- Reçu réutilisé sans duplication de logique PDF

---

## Phase 11 — Commande de chéquier

**Objectif :** Permettre à l'agent d'enregistrer une **demande de chéquier** pour un compte éligible, avec suivi du workflow **PENDING → PROCESSING → DELIVERED** (ou annulation).

**Référence :** Extension **v1.1** — service bancaire courant, **sans mouvement financier** sur le solde.

**Conception :** `documentation/uml/sequence/07-commande-chequier.puml`, entité `checkbook_orders`, enum `CheckbookOrderStatus`.

### Étapes détaillées

#### Étape 11.1 — Base de données
- [x] Migration `V8__create_checkbook_orders.sql` :
  - Enum `checkbook_order_status` : PENDING, PROCESSING, DELIVERED, CANCELLED
  - Table `checkbook_orders` : id, order_number (unique), account_id FK, client_id FK, quantity (défaut 1), status, requested_at, processed_at, delivered_at, requested_by FK users, notes
- [x] Migration `V9__add_checkbook_sheet_count.sql` : colonne `sheet_count` (FEUILLES_20 | FEUILLES_40, défaut FEUILLES_20)
- [x] Index : `checkbook_orders(account_id)`, `checkbook_orders(status)`

#### Étape 11.2 — Backend
- [x] Entité `CheckbookOrder` + enums `CheckbookOrderStatus`, `CheckbookSheetCount`
- [x] `CheckbookOrderService` :
  - **requestCheckbook(accountId, quantity, sheetCount, user)** — génère n° (ex. CHQ-00001)
  - Compte **COURANT** ou **PROFESSIONNEL** et **ACTIVE** uniquement (pas épargne)
  - Une seule commande **PENDING** par compte à la fois
  - **updateStatus(orderId, newStatus, user)** — transitions valides + dates
  - Audit : `CHECKBOOK_ORDER_CREATED`, `CHECKBOOK_ORDER_STATUS_CHANGED`
- [x] Pas de modification du solde

#### Étape 11.3 — Interface
- [x] Depuis fiche compte : bouton **Commander un chéquier** (si éligible), choix **20 ou 40 feuillets**
- [x] `GET /checkbook-orders` — liste paginée avec filtre statut
- [x] Fiche commande : détail + actions changer statut (agent / chef)
- [x] Sur fiche client : liste des commandes liées

#### Étape 11.4 — Tests
- [x] Test : commande sur compte épargne → refus
- [x] Test : deuxième commande PENDING sur même compte → refus
- [x] Test : workflow PENDING → PROCESSING → DELIVERED
- [x] Test : commande avec format 40 feuillets

### Règles métier (extension)

| # | Règle |
|---|---|
| R12 | Chéquier : compte COURANT ou PROFESSIONNEL, statut ACTIVE |
| R13 | Maximum une commande PENDING par compte |
| R14 | Pas d'impact sur le solde — traçabilité audit uniquement |

### Comment tester (Phase 11)
```bash
mvn test
mvn spring-boot:run
```
| Vérification | OK ? |
|---|---|
| Commander chéquier sur compte courant actif → statut PENDING | ✓ |
| Commander sur compte épargne → refus | ✓ |
| Deuxième demande PENDING → refus | ✓ |
| Passer en PROCESSING puis DELIVERED | ✓ |
| Commander chéquier 40 feuillets → sheet_count enregistré | ✓ |
| Liste des commandes filtrable par statut | ✓ |
| Audit journalisé | ✓ |
| `mvn test` passe | ✓ |

### Commit & push
```bash
git add .
git commit -m "feat(checkbook): add checkbook order workflow from account"
git push origin main
```

### Critères de sortie
- Workflow commande chéquier démontrable
- Distinction claire avec les opérations financières (pas de transaction)

---

## Phase 13 — Notifications & alertes métier (A + B)

**Objectif :** Améliorer la supervision sans complexifier le périmètre cahier — centre de notifications in-app et alertes sur le tableau de bord.

**Référence :** extension v1.2 (piste Canal client), workflow chéquier Phase 11.

### Partie A — Centre de notifications

- [x] Migration Flyway `V10__create_notifications.sql`
- [x] Entité `Notification`, enum `NotificationType`
- [x] `NotificationService` : création, liste paginée, compteur non lues, marquer lu / tout lu
- [x] Déclencheurs : nouvelle commande chéquier → tous les chefs actifs ; changement statut → agent demandeur (si ≠ acteur)
- [x] `NotificationController` : `/notifications`, POST marquer lu
- [x] UI : cloche + badge dans le layout, page liste, CSS
- [x] `NotificationModelAdvice` : `unreadNotificationCount` sur toutes les pages

### Partie B — Alertes tableau de bord

- [x] `DashboardStats` : `pendingCheckbookOrders`, `processingCheckbookOrders`
- [x] `CheckbookOrderRepository.countByStatus`
- [x] Bandeau **Alertes métier** sur `/dashboard` avec liens filtrés `/checkbook-orders?status=...`

### Documentation & conception

- [x] MCD / MLD / `schema-relationnel.sql` / dictionnaire — table `notifications`
- [x] Diagramme de classes : `Notification`, `NotificationType`
- [x] Cas d'utilisation `02-operations-supervision` : notifications + alertes
- [x] Séquences `07-commande-chequier` (mise à jour), `08-notifications` (nouveau)
- [x] `TECHNICAL.md`, `manuel-utilisateur.md`, `demo-script.md`, `demo-reset.sql`
- [x] Régénérer les SVG PlantUML

### Tests

- [x] `NotificationIntegrationTest` (création, statut, page, marquer lu, dashboard)
- [x] Nettoyage `notifications` dans les tests d'intégration existants

### Comment tester (Phase 13)
```bash
mvn test
mvn spring-boot:run
# Agent : créer une commande chéquier
# Chef : cloche badge → /notifications → dashboard alertes
```

### Commit & push
```bash
git add .
git commit -m "feat(phase-13): add notification center and dashboard checkbook alerts"
git push origin main
```

### Critères de sortie
- Notifications visibles et marquables comme lues
- Dashboard affiche les alertes chéquier avec liens filtrés
- Tests verts, conception alignée

---

## Phase 14 — Portail client & notifications client

**Objectif :** Nouvel acteur **Client bancaire** avec espace en ligne (`/portal/**`) : consultation + notifications sur les opérations effectuées en agence.

**Référence :** évolution v2 — acteur Client du cahier des charges.

### 14.1 — Authentification client

- [x] Migration Flyway `V11__client_portal.sql` (`password_hash`, `portal_enabled`, `last_login_at`)
- [x] `notifications.client_id` (destinataire client ou personnel)
- [x] `SecurityClient` + chargement composite dans `UserDetailsServiceImpl`
- [x] Spring Security : `/portal/**` → `ROLE_CLIENT`, personnel → routes agence
- [x] Redirection post-login : client → `/portal/dashboard`, personnel → `/dashboard`

### 14.2 — Espace client (consultation)

- [x] Tableau de bord : comptes, soldes, opérations récentes
- [x] Mes comptes, historique filtré, reçus imprimables
- [x] Suivi commandes chéquier
- [x] Layout dédié `portal/layout.html`

### 14.3 — Notifications client

- [x] Types : dépôt, retrait, virement (émis/reçu), facture, chéquier (demande + statut)
- [x] Déclenchement dans `TransactionService`, `BillPaymentService`, `CheckbookOrderService`
- [x] Centre `/portal/notifications` (cloche + badge)

### 14.4 — Démo & tests

- [x] `DemoPortalSync` — portail Ahmed / Youssef (`client123`), resync à chaque démarrage
- [x] `PortalIntegrationTest` (5 tests) — login, accès, isolation, notifications

### 14.5 — Documentation & conception

- [x] `03-portail-client.puml`, `09-portail-client-notifications.puml`
- [x] MLD / schéma / dictionnaire / diagramme de classes
- [x] ROADMAP, TECHNICAL, manuel, demo-script, demo-data
- [x] Régénérer les SVG PlantUML

### Comment tester (Phase 14)
```bash
mvn test
mvn spring-boot:run
# Agent : dépôt sur compte Ahmed
# Login client : CD789012 / client123 → /portal/dashboard → notifications
```

### Commit & push
```bash
git add .
git commit -m "feat(phase-14): add client portal with operation notifications"
git push origin main
```

---

## Phase 12 — Finalisation & préparation soutenance

> **Piste Clôture — à traiter en dernier**, après les phases 13–14. Le numéro **12** est historique ; l'ordre logique place cette phase à la fin du projet.

**Objectif :** Projet propre, documenté, prêt à présenter.

**Référence cahier :** §13 (livrables), §15 (conclusion).

### Étapes détaillées

#### Étape 12.1 — Données de démonstration
- [x] Seed modulaire (profil dev) :
  - `DevUserInitializer` — personnel (si `users` vide)
  - `DevDemoDataInitializer` — clients, comptes, transactions… (si `clients` vide)
  - `DemoPortalSync` — portail Ahmed / Youssef (à chaque démarrage si manquant)
- [x] Script `documentation/demo-reset.sql` + guide [demo-data.md](demo-data.md)
- [x] Identifiants de test documentés dans le README

#### Étape 12.2 — Qualité
- [x] Libellés UI en français (revue globale)
- [x] Messages d'erreur métier explicites (`BusinessRuleException`)
- [x] `mvn test` — tous les tests passent
- [ ] Parcours complet manuel (suivre demo-script.md avant soutenance)

#### Étape 12.3 — Documentation
- [x] `documentation/manuel-utilisateur.md`
- [x] `documentation/demo-script.md` — scénario 5–7 min
- [x] `TECHNICAL.md` §7 checklist (tout en Done)
- [x] README final : installation, lancement, comptes de test

#### Étape 12.4 — Rapport & présentation
- [x] `documentation/presentation-outline.md` — structure slides + checklist rapport
- [ ] Intégrer les diagrammes SVG dans le rapport Word/PDF
- [ ] Rédiger le PowerPoint final (contenu guidé par presentation-outline.md)
- [ ] Vérifier citations cahier des charges dans le rapport écrit

#### Étape 12.5 — Revue cahier des charges
- [x] Checklist finale documentée dans [presentation-outline.md](presentation-outline.md)

### Comment tester (Phase 12)
```bash
mvn clean test
mvn spring-boot:run
# Suivre documentation/demo-script.md du début à la fin
# Vérifier CD789012 / client123 après redémarrage (DemoPortalSync)
```

### Commit & push
```bash
git add .
git commit -m "docs: finalize demo, manual and presentation materials"
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
10. **Paiement facture** LYDEC 200 MAD → reçu avec référence
11. **Commander chéquier** sur compte courant → chef notifié → suivre statut jusqu'à LIVRÉE
12. **Notifications** : cloche, marquer lu, alertes dashboard
13. Login **client** `CD789012` / `client123` → **espace client** → notification après dépôt agent

---

## Ce qu'on ne fait PAS (volontairement)

- Docker / Kubernetes
- Python / Flask / Django
- React / Angular séparé
- API REST publique / Swagger (sauf si utile en interne — pas nécessaire)
- Microservices
- Octroi / gestion de **crédit** (hors périmètre retenu)
- Cartes bancaires / GAB
- CI/CD GitHub Actions (optionnel, pas requis)
- Tests à 100 % de couverture

---

## Prochaine action

**Phases 13–14 livrées — clôturer la Phase 12 (piste finale).**

1. **Redémarrer l'app** puis tester `CD789012` / `client123` (ou `demo-reset.sql`)
2. **Commit + push** : `feat(phase-13)` puis `feat(phase-14)` (ou un commit groupé)
3. Valider la **démo live** (`demo-script.md`)
4. Rédiger **rapport** et **PowerPoint**

### Mise à jour conception v2.1 (seed modulaire + auth dual)

- [x] Séquence `01-authentification` — login personnel ou client, redirections `/dashboard` vs `/portal/dashboard`
- [x] MCD — attributs portail sur CLIENT, relation CLIENT → NOTIFICATION
- [x] Dictionnaire — annexe seed dev (`DemoPortalSync`)
- [x] Régénérer les SVG PlantUML concernés

### Mise à jour conception v2 (Phase 14)

- [x] Acteur Client — `03-portail-client.puml`
- [x] Auth client + champs `clients` (portail)
- [x] `notifications.client_id` — MLD, schéma, dictionnaire
- [x] Séquence `09-portail-client-notifications`
- [x] Régénérer les SVG PlantUML

### Mise à jour conception v1.2 (Phase 13)

- [x] Table `notifications` (MCD, MLD, schéma, dictionnaire)
- [x] Diagramme de classes : `Notification`
- [x] Cas d'utilisation : notifications + alertes métier
- [x] Séquence `08-notifications` + mise à jour `07-commande-chequier`
- [x] Régénérer les SVG PlantUML

### Mise à jour conception v1.1 (documentation)

- [x] Cas d'utilisation : paiement facture, commande chéquier (`02-operations-supervision`, `01-clients-comptes`)
- [x] Diagramme de classes : `BillProvider`, `BillPayment`, `CheckbookOrder`
- [x] Séquences : `06-paiement-facture`, `07-commande-chequier`
- [x] MCD / MLD / schéma relationnel / dictionnaire étendus (dont `sheet_count` V9)
- [x] Régénérer les SVG PlantUML après modification des `.puml`

---

*Dernière mise à jour : 2026-06-14 (réorganisation pistes + seed modulaire)*
