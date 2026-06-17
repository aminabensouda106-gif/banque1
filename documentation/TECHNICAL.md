# TECHNICAL — Système de gestion d'agence bancaire (PFA)

> **Living document.** Update this file whenever architecture, stack, schema, or conventions change.
> Cursor and contributors should treat this as the single source of technical truth.

---

## 1. Project summary

| Field | Value |
|---|---|
| **Name** | Système web de gestion d'agence bancaire et des transactions clients |
| **Type** | PFA universitaire — application web de gestion métier |
| **Scope** | Gestion interne d'une agence (clients, comptes, transactions, utilisateurs, audit) |
| **Source of requirements** | `documentation/cahier-charge-PFA.pdf` |
| **Team** | Solo developer |
| **Status** | Phase 11 terminée — prêt pour Phase 12 (finalisation) |

### Problématique

Une agence bancaire traite quotidiennement des données clients, comptes et mouvements financiers. Sans outil centralisé, les risques sont des erreurs de saisie, des incohérences de solde et une faible traçabilité. Le projet vise une **application web unique** pour fiabiliser ces opérations.

### Objectifs (cahier §3–4)

- Gérer les fiches clients et leurs informations
- Ouvrir, consulter, bloquer et clôturer des comptes
- Réaliser dépôts, retraits et virements avec contrôle métier
- Consulter soldes et historiques de transactions
- Administrer les utilisateurs internes et leurs rôles
- Journaliser les opérations sensibles
- Produire relevés et reçus d'opération
- **(v1.1)** Payer des factures courantes (eau, électricité, télécom) avec reçu
- **(v1.1)** Gérer les commandes de chéquier (workflow statuts)

### Acteurs internes (cahier §6)

| Acteur | Rôle |
|---|---|
| **Administrateur** | Utilisateurs, droits, rôles, supervision globale |
| **Agent bancaire** | Clients, comptes, opérations courantes, historiques |
| **Chef d'agence** | Supervision, tableau de bord, journal d'audit |

> Acteur **Client** (portail en ligne) : hors périmètre v1.

### Scénarios principaux (cahier §12)

1. Authentification
2. Ajout d'un client
3. Ouverture d'un compte
4. Dépôt
5. Retrait
6. Virement
7. Consultation historique
8. Gestion utilisateurs

**Extensions v1.1 (hors cahier strict, côté agence) :**

9. Paiement de facture + reçu
10. Commande de chéquier

### Règles de gestion (cahier §8)

| # | Règle |
|---|---|
| R1 | Un client peut posséder un ou plusieurs comptes |
| R2 | Un compte appartient à un seul client |
| R3 | Retrait refusé si solde insuffisant |
| R4 | Virement : compte source et destination actifs |
| R5 | Chaque transaction : date, heure, utilisateur exécutant |
| R6 | Compte bloqué ou clôturé : pas d'opérations ordinaires |
| R7 | Mots de passe stockés chiffrés (BCrypt) |
| R8 | Opérations importantes → journal d'audit |

**Extensions v1.1 :**

| # | Règle |
|---|---|
| R9 | Paiement facture : compte ACTIVE, solde suffisant |
| R10 | Référence facture obligatoire |
| R11 | Chéquier : compte COURANT ou PROFESSIONNEL actif uniquement |
| R12 | Une seule commande PENDING par compte |
| R13 | Commande chéquier : pas d'impact sur le solde |

### Diagrammes de cas d'utilisation (étape 1.2)

2 diagrammes PlantUML dans `documentation/uml/cas-utilisation/` (SVG uniquement) :

| Fichier | Contenu |
|---|---|
| `01-clients-comptes.puml` | Accès (auth), clients (§7.1), comptes (§7.2), **commande chéquier (v1.1)** |
| `02-operations-supervision.puml` | Transactions (§7.3), **paiement facture (v1.1)**, utilisateurs (§7.4), reporting (§7.5) |

Acteur générique **Personnel de l'agence** (Admin, Agent, Chef en spécialisation) pour éviter les liens redondants.

Exports : `.svg` à côté de chaque `.puml`.

### Diagramme de classes (étape 1.3)

- `documentation/uml/diagramme-classes.puml` (+ SVG)
- 7 entités + 6 énumérations, relations et notes R1–R2, R5, extensions v1.1

### Diagrammes de séquence (étape 1.4)

| Fichier | Scénarios cahier couverts |
|---|---|
| `01-authentification` | Authentification |
| `02-creation-client-compte` | Ajout client, Ouverture compte |
| `03-operations-financieres` | Dépôt, Retrait, Virement |
| `04-consultation-historique` | Consultation historique, relevé, reçu |
| `05-gestion-utilisateurs` | Gestion utilisateurs |
| `06-paiement-facture` | Paiement facture + reçu (v1.1) |
| `07-commande-chequier` | Commande chéquier (v1.1) |

### Couverture cahier des charges (diagrammes UML)

| Exigence | Couvert par |
|---|---|
| 3 acteurs (admin, agent, chef) | Cas d'utilisation |
| Gestion clients (CRUD, recherche, statuts) | UC 01 + séquence 02 |
| Gestion comptes (types, solde, blocage, clôture, liste) | UC 01 + séquence 02 |
| Transactions (dépôt, retrait, virement, historique) | UC 02 + séquence 03–04 |
| Utilisateurs (création, rôles, activation) | UC 02 + séquence 05 |
| Reporting (dashboard, opérations récentes, relevés) | UC 02 + séquence 04 |
| Supervision chef (validation, audit) | UC 02 |
| Paiement facture (v1.1) | UC 02 + séquence 06 |
| Commande chéquier (v1.1) | UC 01 + séquence 07 |
| 8 règles de gestion + extensions R9–R13 | UC légendes, séquences alt, classe |
| Auth BCrypt, traçabilité | Séquences 01, 03, 05 |

### Modèle de données (étape 1.5)

| Fichier | Contenu |
|---|---|
| `modele-donnees/MCD.puml` | Modèle conceptuel (+ SVG) |
| `modele-donnees/MLD.puml` | Modèle logique (+ SVG) |
| `modele-donnees/schema-relationnel.sql` | Schéma PostgreSQL de référence |
| `modele-donnees/dictionnaire-donnees.md` | Dictionnaire de données |

### Diagramme d'activité (étape 1.6)

- `uml/diagramme-activite-virement.puml` (+ SVG)

**Régénérer les SVG :**
```bash
python scripts/generate-plantuml-svgs.py
```
Alternative locale si PlantUML est installé :
```bash
java -jar plantuml.jar -tsvg documentation/modele-donnees/*.puml documentation/uml/**/*.puml
```

### Out of scope (per cahier des charges)

- Interconnexion interbancaire nationale
- Gestion avancée des **crédits** (explicitement exclu du projet)
- Cartes bancaires et GAB réels
- Signature électronique qualifiée
- Portail client (optional future evolution only)

### Extensions v1.1 (in scope, agency-side)

| Feature | Phase | Notes |
|---|---|---|
| Paiement facture + reçu | 10 | Débit compte, catalogue facturiers, réutilise reçu transaction |
| Commande chéquier | 11 | Workflow statuts, pas de mouvement financier |

---

## 2. Technology stack

### Chosen stack

| Layer | Choice | Version target |
|---|---|---|
| **Language** | Java | 21 LTS |
| **Backend** | Spring Boot | 3.4.x |
| **Security** | Spring Security | (Boot starter) |
| **Persistence** | Spring Data JPA + Hibernate | (Boot starter) |
| **Database** | PostgreSQL | **18** (port local **5433** sur cette machine) |
| **Migrations** | Flyway | latest stable |
| **Frontend** | Thymeleaf + Bootstrap | Bootstrap 5.3 |
| **Build** | Maven | 3.9+ |
| **PDF export** | OpenPDF or iText (later phase) | TBD |
| **Tests** | JUnit 5 + Spring Boot Test | minimal set |
| **VCS** | Git + GitHub | — |

### Why this stack (solo, clean, PFA-friendly)

1. **Monolith** — one repo, one deployable JAR; no separate SPA build pipeline.
2. **Spring Boot** — matches cahier recommendation; strong fit for banking (security, transactions, layered architecture).
3. **Thymeleaf** — server-rendered UI; fast to iterate; no React state complexity for CRUD-heavy screens.
4. **PostgreSQL** — robust relational DB; good for financial consistency and constraints.
5. **Flyway** — versioned SQL; reproducible schema; easy to demo and defend at soutenance.

### Explicitly excluded

| Option | Reason |
|---|---|
| **Python** (Flask/Django) | Not used — project is Java-only per team decision |
| Spring Boot + React | Two codebases; unnecessary for a school CRUD app |
| Docker | Not needed — local PostgreSQL install is enough |
| Microservices / CI complexe | Overkill for a PFA |

### Alternatives considered (not chosen)

| Option | Reason not chosen |
|---|---|
| MySQL | Equally valid per cahier; PostgreSQL chosen for constraints and academic demo |

---

## 3. Architecture

### Style

**Layered monolith** with clear package boundaries:

```
HTTP Request
    → Controller (web layer)
    → Service (business rules)
    → Repository (persistence)
    → Database
```

Cross-cutting: Security filter chain, audit logging, validation, exception handling.

### Target package structure

```
com.banque.agence
├── BanqueAgenceApplication.java
├── config/           # Security, JPA, Flyway, web config
├── domain/
│   ├── entity/       # JPA entities
│   └── enums/        # AccountType, AccountStatus, TransactionType, UserRole, etc.
├── repository/       # Spring Data interfaces
├── service/          # Business logic + @Transactional
├── web/
│   ├── controller/   # MVC controllers
│   ├── dto/          # Form objects / view models (if needed)
│   └── advice/       # @ControllerAdvice, global errors
├── security/         # UserDetails, auth helpers
└── audit/            # Audit log service + entity
```

### Design principles

- **Business rules live in services**, never only in controllers.
- **Controllers stay thin** — validate input, call service, return view/redirect.
- **Repositories** — no business logic; query methods only.
- **Use `@Transactional`** on service methods that modify balances or create linked records.
- **Optimistic or pessimistic locking** on `Account` balance updates (decide in Phase 4; default: `@Version` optimistic lock).

---

## 4. Actors and authorization

### Roles (from cahier des charges)

| Role | Code | Capabilities |
|---|---|---|
| Administrateur | `ADMIN` | Full system: users, roles, params, supervision |
| Agent bancaire | `AGENT` | Clients, accounts, daily operations, history read |
| Chef d'agence | `CHEF_AGENCE` | Supervision, dashboards, validate sensitive ops |

### Spring Security mapping (planned)

| Area | ADMIN | CHEF_AGENCE | AGENT |
|---|---|---|---|
| Dashboard | ✓ | ✓ | ✓ (limited) |
| Clients CRUD | ✓ | ✓ | ✓ |
| Accounts CRUD | ✓ | ✓ | ✓ |
| Transactions | ✓ | ✓ | ✓ |
| Bill payment | ✓ | ✓ | ✓ |
| Checkbook orders | ✓ | ✓ | ✓ |
| Sensitive validation | ✓ | ✓ | — |
| User management | ✓ | read | — |
| Audit log | ✓ | ✓ | — |

**Client** actor is **not implemented in v1** (optional future).

### Password policy

- Stored with **BCrypt** only — never plain text (règle de gestion #7).
- Minimum length: 8 characters (configurable in `application.yml`).

---

## 5. Domain model (entities)

### Core entities

```
User (internal staff)
Client
Account
Transaction
BillProvider
BillPayment
CheckbookOrder
AuditLog
```

### Entity sketch

#### `Client`
- `id`, `clientNumber` (unique), `cin` (unique), `firstName`, `lastName`
- `email`, `phone`, `address`
- `professionalInfo` (optional text or structured fields)
- `status`: ACTIVE | SUSPENDED | INACTIVE
- `createdAt`, `updatedAt`

#### `Account`
- `id`, `accountNumber` (unique), `type`: COURANT | EPARGNE | PROFESSIONNEL
- `status`: ACTIVE | BLOCKED | CLOSED
- `balance` (Decimal — `BigDecimal`, never `double`)
- `client` (ManyToOne)
- `openedAt`, `closedAt` (nullable)
- `version` (for optimistic locking)

#### `Transaction`
- `id`, `type`: DEPOT | RETRAIT | VIREMENT | **PAIEMENT_FACTURE**
- `amount` (`BigDecimal`, positive)
- `sourceAccount` (nullable for DEPOT)
- `destinationAccount` (nullable for RETRAIT)
- `executedBy` (User)
- `executedAt` (timestamp)
- `reference` / `description` (optional)
- For VIREMENT: link both accounts; single transaction record or paired entries — **decision: single record with source + destination** (simpler history)
- For **PAIEMENT_FACTURE**: `sourceAccount` = compte client débité ; détails facturier dans `BillPayment`

#### `BillProvider` (v1.1)
- `id`, `code` (unique), `name`, `category`, `active`

#### `BillPayment` (v1.1)
- `id`, `account`, `billProvider`, `clientReference`, `amount`
- `transaction` (OneToOne, type PAIEMENT_FACTURE)
- `createdAt`

#### `CheckbookOrder` (v1.1)
- `id`, `orderNumber` (unique), `account`, `client`, `quantity`
- `sheetCount`: FEUILLES_20 | FEUILLES_40 (20 ou 40 feuillets par chéquier)
- `status`: PENDING | PROCESSING | DELIVERED | CANCELLED
- `requestedAt`, `processedAt`, `deliveredAt`
- `requestedBy` (User), `notes` (optional)

#### `User` (staff)
- `id`, `username` (unique), `passwordHash`, `fullName`, `email`
- `role`: ADMIN | AGENT | CHEF_AGENCE
- `enabled` (boolean)
- `createdAt`, `lastLoginAt`

#### `AuditLog`
- `id`, `action` (enum or string code)
- `entityType`, `entityId`
- `performedBy` (User)
- `performedAt`
- `details` (JSON or text snapshot)

### Relationships (business rules)

1. Client **1 — N** Account
2. Account **N — 1** Client
3. Transaction **N — 1** User (executor)
4. Withdrawal blocked if `balance < amount`
5. Transfer requires both accounts **ACTIVE**
6. BLOCKED or CLOSED account → no ordinary operations
7. Bill payment debits account like withdraw; receipt via linked `Transaction`
8. Checkbook order does not modify balance

### Planned migrations (v1.1)

| Version | Content |
|---|---|
| V7 | `bill_providers`, `bill_payments`, extend `transaction_type` |
| V8 | `checkbook_orders`, enum `checkbook_order_status` |
| V9 | `checkbook_orders.sheet_count` (FEUILLES_20 \| FEUILLES_40) |

---

## 6. Database

### Naming conventions

- Tables: `snake_case`, plural (`clients`, `accounts`, `transactions`, `users`, `audit_logs`)
- PK: `id` (bigint, identity)
- FK columns: `{entity}_id`
- Money: `NUMERIC(19, 4)` mapped to `BigDecimal`
- Timestamps: `TIMESTAMP WITH TIME ZONE` → `Instant` or `LocalDateTime` (pick one project-wide; **default: `Instant`**)

### Flyway layout

```
src/main/resources/db/migration/
├── V1__init_schema.sql
├── V2__seed_roles_and_admin.sql   # dev/demo seed
└── V3__...                        # incremental only, never edit old migrations
```

### Indexes (planned)

- `clients(cin)`, `clients(client_number)`, `clients(last_name, first_name)`
- `accounts(account_number)`, `accounts(client_id)`
- `transactions(executed_at)`, `transactions(source_account_id)`, `transactions(destination_account_id)`
- `audit_logs(performed_at)`

---

## 7. Main user flows (implementation checklist)

| # | Flow | Priority | Status |
|---|---|---|---|
| 1 | Login / logout | P0 | Done (Phase 3) |
| 2 | Dashboard | P1 | Done (Phase 8) |
| 3 | Add / edit / search client | P0 | Done (Phase 4) |
| 4 | Open account for client | P0 | Done (Phase 5) |
| 5 | Deposit | P0 | Done (Phase 6) |
| 6 | Withdraw (balance check) | P0 | Done (Phase 6) |
| 7 | Transfer | P0 | Done (Phase 6) |
| 8 | Transaction history + filters | P1 | Done (Phase 6) |
| 9 | User management (admin) | P1 | Done (Phase 7) |
| 10 | Audit log | P1 | Done (Phase 8) |
| 11 | Statement / receipt PDF | P2 | Done (Phase 8 — HTML imprimable) |
| 12 | Bill payment + receipt | P1 | Done (Phase 10) |
| 13 | Checkbook order workflow | P2 | Done (Phase 11) |
| 14 | Demo seed data (dev) | P2 | Done (Phase 12) |
| 15 | User manual + demo script | P2 | Done (Phase 12) |

---

## 8. UI conventions

- **Bootstrap 5** layout: sidebar nav + top bar with user/role.
- **French** UI labels (application métier).
- Flash messages via `RedirectAttributes` for success/error.
- Forms: server-side validation (`@Valid` + Bean Validation).
- Tables: pagination for clients, accounts, transactions (Spring Data `Pageable`).
- Empty states and explicit error messages (ergonomie NFR).

### Key screens

1. Login
2. Dashboard (KPIs: clients count, accounts, today's transactions, recent ops)
3. Clients — list, search, create, edit, detail
4. Accounts — list, open, detail, block/unblock, close
5. Operations — deposit, withdraw, transfer forms
6. History — filterable transaction list
7. Users — admin CRUD
8. Audit — read-only log
9. Bill payment — form (account, provider, reference, amount)
10. Checkbook orders — list, request from account, status updates

---

## 9. Configuration

### Profiles

| Profile | Purpose |
|---|---|
| `dev` | Local PostgreSQL, SQL logging optional, seed data |
| `prod` | Hardened settings, no seed, secrets from env |

### Environment variables (prod)

```
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
SPRING_PROFILES_ACTIVE=prod
```

### Local dev setup (no Docker)

1. Install **PostgreSQL 18** locally.
2. Sur ce PC, PG 18 écoute le port **5433** (Odoo occupe le 5432).
3. Create database and user with `documentation/setup-postgresql.sql` :
   ```bash
   psql -U postgres -h localhost -p 5433 -f documentation/setup-postgresql.sql
   ```
4. Configure `application-dev.yml` :

- DB: `jdbc:postgresql://localhost:5433/banque_agence`
- User: `banque` / password: `banque` (dev only)

5. Lancer l'application : `mvn spring-boot:run`
6. Ouvrir **http://localhost:8081** (port par défaut dans `application.yml`)

> Sur la machine de dev du projet, le port **8080** est occupé par le serveur web EnterpriseDB (PostgreSQL). Voir `documentation/postgresql-local.md`.

## 10. Repository layout (target)

```
banque/
├── README.md                 ← quick start (add when app boots)
├── documentation/
│   ├── TECHNICAL.md          ← this file
│   ├── ROADMAP.md            ← phased plan & milestones
│   ├── cahier-charge-PFA.pdf
│   ├── uml/                  ← (to add)
│   └── modele-donnees/       ← (to add)
├── src/
│   ├── main/
│   │   ├── java/com/banque/agence/...
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── static/       # css, js
│   │       ├── templates/    # thymeleaf
│   │       └── db/migration/
│   └── test/
├── pom.xml
└── .gitignore
```

---

## 11. Git workflow (solo, clean commits)

### Branch strategy

- `main` — always runnable, demo-ready
- Short-lived feature branches optional: `feat/clients`, `fix/withdraw-balance`
- Merge to `main` when a vertical slice works

### Commit message format

```
<type>(<scope>): <short description>

[optional body]
```

**Types:** `feat`, `fix`, `refactor`, `docs`, `test`, `chore`, `db`

**Examples:**
- `feat(auth): add Spring Security login and role-based access`
- `feat(clients): implement client search by CIN and phone`
- `db(migration): add accounts and transactions tables`
- `docs(technical): document transaction locking strategy`

### Commit rules

1. One logical change per commit.
2. Each commit should leave the project buildable when possible.
3. Never commit secrets (`.env`, real passwords).
4. Update `documentation/TECHNICAL.md` when a **decision** changes (see §13).

---

## 12. Testing strategy (minimal but defensible)

| Level | What |
|---|---|
| Unit | Service rules: insufficient balance, blocked account, transfer validation |
| Integration | Repository + `@DataJpaTest` for critical queries |
| Web | `@SpringBootTest` or MockMvc for login + one happy path per major flow |

Target: **~10–15 focused tests**, not 100% coverage.

---

## 13. Decision log

| Date | Decision | Rationale |
|---|---|---|
| 2026-06-14 | Stack: Spring Boot 3 + Thymeleaf + PostgreSQL + Flyway | Monolith, solo-friendly, matches cahier, strong security story |
| 2026-06-14 | Money as `BigDecimal` | Avoid floating-point errors |
| 2026-06-14 | Virement = single `Transaction` row with source + destination | Simpler history and reporting |
| 2026-06-14 | Client portal deferred to v2 | Per cahier: client is optional actor |
| 2026-06-14 | Docs live under `documentation/` | Keeps project root clean; all PFA docs in one place |
| 2026-06-14 | No Python, no Docker | School project — keep stack simple, Java + local PostgreSQL |
| 2026-06-14 | Phase 1 = UML conception before any code | Required by cahier §11; see `ROADMAP.md` Phase 1 |
| 2026-06-14 | PostgreSQL 18 on port 5433 | Port 5432 is Odoo PG 9.5 on this machine |
| 2026-06-14 | Dev users seeded via `DevUserInitializer` | BCrypt hashes in Java, not SQL — easier to maintain |
| 2026-06-14 | Flyway V2 creates `users` + `user_role` enum | Aligns with `schema-relationnel.sql` |
| 2026-06-15 | App HTTP port 8081 (not 8080) | Port 8080 used by EDB httpd bundled with PostgreSQL on dev machine |
| 2026-06-15 | Client number format CLI-00001 | Auto-increment from last client in DB |
| 2026-06-15 | Audit log on client create/update/status | First use of `audit_logs` table (Phase 4) |
| 2026-06-15 | Account number format ACC-00001 | Auto-increment from last account in DB |
| 2026-06-15 | Close account requires zero balance | Prevents orphaned funds at closure |
| 2026-06-15 | Transaction history via JPA Specifications | Avoids PostgreSQL enum typing issue with nullable JPQL params |
| 2026-06-15 | Flyway V6 creates `transactions` + `transaction_type` enum | Aligns with schema-relationnel.sql |
| 2026-06-15 | Admin user CRUD at `/admin/users/**` with `@PreAuthorize` | Phase 7 — role assignment, enable/disable, password reset |
| 2026-06-15 | Block disable/demote of last active ADMIN | Prevents lockout of administration |
| 2026-06-15 | Dashboard KPIs via `DashboardService` | Phase 8 — active clients/accounts, today's volume |
| 2026-06-15 | Account statement at `/accounts/{id}/statement` | Phase 8 — date filter + current balance header |
| 2026-06-15 | Printable receipt at `/transactions/{id}/receipt` | Phase 8 — HTML print, no OpenPDF dependency |
| 2026-06-15 | Audit log view at `/admin/audit` for ADMIN + CHEF | Phase 8 — paginated consultation |
| 2026-06-14 | Extensions v1.1 : paiement facture + commande chéquier | Enrichissement PFA côté agence ; crédit et portail client exclus |
| 2026-06-14 | `BillPayment` lié 1–1 à `Transaction` PAIEMENT_FACTURE | Réutilise historique et reçu existants |
| 2026-06-14 | `CheckbookOrder` sans impact solde | Service administratif distinct des opérations financières |
| 2026-06-14 | Catalogue `bill_providers` seed (LYDEC, ONEE, IAM…) | Liste fermée pour formulaire agent |
| 2026-06-17 | Bill payment at `/operations/bill-payment` | Phase 10 — `BillPaymentService`, Flyway V7, redirect to receipt |
| 2026-06-17 | Receipt extended with provider + reference | `BillPayment` loaded by transaction id on receipt page |
| 2026-06-17 | Checkbook orders at `/checkbook-orders/**` | Phase 11 — workflow PENDING→PROCESSING→DELIVERED, no balance impact |
| 2026-06-17 | `CheckbookSheetCount` 20/40 feuillets | Flyway V9 — choix à la demande, affiché dans listes et fiches |
| 2026-06-17 | `DevDemoDataInitializer` | Phase 12 — seed dev idempotent (5 clients, 8 comptes, 20 tx, 2 bills, 2 chq) |
| 2026-06-17 | Docs soutenance | `manuel-utilisateur.md`, `demo-script.md`, `presentation-outline.md`, README |

---

## 14. Non-functional requirements checklist

- [x] Authentication + role-based access (Spring Security)
- [x] Password hashing (BCrypt)
- [x] Audit log for sensitive actions
- [x] Input validation on all forms
- [x] Transactional integrity on financial operations
- [x] Clear UI messages (success/error)
- [x] Modular packages and documented setup
- [x] Responsive Bootstrap layout

---

## 15. Deliverables mapping (PFA)

| Cahier deliverable | Where it lives |
|---|---|
| Code source | `src/` + GitHub |
| Scripts SQL | `src/main/resources/db/migration/` |
| Interfaces | `src/main/resources/templates/` |
| UML / MCD / MLD | `documentation/uml/`, `documentation/modele-donnees/` |
| Manuel utilisateur | `documentation/manuel-utilisateur.md` (later) |
| Rapport | External to repo or `documentation/rapport/` |
| Tests | `src/test/` |

---

## 16. How to keep this file useful for Cursor

When starting a new session or feature:

1. Read `documentation/TECHNICAL.md` + `documentation/ROADMAP.md` + current phase in §7 checklist.
2. After implementing, update **§7 status**, **§13 decision log**, and any schema changes in **§5–6**.
3. Do not duplicate long specs here — reference `documentation/cahier-charge-PFA.pdf` for functional requirements.

---

*Last updated: 2026-06-15 — Phase 8 (reporting)*
