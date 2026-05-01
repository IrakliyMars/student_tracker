# Frontend Integration Guide (Studio Student Management)

This document is a concise, end-to-end reference for building a frontend client for the backend API.
It summarizes data models, endpoints, request/response contracts, business rules, and UI flows.

Primary sources:
- `README.md` (API overview + examples)
- `HOW_IT_WORKS.md` (plain-English business behavior)
- `BUSINESS_LOGIC.md` (edge cases + error behavior)

---

## 1) Quick overview

- Domain: private teaching studio tracking students, schedules, sessions, payments, and earnings.
- API base: `http://localhost:8080`
- No authentication/authorization implemented.
- API version constant: `0.1` (`ApiConstants.VERSION`).
- Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## 2) Core concepts (frontend mental model)

- **Student**: profile + pricing model + lifecycle flags (holiday/stopped) + debtor flag.
- **WeeklySchedule**: recurring slot (day/time/duration). Does **not** create sessions automatically.
- **ClassSession**: a concrete class on a specific date (scheduled, completed, cancelled, moved).
- **PackagePurchase**: prepaid bundle of N classes; FIFO deduction when paying sessions.
- **Payer**: third-party contact who pays (does not affect payment logic).

---

## 3) Timezones and time handling

- Supported timezones: `SPAIN`, `RUSSIA_MOSCOW`.
- Many session endpoints accept `timezone` query param (default `SPAIN`).
- Session responses include:
  - `classDate` / `startTime` (in viewer timezone)
  - `originalClassDate` / `originalStartTime` (stored values)
  - `timezone` / `originalTimezone` / `viewerTimezone`
- All time inputs/outputs are ISO-8601 (`YYYY-MM-DD`, `HH:mm`, `YYYY-MM-DDTHH:mm:ss`).

---

## 4) Error response shapes

**Business and server errors** (`400`, `404`, `409`, `500`):
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Session is already paid",
  "path": "/api/sessions/5/pay",
  "timestamp": "2026-04-12T10:30:00"
}
```

**Validation errors** (`400`):
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": {
    "fieldName": "validation message"
  },
  "path": "/api/students",
  "timestamp": "2026-04-12T10:30:00"
}
```

---

## 5) Enums

- `PricingType`: `PER_CLASS`, `PACKAGE`
- `ClassStatus`: `SCHEDULED`, `COMPLETED`, `CANCELLED`, `MOVED`
- `PaymentStatus`: `UNPAID`, `PAID`, `PACKAGE`, `REFUNDED`
- `Currency`: `EUROS` (`EUR`), `DOLLARS` (`USD`), `RUBLES` (`RUB`)
- `StudioTimezone`: `SPAIN`, `RUSSIA_MOSCOW`
- `StudentClassType`: `CASUAL`, `EGE`, `OGE`, `IELTS`, `TOFEL`

---

## 6) Data models (DTO contracts)

### 6.1 Student
**CreateStudentRequest**
```json
{
  "firstName": "Ana",
  "lastName": "Garcia",
  "phoneNumber": "+34 600 000 000",
  "pricingType": "PER_CLASS",
  "pricePerClass": 35.0,
  "currency": "EUROS",
  "timezone": "SPAIN",
  "classType": "CASUAL",
  "startDate": "2026-03-01",
  "holidayMode": false,
  "holidayFrom": null,
  "holidayTo": null,
  "stoppedAttending": false,
  "notes": "Prefers morning classes"
}
```

**UpdateStudentRequest** (partial update; only non-null fields applied)
```json
{
  "firstName": "Ana",
  "pricingType": "PACKAGE",
  "holidayMode": true,
  "holidayFrom": "2026-06-01"
}
```

**StudentResponse**
```json
{
  "id": 12,
  "firstName": "Ana",
  "lastName": "Garcia",
  "fullName": "Ana Garcia",
  "phoneNumber": "+34 600 000 000",
  "pricingType": "PER_CLASS",
  "pricePerClass": 35.0,
  "currency": "EUROS",
  "convertedPrices": { "DOLLARS": 38.0, "EUROS": 35.0, "RUBLES": 3200.0 },
  "timezone": "SPAIN",
  "classType": "CASUAL",
  "startDate": "2026-03-01",
  "holidayMode": false,
  "holidayFrom": null,
  "holidayTo": null,
  "stoppedAttending": false,
  "notes": "Prefers morning classes",
  "debtor": false,
  "createdAt": "2026-03-01T09:00:00",
  "weeklySchedules": [ ... ],
  "payers": [ ... ]
}
```

Key rules:
- `PER_CLASS`: `pricePerClass` required, `currency` required when price is set.
- `PACKAGE`: student-level `pricePerClass` and `currency` must be `null`.
- Switching to `PACKAGE` clears `pricePerClass` + `currency` on the student.

---

### 6.2 Weekly schedule
**WeeklyScheduleRequest**
```json
{ "dayOfWeek": "MONDAY", "startTime": "10:00", "durationMinutes": 60 }
```
Or:
```json
{ "dayOfWeek": "MONDAY", "startTime": "10:00", "endTime": "11:00" }
```

**WeeklyScheduleResponse**
```json
{
  "id": 101,
  "studentId": 12,
  "dayOfWeek": "MONDAY",
  "startTime": "10:00:00",
  "durationMinutes": 60,
  "endTime": "11:00:00"
}
```

**WeeklyPlanningScheduleResponse** (for planning UI)
```json
{
  "id": 1,
  "studentId": 12,
  "studentName": "Ana Garcia",
  "timezone": "SPAIN",
  "dayOfWeek": "MONDAY",
  "startTime": "10:00:00",
  "endTime": "11:00:00",
  "durationMinutes": 60
}
```

---

### 6.3 Sessions (classes)
**OneOffSessionRequest**
```json
{ "classDate": "2026-04-20", "startTime": "11:00", "durationMinutes": 60, "note": "Moved" }
```

**UpdateSessionRequest**
```json
{
  "classDate": "2026-04-20",
  "startTime": "11:00",
  "durationMinutes": 60,
  "status": "COMPLETED",
  "paid": true,
  "paymentDateTime": "2026-04-20T18:30:00",
  "amountOverride": 30.0,
  "note": "Conducted and paid"
}
```

**CancelSessionRequest**
```json
{ "keepAsPaid": false, "note": "Student sick" }
```

**PaySessionRequest**
```json
{ "paymentDateTime": "2026-04-20T18:30:00", "amountOverride": 30.0 }
```

**ClassSessionResponse**
```json
{
  "id": 555,
  "studentId": 12,
  "studentName": "Ana Garcia",
  "classDate": "2026-04-20",
  "startTime": "11:00:00",
  "originalClassDate": "2026-04-20",
  "originalStartTime": "11:00:00",
  "timezone": "SPAIN",
  "originalTimezone": "SPAIN",
  "viewerTimezone": "SPAIN",
  "durationMinutes": 60,
  "status": "SCHEDULED",
  "paymentStatus": "UNPAID",
  "priceCharged": 35.0,
  "currency": "EUROS",
  "paymentDateTime": null,
  "convertedPrices": { "EUROS": 35.0, "DOLLARS": 38.0, "RUBLES": 3200.0 },
  "packagePurchaseId": null,
  "oneOff": true,
  "note": "Moved"
}
```

Notes:
- `paymentDateTime` is required for PER_CLASS payments.
- `convertedPrices` may be empty if conversion unavailable.

---

### 6.4 Packages
**PackagePurchaseRequest**
```json
{
  "totalClasses": 10,
  "amountPaid": 280.0,
  "currency": "EUROS",
  "paymentDate": "2026-04-01",
  "description": "Spring bundle"
}
```

**PackagePurchaseResponse**
```json
{
  "id": 77,
  "studentId": 12,
  "studentName": "Ana Garcia",
  "totalClasses": 10,
  "classesRemaining": 7,
  "amountPaid": 280.0,
  "currency": "EUROS",
  "convertedAmountPaid": { "EUROS": 280.0, "DOLLARS": 300.0, "RUBLES": 26000.0 },
  "paymentDate": "2026-04-01",
  "description": "Spring bundle",
  "exhausted": false,
  "createdAt": "2026-04-01T09:00:00"
}
```

---

### 6.5 Payers
**PayerRequest**
```json
{ "fullName": "Maria Garcia", "phoneNumber": "+34 600 111 222", "note": "Mother" }
```

**PayerResponse**
```json
{
  "id": 5,
  "studentId": 12,
  "studentName": "Ana Garcia",
  "fullName": "Maria Garcia",
  "phoneNumber": "+34 600 111 222",
  "note": "Mother"
}
```

---

### 6.6 Calendar
**CalendarDayResponse**
```json
{
  "date": "2026-04-20",
  "totalHours": 3.0,
  "completedHours": 2.0,
  "sessions": [ ...ClassSessionResponse... ]
}
```

---

### 6.7 Earnings
**DailyEarningsResponse**
```json
{
  "date": "2026-04-20",
  "sessionCount": 2,
  "earningsByCurrency": { "EUROS": 70.0 },
  "totalInBaseCurrency": 70.0,
  "baseCurrency": "EUROS",
  "convertedTotals": { "EUROS": 70.0, "DOLLARS": 76.0, "RUBLES": 6400.0 }
}
```

**PeriodEarningsResponse**
```json
{
  "from": "2026-04-01",
  "to": "2026-04-30",
  "dailyBreakdown": [ ...DailyEarningsResponse... ],
  "totalEarnedByCurrency": { "EUROS": 500.0 },
  "totalEarnedInBaseCurrency": 500.0,
  "totalCouldHaveEarnedExcludingCancellationsByCurrency": { "EUROS": 650.0 },
  "totalCouldHaveEarnedExcludingCancellationsInBaseCurrency": 650.0,
  "totalCouldHaveEarnedIncludingCancellationsByCurrency": { "EUROS": 700.0 },
  "totalCouldHaveEarnedIncludingCancellationsInBaseCurrency": 700.0,
  "baseCurrency": "EUROS",
  "convertedTotalEarned": { "EUROS": 500.0, "DOLLARS": 540.0, "RUBLES": 46000.0 },
  "convertedTotalCouldHaveEarnedExcludingCancellations": { ... },
  "convertedTotalCouldHaveEarnedIncludingCancellations": { ... }
}
```

**MonthlyEarningsResponse**
```json
{
  "year": 2026,
  "month": 4,
  "totalSessionCount": 12,
  "sessionEarningsByCurrency": { "EUROS": 420.0 },
  "totalPackageCount": 2,
  "packageEarningsByCurrency": { "EUROS": 560.0 },
  "totalEarningsByCurrency": { "EUROS": 980.0 },
  "totalInBaseCurrency": 980.0,
  "baseCurrency": "EUROS",
  "convertedTotals": { "EUROS": 980.0, "DOLLARS": 1060.0, "RUBLES": 90000.0 },
  "dailyBreakdown": [ ...DailyEarningsResponse... ]
}
```

**PaymentRecordResponse**
```json
{
  "paymentType": "SESSION",
  "paymentDateTime": "2026-04-20T18:30:00",
  "amount": 35.0,
  "currency": "EUROS",
  "studentId": 12,
  "studentName": "Ana Garcia",
  "sessionId": 555,
  "packagePurchaseId": null,
  "note": "Conducted and paid"
}
```

---

### 6.8 Data export/import
**DataImportResultResponse**
```json
{ "students": 10, "weeklySchedules": 20, "packagePurchases": 5, "classSessions": 200, "payers": 8 }
```

---

## 7) API endpoints (frontend mapping)

### Students — `/api/students`
- `POST /api/students` → create student
- `GET /api/students` → list students (pagination + filters)
  - `search` (string, optional)
  - `debtor` (boolean, optional)
  - `packagePricing` (boolean, optional; true=PACKAGE, false=PER_CLASS)
  - `page` (default `0`), `size` (default `20`)
- `GET /api/students/search?query=...` → search by student or payer name
- `GET /api/students/{id}` → get student
- `PATCH /api/students/{id}` → partial update
- `DELETE /api/students/{id}` → soft delete

### Weekly schedules — `/api/students/{studentId}/schedules`
- `POST /api/students/{id}/schedules` → add **list** of schedules
- `GET /api/students/{id}/schedules` → list schedules
- `GET /api/students/schedules/weekly-planning` → all recurring slots (planning UI)
- `POST /api/students/{id}/schedules/{scheduleId}` → update **list** (first item updates, others create)
- `POST /api/students/{id}/schedules/{scheduleId}/delete` → soft delete

### Student sessions — `/api/students/{studentId}/sessions`
- `POST /api/students/{id}/sessions` → create one-off session (`timezone` query param)
- `GET /api/students/{id}/sessions?from=&to=` → list sessions by date range
- `GET /api/students/{id}/sessions/by-payment?paymentStatus=PAID|UNPAID|PACKAGE|REFUNDED`

### Session actions — `/api/sessions/{sessionId}`
- `GET /api/sessions/{id}` → get session
- `PUT /api/sessions/{id}` → unified update (status/payment toggle/time/note)
- `POST /api/sessions/{id}/cancel` → cancel session
- `POST /api/sessions/{id}/pay` → pay session
- `POST /api/sessions/{id}/completion?completed=true|false` → set completion state
- `POST /api/sessions/{id}/cancel-payment` → revert payment

### Packages — `/api/students/{studentId}/packages`
- `POST /api/students/{id}/packages` → purchase package
- `GET /api/students/{id}/packages` → list all packages (newest first)
- `GET /api/students/{id}/packages/active` → list active packages (FIFO order)
- `GET /api/packages/{packageId}` → get one package

### Payers — `/api/students/{studentId}/payers`
- `POST /api/students/{id}/payers` → add payer
- `GET /api/students/{id}/payers` → list payers (pagination)
- `POST /api/students/{id}/payers/{payerId}` → update payer
- `POST /api/students/{id}/payers/{payerId}/delete` → soft delete

### Calendar — `/api/calendar`
- `GET /api/calendar?from=&to=` → sessions grouped by day (defaults today→30 days)

### Earnings — `/api/earnings`
- `GET /api/earnings/daily?from=&to=&baseCurrency=`
- `GET /api/earnings/monthly?year=&month=&baseCurrency=`
- `GET /api/earnings/payments?page=&size=`

### Data portability — `/api/data`
- `GET /api/data/export` → `application/octet-stream` (GZIP JSON)
- `POST /api/data/import` → consumes `application/json`, `application/octet-stream`, or `application/gzip`
- `POST /api/data/import-file` → `multipart/form-data` upload

---

## 8) Pagination responses

Any endpoint returning `Page<T>` follows Spring Data pagination shape (typical):
```json
{
  "content": [ ... ],
  "totalElements": 120,
  "totalPages": 6,
  "size": 20,
  "number": 0,
  "numberOfElements": 20,
  "first": true,
  "last": false,
  "empty": false,
  "sort": { "sorted": false, "unsorted": true, "empty": true }
}
```

---

## 9) Business rules (frontend-sensitive)

- **Schedule vs sessions**: schedules are templates only; they do not create class records.
- **Student availability checks** before creating sessions:
  - date must be on/after `startDate`
  - student must not be in active holiday (when `holidayMode=true` and date >= `holidayFrom`)
  - student must not be `stoppedAttending=true`
- **Holiday transitions**:
  - Setting `holidayMode=true` requires `holidayFrom` and auto-cancels sessions from that date.
  - Setting `holidayMode=false` requires `holidayTo` and restores cancelled sessions from that date.
- **Cancellation keepAsPaid**:
  - `keepAsPaid=true` keeps payment status as-is, even when cancelled.
  - `keepAsPaid=false` resets per-class payment or returns package slot.
- **Package FIFO**: payment on PACKAGE students always consumes the oldest active package first.
- **Debtor flag**: recomputed in batch after 22:00 local time; startup catch-up runs on boot.

---

## 10) Suggested frontend screens and data needs

- **Students list**: `GET /api/students` with filters + pagination.
- **Student search**: `GET /api/students/search` (student/payer names).
- **Student detail**: `GET /api/students/{id}` (shows schedules + payers).
- **Schedule editor**: `GET /api/students/{id}/schedules` + `POST` for add/update.
- **Weekly planning**: `GET /api/students/schedules/weekly-planning`.
- **Sessions list by student**: `GET /api/students/{id}/sessions` (date filters).
- **Session detail**: `GET /api/sessions/{id}`.
- **Payment actions**: `/pay`, `/cancel`, `/cancel-payment`, `/completion`.
- **Packages**: `GET /api/students/{id}/packages` + `POST` create.
- **Calendar view**: `GET /api/calendar`.
- **Earnings dashboards**: `/api/earnings/daily`, `/monthly`, `/payments`.
- **Data export/import**: `/api/data/export`, `/api/data/import(-file)`.

---

## 11) Frontend edge-case notes

- When switching a student to `PACKAGE`, expect `pricePerClass` and `currency` to be null in subsequent responses.
- A cancelled session can still be `PAID` or `PACKAGE` if it was kept as paid.
- `convertedPrices` or `convertedAmountPaid` may be empty (no conversion cache).
- `PaymentRecordResponse.paymentDateTime` for package purchases is set to the package `paymentDate` at `00:00`.
- Use `viewerTimezone` query param to show session times in a chosen timezone.

---

## 12) Related docs

- Business cases and error details: `BUSINESS_LOGIC.md`
- Plain-English rules and workflows: `HOW_IT_WORKS.md`
- Full API summary and samples: `README.md`

