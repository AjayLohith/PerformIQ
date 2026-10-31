# PerformIQ — System Architecture Document

## 1. System Overview

**PerformIQ** is an enterprise-grade Employee Performance & Growth Management System. It is engineered as a **modular monolith** in Java 21 / Spring Boot 4.1.2 with a React 19 + TypeScript frontend and a PostgreSQL 17 relational persistence tier.

The platform unifies:
1. Employee directory and temporal managerial reporting hierarchies
2. Bi-annual / quarterly review cycles with strict lifecycle state machines
3. Goal setting & OKRs with weighted progress calculations derived from measurable Key Results
4. Periodic check-ins with supporting evidence attachments
5. 360-degree feedback with rubric-based competency evaluations
6. Formal performance reviews with rating calibration
7. Post-review development plans governed by multi-step approval workflows
8. Anomaly detection, data quality monitoring, and system-wide audit logging
9. File-based data ingestion pipeline with cryptographic manifest verification

---

## 2. Technology Stack & Design Decisions

| Tier | Component | Technology / Version | Rationale |
|------|-----------|----------------------|-----------|
| **Runtime** | JVM | Eclipse Temurin JDK 21 | Modern LTS Java featuring virtual threads, pattern matching, and record patterns |
| **Backend** | Framework | Spring Boot 4.1.2 | High performance, modular architecture, Spring Data JPA, Jakarta EE 11 |
| **Database** | Relational Store | PostgreSQL 17 | Robust ACID transactions, JSONB capability, pgcrypto, check constraints |
| **Migration**| DB Versioning | Flyway | Reproducible, version-controlled database schema migrations |
| **Security** | Auth & RBAC | Spring Security + JJWT 0.13 | Stateless token authentication, role-based authorization |
| **Frontend** | SPA Framework | React 19 + Vite 6 | Fast HMR, concurrent mode, modern component ergonomics |
| **Language** | Type Safety | TypeScript 5.8 | End-to-end typed DTOs and component contracts |
| **Styling**  | Design System | Tailwind CSS 4 + Lucide Icons | Clean modern enterprise design system with responsive layouts |
| **Data Gen** | Synthetic Data | Python 3.12+ / Faker | Deterministic, reproducible synthetic dataset generation |

---

## 3. High-Level Architecture

```mermaid
graph TB
    subgraph "Presentation Tier"
        UI["React 19 SPA<br/>Vite / TypeScript / Tailwind CSS"]
    end

    subgraph "API & Security Layer"
        GW["REST Controllers (/api/v1/*)"]
        SEC["Spring Security Filter Chain<br/>JWT Authentication & Authorization"]
        VAL["Jakarta Bean Validation"]
        OPENAPI["SpringDoc OpenAPI (Swagger)"]
    end

    subgraph "Core Business Domains (Modular Monolith)"
        EMP["Employee Domain"]
        MGR["Manager Hierarchy Domain"]
        REV_CYC["Review Cycle State Machine"]
        GOAL["Goal & Key Result Engine"]
        CHK["Check-In & Evidence Tracking"]
        FB["360 Feedback & Competencies"]
        REV["Performance Review Calibration"]
        DEV["Development Plan Workflow"]
        ANOM["Anomaly Detection Engine"]
        IMP["Data Import & Validation Engine"]
        AUD["Audit Logging Engine"]
    end

    subgraph "Persistence Tier"
        JPA["Spring Data JPA Repositories"]
        HIB["Hibernate ORM 6.x"]
        FLY["Flyway Schema Migrations"]
    end

    subgraph "Data Storage"
        PG[("PostgreSQL 17<br/>performiq_db")]
    end

    subgraph "Synthetic Data Pipeline"
        PY["Python Synthetic Generator (Faker)"]
        FILES["Deterministic CSV / JSON Datasets"]
    end

    UI -->|HTTP / JSON + Bearer JWT| GW
    GW --> SEC
    GW --> VAL
    SEC --> EMP
    SEC --> MGR
    SEC --> REV_CYC
    SEC --> GOAL
    SEC --> CHK
    SEC --> FB
    SEC --> REV
    SEC --> DEV
    SEC --> ANOM
    SEC --> IMP
    SEC --> AUD

    EMP --> JPA
    MGR --> JPA
    REV_CYC --> JPA
    GOAL --> JPA
    CHK --> JPA
    FB --> JPA
    REV --> JPA
    DEV --> JPA
    ANOM --> JPA
    IMP --> JPA
    AUD --> JPA

    JPA --> HIB
    HIB --> PG
    FLY --> PG

    PY -->|Generate Seeded Datasets| FILES
    FILES -->|Admin Import API /api/v1/import| IMP
```

---

## 4. Modular Monolith Architecture

The codebase enforces strict domain boundaries inside `com.performiq.*`:

```
com.performiq/
├── PerformiqBackendApplication.java
├── config/                        # SecurityConfig, CorsConfig, OpenApiConfig
├── common/                        # BaseEntity, GlobalExceptionHandler, ErrorResponse
│   ├── entity/
│   ├── exception/
│   └── dto/
├── auth/                          # Authentication, JWT Filter, UserDetails, AppUser
├── employee/                      # Employee lifecycle & profile management
├── manager/                       # Manager hierarchy & reporting histories
├── reviewcycle/                   # Review cycle state machine (DRAFT -> CLOSED)
├── goal/                          # Goals, progress derivation, change history
├── keyresult/                     # Metric calculations (INCREASE/DECREASE), weights
├── checkin/                       # Periodic check-in submissions
├── evidence/                      # Check-in artifact attachments (URL, METRIC, DOC)
├── competency/                    # Catalog of organizational competencies
├── feedback/                      # Multi-rater feedback & rubric evaluations
├── review/                        # Formal reviews & goal score aggregations
├── development/                   # Development plans & approval histories
├── audit/                         # Security & operational audit logging
├── validation/                    # Anomaly case catalog and data quality checks
├── dataimport/                    # Batch CSV/JSON ingestion engine
└── analytics/                     # Dashboard metric aggregations
```

---

## 5. Security & Authorization Matrix

The application uses stateless JWT bearer tokens with standard Role-Based Access Control:

| Role | Permissions & Boundaries |
|------|--------------------------|
| **EMPLOYEE** | Read own profile; create/manage own goals; submit check-ins; submit self & assigned peer feedbacks; view own completed reviews and development plans. |
| **MANAGER** | All employee capabilities; view direct reports; conduct manager evaluations; score reviews for direct reports; approve/reject development plans. |
| **HR_PARTNER** | View organization-wide review cycles; initiate review cycles; monitor completion percentages; view aggregate ratings and distribution charts. |
| **OPERATIONS** | System health monitoring; trigger and inspect synthetic data import runs; view anomaly detection reports and validation discrepancies. |
| **ADMIN** | Full system governance; manage user accounts; override review cycle states; purge/reload test environments; inspect complete audit logs. |
