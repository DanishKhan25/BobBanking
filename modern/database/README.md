# modern/database

This directory contains database scripts for the modernized banking application.

## Files

| File | Purpose |
|---|---|
| `migrate-bcrypt.sql` | One-time migration: replaces plain-text `admin123` password in `EMPLOYEES` with its BCrypt hash |

## migrate-bcrypt.sql

**What it does:** At Spring Boot startup, `DatabaseMigrationRunner` checks whether `EMPLOYEES.PASSWORD` for the `admin` user is already a BCrypt hash (length = 60, starts with `$2a$`). If not, it generates the hash in Java using `BCryptPasswordEncoder` and issues the `UPDATE` programmatically — **no hash is ever committed to source control**.

**When it runs:** Automatically on first `mvn spring-boot:run` or `docker compose up`. It is idempotent — subsequent starts detect the hash is already in place and skip the update.

**Legacy app:** The legacy Swing application (`src/`) compares passwords with plain-text SQL (`WHERE PASSWORD = ?`). It will stop working for the `admin` user after the migration runs. This is expected — the modern Spring Boot backend replaces the Swing login.

**Verify after first backend start:**
```bash
sqlite3 legacybank.db "SELECT USERNAME, length(PASSWORD) FROM EMPLOYEES;"
# Expected output:  admin|60
```

## Original database

The original `legacybank.db` and all files under `database/` are **never modified** by the modernization build. The migration script is applied at runtime by the Spring Boot backend only.
