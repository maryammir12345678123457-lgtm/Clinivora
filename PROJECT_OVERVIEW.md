# Clinivora

Desktop emergency & clinic queue management system built with **Java 21**, **JavaFX 21**, **FXML/CSS**, **SQLite**, **OpenPDF**, and **Maven**.

Package root: `com.Clinivora`  
Database file: `data/Clinivora.db`

## Prerequisites

- JDK 21 (Eclipse Adoptium Temurin 21 recommended)
- Apache Maven 3.9+

## Run

```powershell
mvn clean test
mvn javafx:run
```

## UI

Clinivora uses a **Material-inspired** portal interface with selectable themes:

- Roboto fonts (`src/main/resources/fonts/`)
- Themes: **Light** (default), **Dark**, **Pink**, **Colorful**
- Languages: **English**, **Urdu** (RTL), **Chinese**
- Preferences persist in `data/clinivora-settings.properties`
- Change theme/language on Login or in **Settings**

## Features

- Patient registration and profile management
- Emergency Room, triage, and case management
- Routine clinic queue management
- Doctor and user account management
- Role-based access: **ADMIN**, **RECEPTION**, **TRIAGE**, **DOCTOR**
- Reports with filters, charts, PDF and CSV export
- Audit Log and Change Password
- DSA Laboratory

## Demo credentials

Use only disposable demo accounts with synthetic data. Do not publish real credentials or expose sensitive systems.

## New user password & username rules

- **Username:** 3–40 characters; letters, numbers, `.`, `_`, `-` only; stored lowercase
- **Password:** At least 8 characters with uppercase, lowercase, digit, and special character
- **Role:** Required (defaults to `RECEPTION` in the form)

## Reports

ADMIN **Reports** includes:

- Date range and case status filters
- Pie chart (case status mix) and bar chart (daily arrivals)
- Detailed case table (patient, arrival, complaint, ESI)
- Narrative summary
- **Export PDF** (OpenPDF) and **Export CSV** (UTF-8)

## Screens

- Login, Main Shell, Dashboard, Settings
- Patients, Patient Profile, Emergency Room, Case Detail
- Routine Queue, Doctors, Reports, User Accounts
- Audit Log, DSA Laboratory, Change Password

## Who can edit /
