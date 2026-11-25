# Legacy Banking App — Modernization Plan

## Approved Decisions

| Decision | Choice |
|---|---|
| Database | Retain existing SQLite (`legacybank.db`) |
| UI library | IBM Carbon Design System (`@carbon/react`) |
| Authentication | Stateless JWT (HS256, Spring Security OAuth2 Resource Server) |
| Docker | Yes — `docker-compose.yml` for one-command demo |
| GitHub repository | `git@github.com:DanishKhan25/BobBanking.git` (origin) |
| New code location | `modern/` directory (legacy files untouched) |

---

## Top-Level Overview

### Goal
Modernize the monolithic Java 8 Swing desktop banking application into a 3-tier web application while keeping every existing business rule intact and leaving all legacy files read-only.

### Scope
| In scope | Out of scope |
|---|---|
| New React 19 + TypeScript frontend under `modern/frontend/` | Modifying any file under `src/` or `database/` |
| New Java 21 + Spring Boot 3 REST API under `modern/backend/` | Database migration (SQLite stays) |
| Reuse of existing `legacybank.db` (read path via Spring Data JPA) | New banking features beyond what legacy app supports |
| BCrypt password hashing migration for EMPLOYEES table | Changing the original schema |
| JWT-secured API | Real-time/WebSocket features |
| Carbon Design System dashboard with KPI summary | Native mobile apps |
| Docker Compose for local demo | Cloud deployment / CI/CD pipelines |
| GitHub Epics + Stories with acceptance criteria in `DanishKhan25/BobBanking` | |

### Architecture

```
Browser (React 19 + TypeScript + Carbon)
        │  HTTPS / JSON
        ▼
Spring Boot 3.3 REST API  (Java 21)
  ├─ AuthController  POST /api/auth/login  → JWT
  ├─ CustomerController
  ├─ AccountController
  └─ TransactionController
        │  Spring Data JPA / Hibernate + SQLite dialect
        ▼
legacybank.db  (existing SQLite file, read-write, volume-mounted)
```

### Existing Business Rules Preserved
- Accounts must be ACTIVE for deposits, withdrawals and transfers
- Withdrawal / transfer amount ≤ available balance (no overdraft)
- Amount: positive, max 2 decimal places, max 10,000,000.00
- Transfer source ≠ destination account
- Reference numbers: `DEP` / `WDR` / `TRF` prefix + 10-digit sequential ID (REFERENCE_SEQUENCE table)
- Both legs of a transfer share one `REFERENCE_NUMBER` and are committed atomically

---

## Component Epics and Stories

### Epic 1 — Database

**Stories:**

#### DB-1: BCrypt password migration script
- **Intent:** The EMPLOYEES table stores plain-text passwords. Before the new API can authenticate securely, the existing `admin123` value must be replaced with its BCrypt hash. A standalone SQL migration script does this without touching the schema.
- **Expected Outcomes:** `EMPLOYEES.PASSWORD` for the `admin` row contains a valid BCrypt hash. The legacy Swing app continues to work (it compares plain-text; the migration is opt-in for the new stack only — the script is applied only when the new backend starts).
- **Acceptance Criteria:**
  - `modern/database/migrate-bcrypt.sql` updates `admin`'s password to its BCrypt hash.
  - Running the script twice is idempotent (no duplicate rows, no error).
  - Legacy `legacybank.db` on disk is NOT modified by the script at build time; it is applied at first backend start only.
  - `DatabaseInitializer` (legacy) is not changed.
- **Todo List:**
  1. Create `modern/database/migrate-bcrypt.sql` with an `UPDATE EMPLOYEES SET PASSWORD = '<bcrypt-hash>' WHERE USERNAME = 'admin' AND length(PASSWORD) < 60`.
  2. Document the script in `modern/database/README.md`.
- **Relevant Context:** `database/sqlite-schema.sql`, `database/seed-data.sql` (admin/admin123), `src/main/java/com/legacybank/LoginFrame.java`
- **Status:** [ ] pending

#### DB-2: Spring Data JPA entity mapping
- **Intent:** Define JPA entity classes that map 1:1 to the existing SQLite tables so the new backend can read and write data without changing the schema.
- **Expected Outcomes:** Five JPA entities (`Employee`, `Customer`, `Account`, `Transaction`, `ReferenceSequence`) with correct column mappings, relationships and constraints compile and pass unit tests.
- **Acceptance Criteria:**
  - All five entities present under `modern/backend/src/main/java/com/modernbank/domain/`.
  - `Account` has `@ManyToOne` to `Customer`; `Transaction` has `@ManyToOne` to `Account` (via `ACCOUNT_NUMBER`).
  - `Account.accountType` and `Account.status` mapped to Java enums.
  - `Transaction.transactionType` mapped to Java enum.
  - Hibernate `validate` DDL mode confirms mappings match the existing schema (no schema generation).
  - Integration test verifies entity load from seeded SQLite data.
- **Todo List:**
  1. Add `modern/backend/pom.xml` with Spring Boot 3.3, Spring Data JPA, `sqlite-jdbc`, `hibernate-community-dialects`, Spring Security, `jjwt`, JUnit 5 dependencies.
  2. Create entity classes in `modern/backend/src/main/java/com/modernbank/domain/`.
  3. Configure `application.properties` (`spring.jpa.hibernate.ddl-auto=validate`, SQLite dialect, datasource pointing to `legacybank.db`).
  4. Write integration test loading one `Customer` and one `Account` from seeded data.
- **Relevant Context:** `database/sqlite-schema.sql`, `src/main/java/com/legacybank/DBConnection.java`, `src/main/resources/db.properties`
- **Status:** [ ] pending

---

### Epic 2 — Backend

**Stories:**

#### BE-1: Project scaffold and Spring Boot configuration
- **Intent:** Create the Maven project structure, shared configuration, exception handling framework and Spring Security JWT filter so all subsequent backend stories have a solid foundation to build on.
- **Expected Outcomes:** `mvn clean package` succeeds; the application starts and responds to `GET /api/health`; JWT filter rejects unauthenticated requests on protected routes.
- **Acceptance Criteria:**
  - `modern/backend/` contains a standard Spring Boot Maven layout.
  - `GET /api/health` returns `200 OK` without a token.
  - All other `/api/**` routes return `401` without a valid JWT.
  - `application.properties` externalises the JWT secret and SQLite file path.
  - CORS configured to allow `http://localhost:5173` (Vite dev server).
  - `GlobalExceptionHandler` returns JSON `{ "error": "..." }` for validation and business rule violations.
  - Unit test for the JWT utility (sign + verify round-trip).
- **Todo List:**
  1. Scaffold `modern/backend/` Maven project.
  2. Add Spring Security configuration with JWT filter (`OncePerRequestFilter`).
  3. Add `GlobalExceptionHandler` (`@RestControllerAdvice`).
  4. Add `GET /api/health` endpoint.
  5. Write unit test for JWT sign/verify.
- **Relevant Context:** DB-2 (JPA entities must exist first), `src/main/resources/db.properties`
- **Status:** [ ] pending

#### BE-2: Authentication API
- **Intent:** Expose a login endpoint that validates employee credentials against the EMPLOYEES table and returns a signed JWT, replacing the legacy Swing login dialog.
- **Expected Outcomes:** `POST /api/auth/login` with valid credentials returns a JWT; invalid credentials return `401`.
- **Acceptance Criteria:**
  - `POST /api/auth/login` with `{ "username": "admin", "password": "admin123" }` returns `200` with `{ "token": "<jwt>", "employeeName": "Branch Administrator" }`.
  - Inactive employees (`STATUS != 'ACTIVE'`) are rejected with `401`.
  - Wrong credentials return `401` (no information leakage).
  - BCrypt comparison used (requires DB-1 migration to have run).
  - Integration test covers success, wrong password, inactive employee.
- **Todo List:**
  1. Create `AuthController` with `POST /api/auth/login`.
  2. Create `AuthService` loading employee by username, validating BCrypt password, checking `STATUS = ACTIVE`.
  3. Issue JWT with `employeeId`, `username`, `employeeName` claims and configurable expiry.
  4. Write integration tests (success + failure cases).
- **Relevant Context:** DB-1, DB-2, BE-1, `src/main/java/com/legacybank/LoginFrame.java`
- **Status:** [ ] pending

#### BE-3: Customer API
- **Intent:** Expose customer lookup endpoints that replicate the two search modes in `CustomerSearchFrame` (search by customer ID and by account number).
- **Expected Outcomes:** `GET /api/customers/{id}` and `GET /api/customers/by-account/{accountNumber}` return customer data plus their accounts list.
- **Acceptance Criteria:**
  - `GET /api/customers/{id}` returns customer fields + all their accounts (account number, type, status, balance).
  - `GET /api/customers/by-account/{accountNumber}` returns same shape by looking up the owning customer.
  - `404` returned when customer or account not found.
  - Requires valid JWT (`Authorization: Bearer <token>`).
  - Integration tests cover both search modes and not-found cases.
- **Todo List:**
  1. Create `CustomerController` and `CustomerService`.
  2. Create `CustomerRepository` (Spring Data JPA).
  3. Define response DTOs (`CustomerResponse`, `AccountSummary`).
  4. Write integration tests.
- **Relevant Context:** DB-2, BE-1, `src/main/java/com/legacybank/CustomerSearchFrame.java`
- **Status:** [ ] pending

#### BE-4: Account details API
- **Intent:** Expose a single-account lookup endpoint replicating `AccountDetailsFrame`.
- **Expected Outcomes:** `GET /api/accounts/{accountNumber}` returns full account details including customer name, email and phone.
- **Acceptance Criteria:**
  - Response includes: account number, type, status, balance (formatted to 2 d.p.), opened date, customer ID, customer name, email, phone.
  - `404` for unknown account numbers.
  - Requires valid JWT.
  - Integration test covers success and not-found.
- **Todo List:**
  1. Create `AccountController` and `AccountService`.
  2. Create `AccountRepository`.
  3. Define `AccountDetailsResponse` DTO.
  4. Write integration tests.
- **Relevant Context:** DB-2, BE-1, `src/main/java/com/legacybank/AccountDetailsFrame.java`
- **Status:** [ ] pending

#### BE-5: Transaction operations API — Deposit and Withdrawal
- **Intent:** Expose `POST /api/transactions/deposit` and `POST /api/transactions/withdrawal` endpoints replicating the deposit and withdrawal business logic from `DepositFrame` and `WithdrawalFrame`.
- **Expected Outcomes:** Both endpoints update `ACCOUNTS.BALANCE` and insert a `TRANSACTIONS` row atomically, returning the new balance and reference number.
- **Acceptance Criteria:**
  - Deposit: `POST /api/transactions/deposit` with `{ "accountNumber": "...", "amount": ..., "description": "..." }` returns `{ "referenceNumber": "DEP...", "newBalance": ... }`.
  - Withdrawal: same shape with `WDR` prefix.
  - Business rules enforced: account must exist and be ACTIVE; amount > 0, ≤ 10,000,000, ≤ 2 d.p.; withdrawal amount ≤ balance.
  - Operations are `@Transactional` with `BEGIN IMMEDIATE` equivalent isolation.
  - `400` with descriptive message on rule violation.
  - Requires valid JWT.
  - Integration tests: success, frozen account, insufficient funds, invalid amount.
- **Todo List:**
  1. Create `TransactionController` with deposit and withdrawal endpoints.
  2. Create `TransactionService` with `deposit()` and `withdraw()` methods mirroring `BankUtil` logic.
  3. Create `ReferenceSequenceRepository` for reference number generation.
  4. Create `TransactionRepository`.
  5. Apply `@Transactional` with correct isolation.
  6. Write integration tests for all acceptance criteria cases.
- **Relevant Context:** DB-2, BE-1, `src/main/java/com/legacybank/DepositFrame.java`, `src/main/java/com/legacybank/WithdrawalFrame.java`, `src/main/java/com/legacybank/BankUtil.java`
- **Status:** [ ] pending

#### BE-6: Transaction operations API — Fund Transfer
- **Intent:** Expose `POST /api/transactions/transfer` replicating the atomic two-leg fund transfer from `TransferFrame`.
- **Expected Outcomes:** A single request debits the source account and credits the destination account in one database transaction, both legs sharing the same `TRF` reference number.
- **Acceptance Criteria:**
  - `POST /api/transactions/transfer` with `{ "fromAccount": "...", "toAccount": "...", "amount": ..., "description": "..." }` returns `{ "referenceNumber": "TRF...", "fromNewBalance": ..., "toNewBalance": ... }`.
  - Source ≠ destination enforced (`400`).
  - Both accounts must be ACTIVE.
  - Source balance ≥ amount.
  - Both `TRANSFER_DEBIT` and `TRANSFER_CREDIT` rows share the same `REFERENCE_NUMBER`.
  - Single `@Transactional` boundary; full rollback on any error.
  - Requires valid JWT.
  - Integration tests: success, same-account, insufficient funds, frozen destination.
- **Todo List:**
  1. Add `transfer()` method to `TransactionService`.
  2. Add `POST /api/transactions/transfer` to `TransactionController`.
  3. Write integration tests for all acceptance criteria cases.
- **Relevant Context:** BE-5, `src/main/java/com/legacybank/TransferFrame.java`
- **Status:** [ ] pending

#### BE-7: Transaction history API
- **Intent:** Expose `GET /api/transactions/{accountNumber}` replicating `TransactionHistoryFrame`, with optional type filtering.
- **Expected Outcomes:** Returns a list of transactions for an account, ordered newest first, with optional `?type=` filter.
- **Acceptance Criteria:**
  - `GET /api/transactions/{accountNumber}` returns all transactions newest first.
  - `GET /api/transactions/{accountNumber}?type=DEPOSIT` filters by transaction type.
  - `404` for unknown account number.
  - Each item includes: transaction ID, type, amount, date, reference number, description.
  - Requires valid JWT.
  - Integration test covers unfiltered list, type filter, not-found.
- **Todo List:**
  1. Add `GET /api/transactions/{accountNumber}` to `TransactionController`.
  2. Add `findByAccountNumberOrderByTransactionDateDesc` query to `TransactionRepository`.
  3. Define `TransactionResponse` DTO.
  4. Write integration tests.
- **Relevant Context:** BE-5, `src/main/java/com/legacybank/TransactionHistoryFrame.java`
- **Status:** [ ] pending

---

### Epic 3 — Frontend

**Stories:**

#### FE-1: React project scaffold and routing
- **Intent:** Create the Vite + React 19 + TypeScript project with Carbon Design System, routing, Axios HTTP client, JWT token management and a shared API service layer so all subsequent frontend stories have a consistent foundation.
- **Expected Outcomes:** `npm run dev` starts the app at `http://localhost:5173`; navigating to `/` shows a placeholder; Carbon theme loads correctly.
- **Acceptance Criteria:**
  - `modern/frontend/` created with Vite React-TS template.
  - `@carbon/react`, `react-router-dom`, `axios` installed.
  - Axios instance with base URL from env var, JWT `Authorization` header interceptor.
  - `AuthContext` stores JWT and employee name; redirects unauthenticated users to `/login`.
  - React Router with routes: `/login`, `/dashboard`, `/customers`, `/accounts/:accountNumber`, `/transactions` (placeholder pages).
  - Carbon `Theme` provider wraps the app.
  - Vitest configured; at least one smoke test passes.
- **Todo List:**
  1. Scaffold `modern/frontend/` with `npm create vite@latest`.
  2. Install Carbon, React Router, Axios.
  3. Create `src/api/client.ts` (Axios instance + interceptor).
  4. Create `src/context/AuthContext.tsx`.
  5. Set up `src/App.tsx` with router and protected route wrapper.
  6. Configure Vitest and write smoke test.
- **Relevant Context:** BE-1, `legacy_banking_modernization_workshop.md` (Vite, npm run dev)
- **Status:** [ ] pending

#### FE-2: Login page
- **Intent:** Build a Carbon-styled login page that authenticates against `POST /api/auth/login` and stores the JWT in `AuthContext`.
- **Expected Outcomes:** Entering `admin` / `admin123` navigates to the dashboard; wrong credentials show an inline error message.
- **Acceptance Criteria:**
  - Carbon `Form`, `TextInput`, `PasswordInput`, `Button` used.
  - On success: JWT stored in `AuthContext`, redirect to `/dashboard`.
  - On `401`: inline error message displayed (no alert/console).
  - Logout clears the JWT and redirects to `/login`.
  - Component test: renders form, submits, handles success and error responses.
- **Todo List:**
  1. Create `src/pages/LoginPage.tsx`.
  2. Call `POST /api/auth/login` via Axios.
  3. Store token in `AuthContext`, redirect on success.
  4. Display Carbon `InlineNotification` on error.
  5. Write Vitest component tests.
- **Relevant Context:** FE-1, BE-2
- **Status:** [ ] pending

#### FE-3: Dashboard page with KPI summary
- **Intent:** Build the main dashboard page with Carbon KPI tiles (total accounts, total balance, accounts by status) and a Quick Actions panel replacing the legacy 6-button grid.
- **Expected Outcomes:** After login, the user sees a clean, modern dashboard with live summary tiles and navigation shortcuts.
- **Acceptance Criteria:**
  - Carbon `Tile`, `ClickableTile`, `Grid`, `Column`, `Tag` components used.
  - KPI tiles: Total Customers, Total Accounts, Total Balance, Frozen Accounts count.
  - Quick Actions: Customer Search, Account Details, Deposit, Withdrawal, Transfer, Transaction History (each navigates to the correct route).
  - Logged-in employee name displayed in Carbon `Header`.
  - Data loaded from backend API on page mount.
  - Loading skeleton shown while data is fetching.
  - Component test: renders tiles with mocked API data.
- **Todo List:**
  1. Add `GET /api/dashboard/summary` endpoint to backend (AccountService aggregation query).
  2. Create `src/pages/DashboardPage.tsx`.
  3. Implement KPI tiles and Quick Actions grid.
  4. Add Carbon `Header` with employee name and logout button.
  5. Write Vitest component test with mocked summary data.
- **Relevant Context:** FE-1, FE-2, BE-4
- **Status:** [ ] pending

#### FE-4: Customer search page
- **Intent:** Build the customer search page replicating `CustomerSearchFrame` — search by customer ID or account number, display customer info and their accounts table.
- **Expected Outcomes:** Submitting a valid customer ID or account number shows the customer record and a Carbon data table of their accounts.
- **Acceptance Criteria:**
  - Carbon `Search` or `TextInput` + `RadioButtonGroup` for search mode selection.
  - Carbon `DataTable` displaying account number, type, status (Carbon `Tag`), balance.
  - Customer info panel (name, email, phone, address) shown above the table.
  - `404` case shows Carbon `InlineNotification`.
  - Component test: renders results with mocked API data.
- **Todo List:**
  1. Create `src/pages/CustomerSearchPage.tsx`.
  2. Call `GET /api/customers/{id}` or `GET /api/customers/by-account/{accountNumber}`.
  3. Render customer panel and accounts DataTable.
  4. Handle not-found and loading states.
  5. Write Vitest component test.
- **Relevant Context:** FE-1, BE-3
- **Status:** [ ] pending

#### FE-5: Account details page
- **Intent:** Build the account details page replicating `AccountDetailsFrame` — look up a single account by number and display all its fields.
- **Expected Outcomes:** Navigating to `/accounts/:accountNumber` shows a Carbon detail panel with all account and customer fields.
- **Acceptance Criteria:**
  - Carbon `StructuredList` or labelled `Tile` for field display.
  - Shows: account number, type, status (Carbon `Tag`), balance, opened date, customer ID, customer name, email, phone.
  - Account number can be pre-filled from URL param or entered manually.
  - `404` shows Carbon `InlineNotification`.
  - Component test with mocked API data.
- **Todo List:**
  1. Create `src/pages/AccountDetailsPage.tsx`.
  2. Call `GET /api/accounts/{accountNumber}`.
  3. Render structured detail view.
  4. Write Vitest component test.
- **Relevant Context:** FE-1, BE-4
- **Status:** [ ] pending

#### FE-6: Deposit and Withdrawal forms
- **Intent:** Build Carbon-styled forms for deposit and withdrawal operations, replicating `DepositFrame` and `WithdrawalFrame`.
- **Expected Outcomes:** Submitting a valid deposit or withdrawal shows the new balance and reference number in a success notification.
- **Acceptance Criteria:**
  - Carbon `Form`, `TextInput`, `NumberInput`, `Button` used.
  - Both operations on a single page at `/transactions` with a Carbon `Tabs` switcher.
  - On success: Carbon `InlineNotification` with reference number and new balance.
  - On business rule violation (`400`): error message shown inline.
  - Client-side validation mirrors backend rules (amount > 0, ≤ 10,000,000).
  - Component tests for submit success and error display.
- **Todo List:**
  1. Create `src/pages/TransactionsPage.tsx` with Carbon Tabs.
  2. Implement Deposit tab calling `POST /api/transactions/deposit`.
  3. Implement Withdrawal tab calling `POST /api/transactions/withdrawal`.
  4. Display success/error notifications.
  5. Write Vitest component tests.
- **Relevant Context:** FE-1, BE-5
- **Status:** [ ] pending

#### FE-7: Fund transfer form
- **Intent:** Add a Transfer tab to the transactions page, replicating `TransferFrame`.
- **Expected Outcomes:** A valid transfer shows both new balances and the shared `TRF` reference number.
- **Acceptance Criteria:**
  - Transfer tab on `/transactions` (extends FE-6 Tabs).
  - Source account, destination account, amount fields.
  - On success: both new balances and reference number displayed.
  - Business rule errors shown inline.
  - Component test for success and same-account error.
- **Todo List:**
  1. Add Transfer tab to `TransactionsPage.tsx`.
  2. Call `POST /api/transactions/transfer`.
  3. Display response in success notification.
  4. Write Vitest component tests.
- **Relevant Context:** FE-6, BE-6
- **Status:** [ ] pending

#### FE-8: Transaction history page
- **Intent:** Build the transaction history view replicating `TransactionHistoryFrame` — query by account number with optional type filter.
- **Expected Outcomes:** A Carbon DataTable shows all transactions for an account, sortable by date, with a type filter dropdown.
- **Acceptance Criteria:**
  - Account number input + Carbon `Dropdown` for type filter.
  - Carbon `DataTable` with columns: ID, type (Carbon `Tag`), amount, date, reference, description.
  - Newest first ordering.
  - Loading skeleton and empty-state message.
  - `404` / unknown account handled with inline notification.
  - Component test with mocked API data.
- **Todo List:**
  1. Create `src/pages/TransactionHistoryPage.tsx`.
  2. Call `GET /api/transactions/{accountNumber}?type=...`.
  3. Render DataTable with Tag-coloured type column.
  4. Write Vitest component test.
- **Relevant Context:** FE-1, BE-7
- **Status:** [ ] pending

---

### Epic 4 — DevOps

**Stories:**

#### DO-1: Docker Compose setup
- **Intent:** Create a `docker-compose.yml` that builds and starts the backend and frontend containers with the SQLite file volume-mounted, so the full stack runs with `docker compose up --build`.
- **Expected Outcomes:** `docker compose up --build` from `modern/` starts the app; the frontend is accessible at `http://localhost:5173`; login with `admin` / `admin123` works end-to-end.
- **Acceptance Criteria:**
  - `modern/docker-compose.yml` defines `backend` and `frontend` services.
  - `backend` service: builds from `modern/backend/Dockerfile`, mounts `../legacybank.db` as a volume (read-write), exposes port 8080.
  - `frontend` service: builds from `modern/frontend/Dockerfile`, exposes port 5173 (or 80 via nginx).
  - Environment variables for JWT secret and SQLite path are set via `.env` file (`.env.example` committed; `.env` git-ignored).
  - `modern/README.md` documents `docker compose up --build` and the separate `mvn` / `npm` run commands.
  - Smoke test: `curl http://localhost:8080/api/health` returns `200` after compose up.
- **Todo List:**
  1. Create `modern/backend/Dockerfile` (multi-stage: Maven build → JRE 21 runtime).
  2. Create `modern/frontend/Dockerfile` (Node build → nginx static serve).
  3. Create `modern/docker-compose.yml` with both services and SQLite volume.
  4. Create `modern/.env.example`.
  5. Create `modern/README.md` with run instructions.
- **Relevant Context:** BE-1, FE-1, `legacy_banking_modernization_workshop.md` §5
- **Status:** [ ] pending

---

## Implementation Sequence

The stories are ordered so each one builds on completed work:

```
DB-1  ──► DB-2  ──► BE-1  ──► BE-2  ──► BE-3
                                │          │
                               BE-4       BE-5  ──► BE-6
                                │          │
                               FE-1  ◄───FE-1
                                │
                               FE-2  ──► FE-3
                                          │
                   FE-4 ◄── FE-5 ◄── FE-6 ──► FE-7 ──► FE-8
                                                            │
                                                          DO-1
```

**Recommended linear sequence:**

| Order | Story | Why first |
|---|---|---|
| 1 | DB-1 | BCrypt migration script — no code dependencies |
| 2 | DB-2 | JPA entities — foundation for all backend stories |
| 3 | BE-1 | Spring Boot scaffold + JWT filter — required by all API stories |
| 4 | BE-2 | Auth API — required by frontend login |
| 5 | BE-3 | Customer API |
| 6 | BE-4 | Account details API |
| 7 | BE-5 | Deposit + Withdrawal API |
| 8 | BE-6 | Transfer API (extends BE-5 service) |
| 9 | BE-7 | Transaction history API |
| 10 | FE-1 | React scaffold + routing — foundation for all UI stories |
| 11 | FE-2 | Login page |
| 12 | FE-3 | Dashboard (requires a summary endpoint added to BE-4) |
| 13 | FE-4 | Customer search |
| 14 | FE-5 | Account details |
| 15 | FE-6 | Deposit + Withdrawal forms |
| 16 | FE-7 | Transfer form (extends FE-6 Tabs) |
| 17 | FE-8 | Transaction history |
| 18 | DO-1 | Docker Compose (all code complete) |

---

## GitHub Issues to Create

### Epics (one per component)
| Epic title | Label |
|---|---|
| Database | `epic` |
| Backend | `epic` |
| Frontend | `epic` |
| DevOps | `epic` |

### Stories (linked to their component Epic)
| Story ID | Title | Epic |
|---|---|---|
| DB-1 | BCrypt password migration script | Database |
| DB-2 | Spring Data JPA entity mapping | Database |
| BE-1 | Spring Boot scaffold and configuration | Backend |
| BE-2 | Authentication API | Backend |
| BE-3 | Customer API | Backend |
| BE-4 | Account details API | Backend |
| BE-5 | Transaction operations API — Deposit and Withdrawal | Backend |
| BE-6 | Transaction operations API — Fund Transfer | Backend |
| BE-7 | Transaction history API | Backend |
| FE-1 | React project scaffold and routing | Frontend |
| FE-2 | Login page | Frontend |
| FE-3 | Dashboard page with KPI summary | Frontend |
| FE-4 | Customer search page | Frontend |
| FE-5 | Account details page | Frontend |
| FE-6 | Deposit and Withdrawal forms | Frontend |
| FE-7 | Fund transfer form | Frontend |
| FE-8 | Transaction history page | Frontend |
| DO-1 | Docker Compose setup | DevOps |

> All Issues will be created in `DanishKhan25/BobBanking` (the configured `origin`). No Issues will be created in the facilitator's repository.

---

## Guardrails

- All new code lives under `modern/` — zero modifications to `src/`, `database/`, `pom.xml` or `legacybank.db`.
- `legacybank.db` is never committed to the repository.
- Build output (`target/`, `node_modules/`, `dist/`) is git-ignored.
- BCrypt migration (`modern/database/migrate-bcrypt.sql`) is applied by the backend at startup, not at plan time.
- Legacy Swing app remains runnable after modernization: `mvn clean package && java -jar target/legacy-banking-app.jar` still works.
