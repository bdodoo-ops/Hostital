# Hospital Management System (HospitalGUI)

A standalone desktop Hospital Management System built in Java (Swing), covering ten clinical/administrative modules on top of a shared Patients/Doctors registry.

## Modules
1. Queue Management
2. Medical Records
3. Pharmacy
4. Appointments
5. Blood Donation
6. Vaccination
7. Ambulance Dispatch
8. Laboratory
9. Billing & Payment
10. Maternal Health

## Architecture
- `base/` — `Entity` (abstract), `Manager<T>` (generic abstract CRUD + file persistence), `Displayable` interface
- `model/` — 24 entity classes, one or more per module
- `manager/` — 10 manager classes with business rules (billing, stock, queue priority, etc.)
- `panel/` — 10 Swing panels extending a shared `BasePanel`
- `ui/` — `MainWindow` (app shell), `FormDialog` (generic modal form)
- `util/` — `AppTheme`, `Validator`, `FileUtil`
- `data/` — flat, pipe-delimited text files (one per entity type), used as the persistence layer

No external database or server is required.

## Running

Requires a JDK (path is set in the batch files — adjust if yours differs).

```
compile.bat
run.bat
```

## Screenshots

See [screenshots/](screenshots) for UI captures of every module.

## Documentation

See [HospitalGUI_Technical_Report.docx](HospitalGUI_Technical_Report.docx) for the full technical report (architecture, OOP concepts, class descriptions).
