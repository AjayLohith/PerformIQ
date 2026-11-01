<div align="center">

# PerformIQ

### Employee Performance & Growth Management System

An enterprise modular monolith for high-velocity organizations to manage performance review cycles, OKRs, 360° multi-rater feedback, career development pathways, and tamper-evident audit trails.

[![Java 21](https://img.shields.io/badge/Java-21-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.2-6DB33F?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-316192?style=flat-square&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![React](https://img.shields.io/badge/React-19.3-20232A?style=flat-square&logo=react&logoColor=61DAFB)](https://react.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.x-007ACC?style=flat-square&logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-4.x-38B2AC?style=flat-square&logo=tailwind-css&logoColor=white)](https://tailwindcss.com/)
[![Tests](https://img.shields.io/badge/Tests-15%2F15%20Passing-brightgreen?style=flat-square)](backend)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=flat-square)](LICENSE)

[Features](#key-features) • [Quick Start](#quick-start) • [Demo Accounts](#demo-personas--role-access) • [Project Structure](#project-structure) • [Architecture](#architecture) • [Business Rules](#core-business-logic) • [Testing](#testing--quality-assurance)

</div>

---

## Overview

**PerformIQ** bridges organizational OKRs with ground-level career development. Built with relational constraints, transactional business rules, and an audit ledger, PerformIQ provides full traceability across the employee review lifecycle.

```mermaid
flowchart LR
    A["Review Cycle (DRAFT)"] --> B["Cycle Active (Goal Setting)"]
    B --> C["Check-Ins & KR Updates"]
    C --> D["360 Feedback (Self, Peer, Manager)"]
    D --> E["Review Scoring (60% OKR + 40% Feedback)"]
    E --> F["Development Plan (Approval Workflow)"]
    F --> G["Cycle Closed (Audit Sealed)"]
```

---

## Key Features

- **Goal & OKR Progress Cascade**: Key Results support both `INCREASE` and `DECREASE` metric types with zero-division safeguards, normalized progress bounds [0, 100]%, and real-time weighted aggregation into parent goals.
- **Cycle Weight Constraint**: Enforces a strict $\sum \le 100\%$ total goal weight per employee per review cycle.
- **360° Multi-Rater Feedback**: Self, Manager, Peer, and Upward feedback collection with 8 standardized competency rubrics and multi-dimensional scoring summaries.
- **Composite Review Scoring**: Mathematically balanced formula blending 60% OKR Goal Achievement and 40% Multi-Rater Feedback.
- **Two-Phase Development Plan Approvals**: State machine workflow (`DRAFT -> SUBMITTED -> APPROVED / REJECTED -> IN_PROGRESS -> COMPLETED`) with mandatory coaching justification on rejections.
- **Append-Only SOC2 Audit Ledger**: Transactional state mutation logging with a JSON side-by-side state diff inspector.
- **Synthetic Data & Anomaly Injection**: Python Faker generator producing 7,489 benchmark records with SHA-256 manifest verification and 10 calibrated anomaly test cases.

---

## Quick Start

### 1. In-Memory Mode (Zero Docker Setup)

Run the entire platform locally without external database installation:

```bash
# Terminal 1: Backend (Spring Boot with In-Memory PostgreSQL H2 compatibility)
cd backend
./mvnw spring-boot:run "-Dspring-boot.run.profiles=local"
```

```bash
# Terminal 2: Frontend (React 19 + Vite 6)
cd frontend
npm run dev
```

- **Web Application**: `http://localhost:5173`
- **REST API**: `http://localhost:8080/api/v1`
- **Swagger UI**: `http://localhost:8080/api/v1/swagger-ui.html`

---

### 2. Production Docker Mode

For production environments with containerized PostgreSQL 17:

```bash
# Start PostgreSQL Container
docker compose up -d

# Run Backend (Flyway automatically executes migrations V1–V4)
cd backend
./mvnw spring-boot:run

# Run Frontend
cd frontend
npm run dev
```

---

## Demo Personas & Role Access

PerformIQ provides a **1-Click Demo Persona Switcher** on the login page and top navigation bar. Every persona is pre-configured with the default password: `password123`.

| Persona | Role | Username | Department | Scope of Responsibility |
| :--- | :--- | :--- | :--- | :--- |
| **Platform Admin** | `ADMIN` | `admin` | People Operations | Full system governance, batch ingestion, review cycle state transitions, SOC2 audit trail |
| **Sarah Chen** | `MANAGER` | `sarah.chen` | Engineering | VP of Engineering: team overview, composite performance scoring studio, plan approvals |
| **Marcus Vance** | `MANAGER` | `marcus.vance` | Engineering | Director of Engineering: direct report coaching, mandatory rejection reason workflows |
| **Elena Rostova** | `EMPLOYEE` | `elena.rostova` | Engineering | Staff Software Engineer: interactive OKR sliders, check-in evidence, 360 feedback hub |
| **Alex Rivera** | `EMPLOYEE` | `alex.rivera` | Engineering | Senior Staff Engineer: technical milestones, career development roadmap |

---

## Project Structure

Click on any directory or file below to jump directly to it:

- 📦 [**`backend/`**](backend/) — Spring Boot 4.1.2 & Java 21 REST API Monolith
  - 📂 [`src/main/java/com/performiq/`](backend/src/main/java/com/performiq/) — Domain packages
    - 📁 [`audit/`](backend/src/main/java/com/performiq/audit/) — Transactional audit trail & ledger
    - 📁 [`auth/`](backend/src/main/java/com/performiq/auth/) — JWT security filter, auth controller & user repository
    - 📁 [`checkin/`](backend/src/main/java/com/performiq/checkin/) — Milestone check-ins, reflection notes & snapshot history
    - 📁 [`common/`](backend/src/main/java/com/performiq/common/) — Global exception handling, base entities & standard DTOs
    - 📁 [`competency/`](backend/src/main/java/com/performiq/competency/) — 8 core competency definitions & rating rubrics
    - 📁 [`config/`](backend/src/main/java/com/performiq/config/) — Security filter chain, Swagger OpenAPI & local profiles
    - 📁 [`dataimport/`](backend/src/main/java/com/performiq/dataimport/) — CSV batch data ingestion engine
    - 📁 [`development/`](backend/src/main/java/com/performiq/development/) — Growth plans & manager coaching approval workflow
    - 📁 [`employee/`](backend/src/main/java/com/performiq/employee/) — Employee profiles, organizational directory & reporting tree
    - 📁 [`evidence/`](backend/src/main/java/com/performiq/evidence/) — Milestone deliverables, PR attachments & URL artifacts
    - 📁 [`feedback/`](backend/src/main/java/com/performiq/feedback/) — 360° multi-rater feedback matrix (Self, Peer, Manager)
    - 📁 [`goal/`](backend/src/main/java/com/performiq/goal/) — OKR goals, lifecycle state machine & cycle weight validation ($\le 100\%$)
    - 📁 [`keyresult/`](backend/src/main/java/com/performiq/keyresult/) — INCREASE / DECREASE metrics, target tracking & cascade math
    - 📁 [`manager/`](backend/src/main/java/com/performiq/manager/) — Manager assignments & team oversight
    - 📁 [`review/`](backend/src/main/java/com/performiq/review/) — Performance review cycle evaluations & 60/40 composite scoring
    - 📁 [`reviewcycle/`](backend/src/main/java/com/performiq/reviewcycle/) — Bi-annual review cycles & transition state pipeline
    - 📁 [`validation/`](backend/src/main/java/com/performiq/validation/) — Data anomaly cases & rule audits
  - 📂 [`src/main/resources/`](backend/src/main/resources/)
    - 📁 [`db/migration/`](backend/src/main/resources/db/migration/) — Flyway relational migrations (`V1`–`V4`)
    - 📄 [`application.yaml`](backend/src/main/resources/application.yaml) — PostgreSQL 17 production database configuration
    - 📄 [`application-local.yaml`](backend/src/main/resources/application-local.yaml) — In-memory H2 configuration
  - 📂 [`src/test/java/com/performiq/`](backend/src/test/java/com/performiq/) — Test suites (unit, integration & security)
  - 📄 [`pom.xml`](backend/pom.xml) — Maven project definition & dependency configuration
- 📦 [**`frontend/`**](frontend/) — React 19 + TypeScript + Vite 6 Single-Page Application
  - 📂 [`src/components/`](frontend/src/components/) — Layout, Sidebar, Navbar, and persona switcher
  - 📂 [`src/context/`](frontend/src/context/) — Authentication context & JWT session storage
  - 📂 [`src/pages/`](frontend/src/pages/) — Role-tailored dashboards, OKR tracker, 360 feedback & development plans
  - 📂 [`src/services/`](frontend/src/services/) — Axios HTTP client & REST API endpoints
  - 📂 [`src/types/`](frontend/src/types/) — TypeScript models & DTOs
  - 📄 [`package.json`](frontend/package.json) — Frontend dependencies & scripts
  - 📄 [`vite.config.ts`](frontend/vite.config.ts) — Vite build and API reverse-proxy configuration
- 📦 [**`data-generator/`**](data-generator/) — Python synthetic dataset generator with anomaly injection
  - 📂 [`generator/`](data-generator/generator/) — DAG dependency generator & anomaly injector
  - 📂 [`output/`](data-generator/output/) — Generated benchmark datasets & signed manifest
- 📦 [**`docs/`**](docs/) — Architecture diagrams, database schemas, and API documentation
- 📄 [**`docker-compose.yml`**](docker-compose.yml) — PostgreSQL 17 database container
- 📄 [**`.env.example`**](.env.example) — Template environment variables

---

## Architecture

PerformIQ employs a **Modular Monolith** architecture organized package-by-feature under `com.performiq.*`. All domain packages communicate through service interfaces and domain repositories, ensuring low coupling and high cohesion without distributed microservice operational complexity.

---

## Core Business Logic

### 1. Key Result Metric Mathematics

Key Result progress is computed depending on the metric orientation and clamped to `[0, 100]%`:

- **INCREASE Metric** (e.g., test coverage from 60% to 90%):
  $$\text{Progress} = \left(\frac{V_{\text{current}} - V_{\text{start}}}{V_{\text{target}} - V_{\text{start}}}\right) \times 100$$

- **DECREASE Metric** (e.g., API latency from 250ms to 45ms):
  $$\text{Progress} = \left(\frac{V_{\text{start}} - V_{\text{current}}}{V_{\text{start}} - V_{\text{target}}}\right) \times 100$$

- **Cascade to Parent Goal**:
  $$\text{Goal Progress} = \frac{\sum_{i=1}^{n} (\text{Progress}_i \times \text{Weight}_i)}{\sum_{i=1}^{n} \text{Weight}_i}$$

### 2. Cycle Weight Constraint

The total weight of active goals for an employee within a review cycle must satisfy:

$$\sum_{j=1}^{m} \text{Goal Weight}_j \le 100.00\%$$

Attempts to exceed 100% are rejected with an explicit `BadRequestException`.

### 3. Review Scoring Blend

Official cycle scores combine OKR execution and competency feedback:

$$\text{Final Score} = \left(0.60 \times \text{Average Goal Score}\right) + \left(0.40 \times \text{Average 360 Feedback}\right)$$

---

## Testing & Quality Assurance

### Automated Backend Test Suites

```bash
cd backend
./mvnw test
```

- `BusinessLogicCalculationTest`: OKR math, zero-division safeguards, composite scoring formulas, and 360 feedback aggregation.
- `BusinessLogicIntegrationTest`: Database constraints, cycle weight bounds ($\le 100\%$), review cycle state transitions, and plan approval state machine.
- `RestApiSecurityAndEndpointsTest`: Authentication token issuance, RBAC endpoint protection, employee profile fetching, and API contracts.

**Result**: 15 / 15 tests passing (0 failures, 0 errors).

---

## Documentation

- [Database Schema & ER Topology](docs/database_schema.md)
- [Data Dictionary & Entity Models](docs/data_dictionary.md)
- [REST API Specification](docs/api_specification.md)
- [Core Business Logic Rules](docs/business_logic.md)
- [Synthetic Data Generation & Anomaly Model](docs/synthetic_data_generation.md)
- [Data Quality & Validation Engine](docs/data_validation.md)
- [Engineering Log & Decision Records](docs/prompt_history.md)

---

## License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.
