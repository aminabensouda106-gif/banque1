# ROADMAP — Agence bancaire PFA

> Phased plan for **solo development**: detailed steps, **run & test after every phase**, then **commit and push to GitHub**.
> Technical reference: [`TECHNICAL.md`](./TECHNICAL.md).

---

## Stack reminder

| Used | Not used |
|---|---|
| Java 21, Spring Boot 3, Maven | **Python** (Flask, Django, scripts) |
| Thymeleaf, Bootstrap 5, HTML/CSS/JS | React, separate SPA |
| PostgreSQL, Flyway, Spring Security | MySQL (optional only) |

Every phase ends with a **runnable, testable** application on `main`.

---

## Workflow after each phase

Repeat this checklist at the end of **every** phase before moving on:

### 1. Build & automated tests

```bash
./mvnw clean test
```

- [ ] Build succeeds (`BUILD SUCCESS`)
- [ ] All existing tests pass (0 failures)

### 2. Run locally

```bash
# Start PostgreSQL (Docker example)
docker run -d --name banque-pg -e POSTGRES_DB=banque_agence -e POSTGRES_USER=banque -e POSTGRES_PASSWORD=banque -p 5432:5432 postgres:16

./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

- [ ] App starts on `http://localhost:8080` without stack trace
- [ ] Flyway migrations apply cleanly (check console logs)

### 3. Manual smoke test

- [ ] Complete the **Manual test checklist** for the current phase (below)
- [ ] No regression on features from previous phases

### 4. Git commit & GitHub push

```bash
git status
git add -A
git commit -m "feat(<scope>): phase N — <short description>"
git push origin main
```

**Rules:**
- One phase = at least **one meaningful commit** on `main` (multiple small commits during the phase are fine).
- Tag optional milestones: `git tag -a phase-2-clients -m "Phase 2 complete"`
- Never push secrets (`.env`, real passwords).
- Update `documentation/TECHNICAL.md` §7 checklist before pushing.

---

## Overview

```
Phase 0 ──► Phase 1 ──► Phase 2 ──► Phase 3 ──► Phase 4 ──► Phase 5 ──► Phase 6 ──► Phase 7
 Bootstrap   Auth+UI     Clients     Accounts    Transactions  Users       Dashboard   Ship
 ~1 day      ~2 days     ~2 days     ~2 days     ~3 days       ~2 days     ~2 days     ~2 days
```

**Global definition of done:** All P0 flows work, security + audit in place, `mvn test` green, demo rehearsed, GitHub history shows one clean commit per phase minimum.

---

# Phase 0 — Project bootstrap

**Goal:** Empty Spring Boot shell that compiles, runs, and connects to PostgreSQL.  
**Duration:** ~1 day  
**GitHub commit message:** `chore: phase 0 — Spring Boot bootstrap with PostgreSQL and Flyway`

---

## Step 0.1 — Initialize the Java project

- [ ] Create project via [start.spring.io](https://start.spring.io) or Maven manually
- [ ] Set `groupId`: `com.banque.agence`, `artifactId`: `banque-agence`
- [ ] Java **21**, packaging **Jar**, build **Maven**
- [ ] Add starters: Web, Security, JPA, Thymeleaf, Validation, Flyway, PostgreSQL, DevTools
- [ ] Add `BanqueAgenceApplication.java` main class
- [ ] Verify `./mvnw clean compile` succeeds

## Step 0.2 — Package structure

- [ ] Create empty packages per `TECHNICAL.md` §3:
  - `config`, `domain.entity`, `domain.enums`, `repository`, `service`
  - `web.controller`, `web.dto`, `web.advice`, `security`, `audit`
- [ ] Add placeholder `.gitkeep` or empty classes only if needed for compile

## Step 0.3 — Configuration files

- [ ] `src/main/resources/application.yml` — app name, default profile `dev`
- [ ] `src/main/resources/application-dev.yml` — datasource URL, user, password, JPA `ddl-auto: validate`, Flyway enabled
- [ ] `src/main/resources/application-prod.yml` — env-based datasource (no hardcoded secrets)

## Step 0.4 — Database & Docker helper

- [ ] Document PostgreSQL setup in `README.md` (local install + Docker one-liner)
- [ ] Optional: `docker-compose.yml` with PostgreSQL 16 service

## Step 0.5 — Project hygiene

- [ ] `.gitignore` — `target/`, `.idea/`, `.vscode/`, `*.iml`, `.env`
- [ ] `README.md` — prerequisites (Java 21, Maven, PostgreSQL), run commands
- [ ] Move planning docs already in `documentation/` (TECHNICAL, ROADMAP, cahier PDF)

## Step 0.6 — Minimal runnable endpoint

- [ ] Add `HomeController` returning a simple Thymeleaf page or static `index.html`: "Banque Agence — Phase 0 OK"
- [ ] Temporarily permit all requests in Security (full lockdown in Phase 1)

---

### Phase 0 — Manual test checklist

| # | Action | Expected result |
|---|---|---|
| 1 | `mvnw clean test` | BUILD SUCCESS (0 tests or 1 context test) |
| 2 | Start PostgreSQL | DB `banque_agence` reachable on port 5432 |
| 3 | `mvnw spring-boot:run` | App listens on :8080 |
| 4 | Open `http://localhost:8080` | Placeholder page visible |
| 5 | Stop app, restart | Starts again without manual DB fixes |

### Phase 0 — GitHub

```bash
git add -A
git commit -m "chore: phase 0 — Spring Boot bootstrap with PostgreSQL and Flyway"
git push origin main
```

---

# Phase 1 — Foundation (auth, schema, UI shell)

**Goal:** Login works, users table exists, shared Bootstrap layout, role-based access skeleton.  
**Duration:** ~2 days  
**GitHub commit message:** `feat: phase 1 — authentication, user schema and UI layout`

---

## Step 1.1 — Database: users table

- [ ] `V1__init_schema.sql` — create `users` table:
  - `id`, `username` (unique), `password_hash`, `full_name`, `email`
  - `role` (enum: ADMIN, AGENT, CHEF_AGENCE), `enabled`, `created_at`, `last_login_at`
- [ ] `V2__seed_dev_users.sql` — seed 3 users (one per role), BCrypt passwords documented in README for dev only:
  - `admin` / `admin123` → ADMIN
  - `agent` / `agent123` → AGENT
  - `chef` / `chef123` → CHEF_AGENCE

## Step 1.2 — User domain layer

- [ ] `UserRole` enum
- [ ] `User` JPA entity mapping to `users`
- [ ] `UserRepository` extends `JpaRepository`
- [ ] `CustomUserDetailsService` implements `UserDetailsService`

## Step 1.3 — Spring Security

- [ ] `SecurityConfig` — form login at `/login`, logout at `/logout`
- [ ] BCrypt password encoder bean
- [ ] URL rules:
  - `/login`, `/css/**`, `/js/**` — public
  - `/admin/**` — ADMIN only
  - `/users/**` — ADMIN only (prep for Phase 5)
  - everything else — authenticated
- [ ] Disabled user (`enabled = false`) cannot authenticate

## Step 1.4 — Thymeleaf UI shell

- [ ] `templates/layout/main.html` — Bootstrap 5 sidebar + navbar
- [ ] `templates/login.html` — login form, error message display
- [ ] `templates/dashboard/index.html` — stub: "Bienvenue, {username} ({role})"
- [ ] `static/css/app.css` — minimal custom styles
- [ ] Sidebar links (disabled/placeholder OK): Tableau de bord, Clients, Comptes, Opérations, Historique

## Step 1.5 — Web layer basics

- [ ] `DashboardController` — GET `/` or `/dashboard`
- [ ] `LoginController` — GET `/login`
- [ ] `GlobalExceptionHandler` — friendly error pages
- [ ] `templates/error/403.html`, `404.html`
- [ ] Flash message fragment for success/error alerts

## Step 1.6 — Smoke test class

- [ ] `BanqueAgenceApplicationTests` — context loads
- [ ] `SecurityIntegrationTest` — unauthenticated GET `/dashboard` redirects to login

---

### Phase 1 — Manual test checklist

| # | Action | Expected result |
|---|---|---|
| 1 | `mvnw clean test` | All tests pass |
| 2 | Open `/login` without auth | Login form shown |
| 3 | Login as `admin` / `admin123` | Redirect to dashboard, name + ADMIN shown |
| 4 | Logout | Back to login, `/dashboard` blocked |
| 5 | Login as `agent` | Dashboard accessible |
| 6 | Wrong password | Error message, no crash |
| 7 | Visit `/admin/test` as agent | 403 Forbidden page |
| 8 | Restart app | Seed users still work, Flyway does not re-run V1/V2 |

### Phase 1 — GitHub

```bash
git add -A
git commit -m "feat: phase 1 — authentication, user schema and UI layout"
git push origin main
git tag -a phase-1-foundation -m "Auth and UI shell complete"
```

---

# Phase 2 — Client management

**Goal:** Full client CRUD + multicriteria search per cahier §7.1.  
**Duration:** ~2 days  
**GitHub commit message:** `feat: phase 2 — client management with search and status`

---

## Step 2.1 — Database: clients table

- [ ] `V3__create_clients.sql`:
  - `id`, `client_number` (unique), `cin` (unique), `first_name`, `last_name`
  - `email`, `phone`, `address`, `professional_info`
  - `status` (ACTIVE, SUSPENDED, INACTIVE), `created_at`, `updated_at`

## Step 2.2 — Client domain layer

- [ ] `ClientStatus` enum
- [ ] `Client` entity + `ClientRepository`
- [ ] `ClientService` — create, update, findById, search, changeStatus
- [ ] `ClientNumberGenerator` — auto-generate unique client number (e.g. `CLI-2026-00001`)

## Step 2.3 — Validation

- [ ] `ClientForm` DTO with Bean Validation (`@NotBlank`, `@Email`, CIN format)
- [ ] Unique CIN check in service → clear error message

## Step 2.4 — Controllers & views

- [ ] `ClientController`:
  - GET `/clients` — paginated list
  - GET `/clients/new`, POST `/clients` — create
  - GET `/clients/{id}` — detail
  - GET `/clients/{id}/edit`, POST `/clients/{id}` — update
  - POST `/clients/{id}/status` — activate / suspend / deactivate
- [ ] Templates: `clients/list.html`, `form.html`, `detail.html`
- [ ] Search form on list: name, CIN, client number, phone (query params)

## Step 2.5 — Navigation & UX

- [ ] Enable "Clients" link in sidebar
- [ ] Flash messages on create/update/status change
- [ ] Empty state when no clients
- [ ] Pagination controls (page size 10)

## Step 2.6 — Audit log (minimal)

- [ ] `V4__create_audit_logs.sql` — audit_logs table
- [ ] `AuditLog` entity + `AuditService.log(action, entityType, entityId, details)`
- [ ] Log on client create, update, status change

## Step 2.7 — Tests

- [ ] `ClientServiceTest` — duplicate CIN rejected
- [ ] `ClientServiceTest` — search by phone returns match

---

### Phase 2 — Manual test checklist

| # | Action | Expected result |
|---|---|---|
| 1 | `mvnw clean test` | All tests pass |
| 2 | Login as agent → Clients | List page loads |
| 3 | Create client with valid data | Success flash, appears in list |
| 4 | Create client with same CIN | Validation error, not saved |
| 5 | Search by last name | Filtered results |
| 6 | Search by CIN | Exact match found |
| 7 | Edit client phone | Updated on detail page |
| 8 | Suspend client | Status badge changes to SUSPENDED |
| 9 | Reactivate client | Status back to ACTIVE |
| 10 | Phase 1 login/logout | Still works |

### Phase 2 — GitHub

```bash
git add -A
git commit -m "feat: phase 2 — client management with search and status"
git push origin main
git tag -a phase-2-clients -m "Client CRUD complete"
```

---

# Phase 3 — Account management

**Goal:** Open, view, block, unblock, close accounts per cahier §7.2.  
**Duration:** ~2 days  
**GitHub commit message:** `feat: phase 3 — bank account lifecycle management`

---

## Step 3.1 — Database: accounts table

- [ ] `V5__create_accounts.sql`:
  - `id`, `account_number` (unique), `type` (COURANT, EPARGNE, PROFESSIONNEL)
  - `status` (ACTIVE, BLOCKED, CLOSED), `balance` NUMERIC(19,4) default 0
  - `client_id` FK, `opened_at`, `closed_at`, `version` (optimistic lock)

## Step 3.2 — Account domain layer

- [ ] `AccountType`, `AccountStatus` enums
- [ ] `Account` entity with `BigDecimal balance`, `@Version`
- [ ] `AccountRepository`
- [ ] `AccountService` — open, findById, findByClient, block, unblock, close
- [ ] `AccountNumberGenerator` — e.g. `ACC-2026-00001`

## Step 3.3 — Business rules in service

- [ ] Cannot open account for INACTIVE client
- [ ] Close sets `closed_at`, status CLOSED, balance must be 0 (or warn)
- [ ] Blocked account stays blocked until explicit unblock
- [ ] `AccountService.assertOperable(account)` — throws if not ACTIVE (used in Phase 4)

## Step 3.4 — Controllers & views

- [ ] `AccountController`:
  - GET `/accounts` — list all (paginated)
  - GET `/clients/{clientId}/accounts/new`, POST — open account for client
  - GET `/accounts/{id}` — detail (balance, type, status, client link)
  - POST `/accounts/{id}/block`, `/unblock`, `/close`
- [ ] Client detail page — section "Comptes du client" with list + "Ouvrir un compte"
- [ ] Templates: `accounts/list.html`, `detail.html`, `open-form.html`

## Step 3.5 — Navigation

- [ ] Enable "Comptes" in sidebar
- [ ] Audit log on open, block, unblock, close

## Step 3.6 — Tests

- [ ] `AccountServiceTest` — open account for active client
- [ ] `AccountServiceTest` — cannot open for inactive client
- [ ] `AccountServiceTest` — close blocked account flow

---

### Phase 3 — Manual test checklist

| # | Action | Expected result |
|---|---|---|
| 1 | `mvnw clean test` | All tests pass |
| 2 | Open compte courant for client | Account created, balance 0.00 |
| 3 | Open second account (épargne) same client | Both listed on client detail |
| 4 | View account detail | Correct type, status ACTIVE, client name |
| 5 | Block account | Status BLOCKED, badge visible |
| 6 | Try unblock | Status ACTIVE again |
| 7 | Close account (balance 0) | Status CLOSED, closed_at set |
| 8 | Accounts list page | Pagination works |
| 9 | Phases 1–2 regression | Login + clients still work |

### Phase 3 — GitHub

```bash
git add -A
git commit -m "feat: phase 3 — bank account lifecycle management"
git push origin main
git tag -a phase-3-accounts -m "Account management complete"
```

---

# Phase 4 — Transactions (core banking)

**Goal:** Deposit, withdraw, transfer with balance integrity per cahier §7.3.  
**Duration:** ~3 days  
**GitHub commit message:** `feat: phase 4 — deposits, withdrawals, transfers and history`

---

## Step 4.1 — Database: transactions table

- [ ] `V6__create_transactions.sql`:
  - `id`, `type` (DEPOT, RETRAIT, VIREMENT), `amount` NUMERIC(19,4)
  - `source_account_id` (nullable), `destination_account_id` (nullable)
  - `executed_by` FK users, `executed_at`, `description`

## Step 4.2 — Transaction domain layer

- [ ] `TransactionType` enum
- [ ] `Transaction` entity + `TransactionRepository`
- [ ] `TransactionService` with `@Transactional`:
  - `deposit(accountId, amount, user)` — credit destination
  - `withdraw(accountId, amount, user)` — check balance ≥ amount, debit
  - `transfer(sourceId, destId, amount, user)` — both ACTIVE, debit + credit

## Step 4.3 — Business rules (critical)

- [ ] Reject withdraw if `balance < amount` → user-friendly message
- [ ] Reject operations on BLOCKED or CLOSED accounts
- [ ] Reject transfer to same account
- [ ] Reject zero or negative amounts
- [ ] Use `@Version` on Account — handle `OptimisticLockingFailureException`
- [ ] Each transaction records `executedBy` + `executedAt`

## Step 4.4 — Controllers & views

- [ ] `TransactionController`:
  - GET/POST `/operations/deposit`
  - GET/POST `/operations/withdraw`
  - GET/POST `/operations/transfer`
  - GET `/transactions` — history with filters (date from/to, type, account number, user)
- [ ] Templates: `operations/deposit.html`, `withdraw.html`, `transfer.html`, `transactions/list.html`
- [ ] Account selector dropdowns (active accounts only)

## Step 4.5 — Navigation & audit

- [ ] Enable "Opérations" and "Historique" in sidebar
- [ ] Audit log entry for every financial operation

## Step 4.6 — Tests (mandatory this phase)

- [ ] `TransactionServiceTest` — deposit increases balance
- [ ] `TransactionServiceTest` — withdraw insufficient funds throws
- [ ] `TransactionServiceTest` — withdraw on blocked account throws
- [ ] `TransactionServiceTest` — transfer updates both balances
- [ ] `TransactionServiceTest` — transfer blocked if source inactive

---

### Phase 4 — Manual test checklist

| # | Action | Expected result |
|---|---|---|
| 1 | `mvnw clean test` | All tests pass (including transaction tests) |
| 2 | Deposit 10 000 on courant | Balance = 10 000.00 |
| 3 | Withdraw 500 | Balance = 9 500.00 |
| 4 | Withdraw 99 999 | Error: solde insuffisant |
| 5 | Transfer 1 000 courant → épargne | Courant 8 500, épargne 1 000 |
| 6 | Deposit on blocked account | Operation rejected |
| 7 | History — filter by account | Shows only that account's ops |
| 8 | History — filter by type DEPOT | Correct filter |
| 9 | Each row shows agent name + timestamp | Traçabilité OK |
| 10 | Full flow from Phase 0–3 | No regression |

### Phase 4 — GitHub

```bash
git add -A
git commit -m "feat: phase 4 — deposits, withdrawals, transfers and history"
git push origin main
git tag -a phase-4-transactions -m "Core banking operations complete"
```

---

# Phase 5 — Internal user management

**Goal:** Admin manages staff accounts per cahier §7.4.  
**Duration:** ~2 days  
**GitHub commit message:** `feat: phase 5 — internal user administration`

---

## Step 5.1 — User administration service

- [ ] `UserAdminService` — create, update, enable, disable, setPassword
- [ ] Password always BCrypt-hashed on create/reset
- [ ] Prevent admin from disabling themselves

## Step 5.2 — Controllers & views (ADMIN only)

- [ ] `UserAdminController`:
  - GET `/users` — paginated list
  - GET/POST `/users/new` — create with role
  - GET/POST `/users/{id}/edit` — edit name, email, role
  - POST `/users/{id}/enable`, `/disable`
  - POST `/users/{id}/reset-password`
- [ ] Templates: `users/list.html`, `form.html`

## Step 5.3 — Security hardening

- [ ] Confirm `/users/**` requires ADMIN role
- [ ] CHEF_AGENCE and AGENT get 403 on `/users`

## Step 5.4 — Seed update (optional)

- [ ] No change to Flyway seeds — new users created via UI only after Phase 5

## Step 5.5 — Audit & tests

- [ ] Audit log on user create, disable, role change
- [ ] `UserAdminServiceTest` — disabled user cannot authenticate
- [ ] `UserAdminServiceTest` — create user with AGENT role

---

### Phase 5 — Manual test checklist

| # | Action | Expected result |
|---|---|---|
| 1 | `mvnw clean test` | All tests pass |
| 2 | Login as admin → Users | User list with 3 seed users |
| 3 | Create new agent user | Can login with new credentials |
| 4 | Disable that user | Login fails |
| 5 | Re-enable user | Login works again |
| 6 | Login as agent → `/users` | 403 Forbidden |
| 7 | Change user role AGENT → CHEF_AGENCE | Role updated in list |
| 8 | Phases 1–4 regression | Banking flows still work |

### Phase 5 — GitHub

```bash
git add -A
git commit -m "feat: phase 5 — internal user administration"
git push origin main
git tag -a phase-5-users -m "User admin complete"
```

---

# Phase 6 — Reporting & dashboard

**Goal:** Dashboard KPIs, recent ops, statements per cahier §7.5.  
**Duration:** ~2 days  
**GitHub commit message:** `feat: phase 6 — dashboard, statements and receipts`

---

## Step 6.1 — Dashboard service

- [ ] `DashboardService` queries:
  - Total clients (active count)
  - Total active accounts
  - Today's transaction count + total volume
  - Last 10 transactions (join account + user)

## Step 6.2 — Dashboard UI

- [ ] Replace dashboard stub with KPI cards + recent transactions table
- [ ] CHEF_AGENCE and ADMIN see full stats; AGENT sees simplified view (optional)

## Step 6.3 — Account statement

- [ ] GET `/accounts/{id}/statement?from=&to=` — transactions in period
- [ ] Running balance column (optional but impressive)
- [ ] Print-friendly CSS (`@media print`)

## Step 6.4 — Transaction receipt

- [ ] GET `/transactions/{id}/receipt` — printable receipt (HTML)
- [ ] Shows: type, amount, accounts, date, agent, reference

## Step 6.5 — PDF export (P2 — optional)

- [ ] Add OpenPDF dependency
- [ ] GET `/accounts/{id}/statement.pdf` — same data as HTML statement
- [ ] Skip if time-constrained; HTML print is enough for P0

## Step 6.6 — Tests

- [ ] `DashboardServiceTest` — counts match seeded data

---

### Phase 6 — Manual test checklist

| # | Action | Expected result |
|---|---|---|
| 1 | `mvnw clean test` | All tests pass |
| 2 | Login as chef → Dashboard | KPI cards show real numbers |
| 3 | Recent transactions table | Last ops visible with amounts |
| 4 | Open statement for account (last 30 days) | Correct transactions listed |
| 5 | Print statement (Ctrl+P) | Layout readable, no sidebar clutter |
| 6 | Open receipt for a deposit | All fields correct |
| 7 | PDF export (if implemented) | File downloads and opens |
| 8 | Full banking flow regression | End-to-end still works |

### Phase 6 — GitHub

```bash
git add -A
git commit -m "feat: phase 6 — dashboard, statements and receipts"
git push origin main
git tag -a phase-6-reporting -m "Dashboard and reporting complete"
```

---

# Phase 7 — Polish, tests, documentation, ship

**Goal:** Demo-ready, academically defensible, GitHub repo presentable.  
**Duration:** ~2 days  
**GitHub commit message:** `chore: phase 7 — demo data, tests, docs and soutenance prep`

---

## Step 7.1 — Demo dataset

- [ ] `V7__demo_data.sql` (dev profile only) OR Java `ApplicationRunner` seed:
  - 5 clients, 8 accounts, 20 transactions
- [ ] Document demo credentials and sample data in README

## Step 7.2 — Complete test suite

- [ ] Reach ~10–15 tests total (see `TECHNICAL.md` §12)
- [ ] `mvnw clean test` — 100% pass rate
- [ ] Optional: one `@SpringBootTest` end-to-end login → deposit flow

## Step 7.3 — UI polish

- [ ] Consistent French labels across all pages
- [ ] Format amounts: `10 000,00 MAD` or `10 000.00 DH`
- [ ] Status badges (ACTIVE = green, BLOCKED = red, etc.)
- [ ] Mobile-responsive sidebar (Bootstrap collapse)

## Step 7.4 — Documentation

- [ ] `documentation/manuel-utilisateur.md` — screenshots + steps per role
- [ ] `documentation/demo-script.md` — 5–7 min soutenance walkthrough
- [ ] Update `README.md` — features list, tech stack, test commands, demo users
- [ ] Update `TECHNICAL.md` §7 — all flows marked Done

## Step 7.5 — UML & data model (rapport)

- [ ] `documentation/uml/` — use case, class, sequence diagrams
- [ ] `documentation/modele-donnees/` — MCD, MLD, dictionary
- [ ] Align diagrams with actual code (not aspirational)

## Step 7.6 — Production sanity

- [ ] `application-prod.yml` loads from env vars
- [ ] `mvnw clean package` produces runnable JAR
- [ ] `java -jar target/banque-agence-*.jar --spring.profiles.active=prod` documented

## Step 7.7 — Final GitHub hygiene

- [ ] README badge or clear "PFA Banque Agence" title
- [ ] Clean commit history on `main` (one commit per phase minimum)
- [ ] Tags: `phase-0` through `phase-7` (optional)
- [ ] Remove any dev secrets from tracked files

---

### Phase 7 — Manual test checklist (full demo rehearsal)

| # | Step | Role |
|---|---|---|
| 1 | Login | agent |
| 2 | Create client, search by CIN | agent |
| 3 | Open courant + épargne | agent |
| 4 | Deposit 10 000 | agent |
| 5 | Withdraw 500 | agent |
| 6 | Transfer 1 000 | agent |
| 7 | View filtered history | agent |
| 8 | View receipt | agent |
| 9 | Login, view dashboard KPIs | chef |
| 10 | Login, manage users | admin |
| 11 | `mvnw clean test` | — |
| 12 | `mvnw clean package` | — |

### Phase 7 — GitHub

```bash
git add -A
git commit -m "chore: phase 7 — demo data, tests, docs and soutenance prep"
git push origin main
git tag -a v1.0.0-pfa -m "PFA demo-ready release"
```

---

## Parallel track — Analysis & conception (rapport)

Run alongside coding; sync after Phases 3 and 4:

| Deliverable | Folder | Best time to finalize |
|---|---|---|
| Cas d'utilisation | `documentation/uml/` | After Phase 2 |
| Diagramme de classes | `documentation/uml/` | After Phase 4 |
| Diagrammes de séquence | `documentation/uml/` | After Phase 4 |
| MCD / MLD | `documentation/modele-donnees/` | After Phase 3 |
| Dictionnaire de données | `documentation/modele-donnees/` | After MLD |

---

## GitHub commit map (summary)

| Phase | Suggested commit message | Tag |
|---|---|---|
| 0 | `chore: phase 0 — Spring Boot bootstrap with PostgreSQL and Flyway` | `phase-0-bootstrap` |
| 1 | `feat: phase 1 — authentication, user schema and UI layout` | `phase-1-foundation` |
| 2 | `feat: phase 2 — client management with search and status` | `phase-2-clients` |
| 3 | `feat: phase 3 — bank account lifecycle management` | `phase-3-accounts` |
| 4 | `feat: phase 4 — deposits, withdrawals, transfers and history` | `phase-4-transactions` |
| 5 | `feat: phase 5 — internal user administration` | `phase-5-users` |
| 6 | `feat: phase 6 — dashboard, statements and receipts` | `phase-6-reporting` |
| 7 | `chore: phase 7 — demo data, tests, docs and soutenance prep` | `v1.0.0-pfa` |

---

## Soutenance demo script (7 minutes)

1. **Login** as agent
2. **Créer client** + recherche CIN
3. **Ouvrir** compte courant + épargne
4. **Dépôt** 10 000 MAD
5. **Retrait** 500 MAD (show solde check)
6. **Virement** 1 000 MAD
7. **Historique** filtré + reçu
8. **Login** chef → tableau de bord
9. **Login** admin → gestion utilisateurs
10. Mentionner **Java/Spring Boot**, **BCrypt**, **journal d'audit**, **Flyway**

---

## Next action

**Start Phase 0, Step 0.1** — initialize Spring Boot project on `main`.

When ready, say *"start phase 0"* and we implement Step 0.1 through 0.6, then run the Phase 0 test checklist and push to GitHub.

---

*Last updated: 2026-06-14*
