PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS app_user (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    username        TEXT    NOT NULL UNIQUE,
    password_hash   TEXT    NOT NULL,
    salt            TEXT    NOT NULL,
    role            TEXT    NOT NULL,
    display_name    TEXT    NOT NULL,
    active          INTEGER NOT NULL DEFAULT 1,
    created_at      TEXT    NOT NULL,
    updated_at      TEXT    NOT NULL
);

CREATE TABLE IF NOT EXISTS patient (
    id                  INTEGER PRIMARY KEY AUTOINCREMENT,
    display_code        TEXT    NOT NULL UNIQUE,
    first_name          TEXT    NOT NULL,
    last_name           TEXT    NOT NULL,
    date_of_birth       TEXT,
    phone               TEXT,
    gender              TEXT,
    national_id         TEXT,
    address             TEXT,
    emergency_contact   TEXT,
    active              INTEGER NOT NULL DEFAULT 1,
    created_at          TEXT    NOT NULL,
    updated_at          TEXT    NOT NULL
);

CREATE TABLE IF NOT EXISTS doctor (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id         INTEGER REFERENCES app_user(id),
    display_code    TEXT    NOT NULL UNIQUE,
    full_name       TEXT    NOT NULL,
    specialty       TEXT,
    available       INTEGER NOT NULL DEFAULT 1,
    active          INTEGER NOT NULL DEFAULT 1,
    created_at      TEXT    NOT NULL
);

CREATE TABLE IF NOT EXISTS emergency_case (
    id                  INTEGER PRIMARY KEY AUTOINCREMENT,
    display_code        TEXT    NOT NULL UNIQUE,
    patient_id          INTEGER NOT NULL REFERENCES patient(id),
    chief_complaint     TEXT,
    arrival_time        TEXT    NOT NULL,
    status              TEXT    NOT NULL,
    case_type           TEXT    NOT NULL,
    created_by          INTEGER REFERENCES app_user(id),
    created_at          TEXT    NOT NULL,
    updated_at          TEXT    NOT NULL
);

CREATE TABLE IF NOT EXISTS triage_assessment (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    case_id         INTEGER NOT NULL UNIQUE REFERENCES emergency_case(id),
    priority        TEXT    NOT NULL,
    vital_signs     TEXT,
    notes           TEXT,
    assessed_by     INTEGER REFERENCES app_user(id),
    assessed_at     TEXT    NOT NULL
);

CREATE TABLE IF NOT EXISTS case_assignment (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    case_id         INTEGER NOT NULL REFERENCES emergency_case(id),
    doctor_id       INTEGER NOT NULL REFERENCES doctor(id),
    assigned_at     TEXT    NOT NULL,
    released_at     TEXT,
    status          TEXT    NOT NULL,
    assigned_by     INTEGER REFERENCES app_user(id)
);

CREATE TABLE IF NOT EXISTS service_ticket (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    display_code    TEXT    NOT NULL UNIQUE,
    patient_id      INTEGER NOT NULL REFERENCES patient(id),
    case_id         INTEGER REFERENCES emergency_case(id),
    queue_number    INTEGER,
    status          TEXT    NOT NULL,
    created_at      TEXT    NOT NULL,
    called_at       TEXT,
    completed_at    TEXT
);

CREATE TABLE IF NOT EXISTS patient_event (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    patient_id      INTEGER NOT NULL REFERENCES patient(id),
    case_id         INTEGER REFERENCES emergency_case(id),
    event_type      TEXT    NOT NULL,
    description     TEXT,
    created_by      INTEGER REFERENCES app_user(id),
    created_at      TEXT    NOT NULL
);

CREATE TABLE IF NOT EXISTS case_note (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    case_id         INTEGER NOT NULL REFERENCES emergency_case(id),
    body            TEXT    NOT NULL,
    created_by      INTEGER REFERENCES app_user(id),
    created_at      TEXT    NOT NULL
);

CREATE TABLE IF NOT EXISTS audit_log (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id         INTEGER REFERENCES app_user(id),
    action          TEXT    NOT NULL,
    entity_type     TEXT,
    entity_id       INTEGER,
    details         TEXT,
    created_at      TEXT    NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_patient_display_code ON patient(display_code);
CREATE INDEX IF NOT EXISTS idx_patient_name ON patient(last_name, first_name);
-- idx_patient_active is created in DatabaseInitializer.migrate() after the column exists on older DBs
CREATE INDEX IF NOT EXISTS idx_emergency_case_status ON emergency_case(status);
CREATE INDEX IF NOT EXISTS idx_emergency_case_patient ON emergency_case(patient_id);
CREATE INDEX IF NOT EXISTS idx_emergency_case_arrival ON emergency_case(arrival_time);
CREATE INDEX IF NOT EXISTS idx_triage_case ON triage_assessment(case_id);
CREATE INDEX IF NOT EXISTS idx_triage_priority ON triage_assessment(priority);
CREATE INDEX IF NOT EXISTS idx_assignment_case ON case_assignment(case_id);
CREATE INDEX IF NOT EXISTS idx_assignment_doctor ON case_assignment(doctor_id);
CREATE INDEX IF NOT EXISTS idx_assignment_status ON case_assignment(status);
CREATE INDEX IF NOT EXISTS idx_ticket_status ON service_ticket(status);
CREATE INDEX IF NOT EXISTS idx_ticket_patient ON service_ticket(patient_id);
CREATE INDEX IF NOT EXISTS idx_patient_event_patient ON patient_event(patient_id);
CREATE INDEX IF NOT EXISTS idx_patient_event_case ON patient_event(case_id);
CREATE INDEX IF NOT EXISTS idx_case_note_case ON case_note(case_id);
CREATE INDEX IF NOT EXISTS idx_audit_user ON audit_log(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_created ON audit_log(created_at);
CREATE INDEX IF NOT EXISTS idx_audit_action ON audit_log(action);
CREATE INDEX IF NOT EXISTS idx_doctor_available ON doctor(available);
-- idx_doctor_active is created in DatabaseInitializer.migrate() after the column exists on older DBs
CREATE INDEX IF NOT EXISTS idx_app_user_username ON app_user(username);
