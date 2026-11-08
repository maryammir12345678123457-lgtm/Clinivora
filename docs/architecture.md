# Clinivora Architecture

## Layers

1. **UI (JavaFX / FXML)** – Controllers under `com.Clinivora.ui` load views from `/com/Clinivora/view`.
2. **Services** – Business rules, authorization checks, DSA-backed workflows.
3. **Repositories** – JDBC `PreparedStatement` access to SQLite.
4. **DSA** – Custom structures used by services and the DSA Laboratory (no `ArrayList`/`HashMap` internals).

## Composition root

`AppContext` constructs repositories and services once.  
`App` bootstraps the schema + demo seed, then shows `LoginView`. After login, `MainShellView` hosts content pages.

## Navigation

`NavigationService` tracks the current `View`, history (`ArrayStack`), selected patient/case IDs, and role-gated visibility.  
`MainShellController` listens via `setOnNavigate` and loads the matching FXML into the content pane.

## Data

- Single SQLite database: `data/Clinivora.db`
- Schema: `src/main/resources/db/schema.sql`
- Foreign keys enabled (`PRAGMA foreign_keys = ON`)
- Tables: `app_user`, `patient`, `doctor`, `emergency_case`, `triage_assessment`, `case_assignment`, `service_ticket`, `patient_event`, `audit_log`

## AuthZ

`AuthorizationService` + service-level `requireRole` calls enforce permissions.  
Sidebar buttons are hidden by role; attempting a forbidden view throws `SecurityException`.

## Role → primary screens

| Role       | Primary areas                                      |
|------------|----------------------------------------------------|
| ADMIN      | All screens                                        |
| RECEPTION  | Dashboard, Patients, ER, Routine Queue, DSA Lab     |
| TRIAGE     | Dashboard, ER, Case detail, DSA Lab                |
| DOCTOR     | Dashboard, ER, Case detail, Doctors, DSA Lab       |

## DSA usage in the product

| Structure          | Product usage                                      |
|--------------------|----------------------------------------------------|
| ArrayStack         | Navigation history; patient form undo              |
| IndexedMinHeap     | Emergency priority queue                           |
| CircularQueue      | Routine queue call-next simulation                 |
| DoublyLinkedList   | Patient timeline ordering                          |
| HashTable          | Report aggregation / DSA lab                       |
| DynamicArray       | Heap storage; graph adjacency lists                |
| BinarySearchTree   | DSA lab / ordered lookup demos                     |
| AdjacencyListGraph | Care-flow graph demo in DSA lab                    |
