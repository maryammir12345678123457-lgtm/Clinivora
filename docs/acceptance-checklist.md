# Clinivora Acceptance Checklist

## Build & runtime

- [x] Maven project builds with Java 21 / JavaFX 21
- [x] `mvn clean test` green (25 tests)
- [x] SQLite DB created under `data/Clinivora.db`
- [x] Schema creates all tables + indexes + FKs
- [x] Additive migrate() for existing DBs (no data wipe)
- [x] Demo accounts seed idempotently

## Authentication & roles

- [x] Login with admin / reception / triage / doctor
- [x] Logout confirmation dialog
- [x] Change password dialog (all roles)
- [x] Sidebar nav filtered by role
- [x] Services enforce role checks

## Record mutation (elevated ADMIN + TRIAGE)

- [x] Soft-delete patients (ADMIN, TRIAGE)
- [x] Restore patients (ADMIN)
- [x] Edit patient demographics (ADMIN, TRIAGE, RECEPTION)
- [x] RECEPTION/DOCTOR denied soft-delete
- [x] Edit emergency case complaint (ADMIN, TRIAGE)
- [x] Soft-deactivate doctors (ADMIN)
- [x] Confirm dialogs + audit + patient_event

## Account management

- [x] User Accounts screen (ADMIN)
- [x] Create / activate / deactivate / reset password
- [x] AccountService unit tests

## Clinical workflows

- [x] Register patient (modal form + ArrayStack undo)
- [x] Patient profile + timeline
- [x] Open emergency case / triage / priority queue
- [x] Case detail assign / treatment / close
- [x] Clinical notes on case detail
- [x] Routine queue enqueue + serve next
- [x] Reports + Audit Log viewer
- [x] Dashboard last-refreshed + waiting longest

## DSA Laboratory

- [x] All 8 custom DSAs + visualizer + benchmark

## UI / branding

- [x] Navy/teal denser ED polish, chips, empty states
- [x] Logo / icon PNGs and Stage icons
- [x] AlertHelper confirms

## Docs

- [x] README.md (roles, accounts, credentials)
- [x] docs/architecture.md
- [x] docs/acceptance-checklist.md
- [x] docs/dsa-reference.md
