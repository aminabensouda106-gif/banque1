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
| **Status** | Phase 1 — Conception (no code yet) |

### Out of scope (per cahier des charges)

- Interconnexion interbancaire nationale
- Gestion avancée des crédits
- Cartes bancaires et GAB réels
- Signature électronique qualifiée
- Portail client (optional future evolution only)

---

## 2. Technology stack

### Chosen stack

| Layer | Choice | Version target |
|---|---|---|
| **Language** | Java | 21 LTS |
| **Backend** | Spring Boot | 3.4.x |
| **Security** | Spring Security | (Boot starter) |
| **Persistence** | Spring Data JPA + Hibernate | (Boot starter) |
| **Database** | PostgreSQL | 16+ |
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
- `id`, `type`: DEPOT | RETRAIT | VIREMENT
- `amount` (`BigDecimal`, positive)
- `sourceAccount` (nullable for DEPOT)
- `destinationAccount` (nullable for RETRAIT)
- `executedBy` (User)
- `executedAt` (timestamp)
- `reference` / `description` (optional)
- For VIREMENT: link both accounts; single transaction record or paired entries — **decision: single record with source + destination** (simpler history)

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
| 1 | Login / logout | P0 | Not started |
| 2 | Dashboard | P1 | Not started |
| 3 | Add / edit / search client | P0 | Not started |
| 4 | Open account for client | P0 | Not started |
| 5 | Deposit | P0 | Not started |
| 6 | Withdraw (balance check) | P0 | Not started |
| 7 | Transfer | P0 | Not started |
| 8 | Transaction history + filters | P1 | Not started |
| 9 | User management (admin) | P1 | Not started |
| 10 | Audit log | P1 | Not started |
| 11 | Statement / receipt PDF | P2 | Not started |

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

1. Install PostgreSQL locally (Windows installer or existing school setup).
2. Create database and user manually (see `ROADMAP.md` Phase 2).
3. Configure `application-dev.yml`:

- DB: `jdbc:postgresql://localhost:5432/banque_agence`
- User: `banque` / password: `banque` (dev only)

---

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
| TBD | Optimistic vs pessimistic lock on balance | To validate under concurrent withdraw tests |

---

## 14. Non-functional requirements checklist

- [ ] Authentication + role-based access (Spring Security)
- [ ] Password hashing (BCrypt)
- [ ] Audit log for sensitive actions
- [ ] Input validation on all forms
- [ ] Transactional integrity on financial operations
- [ ] Clear UI messages (success/error)
- [ ] Modular packages and documented setup
- [ ] Responsive Bootstrap layout

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

*Last updated: 2026-06-14 — Planning phase*
