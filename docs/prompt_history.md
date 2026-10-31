# PerformIQ — Engineering Log & Architecture Decision Records

This document tracks the phased development lifecycle of **PerformIQ**, architectural decisions, and selective AI-assisted consultations on complex mathematical and topological algorithms.

---

## Phase 0 — Requirements Analysis & System Design
- **Date**: 2026-10-30
- **Author**: Engineering Lead
- **Decisions & Scope**:
  - Selected Modular Monolith architecture in Spring Boot 4 / Java 21 to avoid distributed microservice network overhead while maintaining strict package-by-feature domain boundaries.
  - Chose PostgreSQL 17 with Flyway versioned migrations to guarantee relational database integrity and schema reproducibility across environments.
  - Defined deterministic business identifier strategy (`EMP-000001`, `CYCLE-2026-H1`, `GOAL-000001`) separate from internal UUID primary keys.
- **Artifacts**: Initial architectural blueprint, entity relationship topology, and 10-phase delivery roadmap.
- **Status**: COMPLETED

---

## Phase 1 — Project Scaffolding & Foundation
- **Date**: 2026-10-30
- **Author**: Engineering Lead
- **Decisions & Scope**:
  - Flattened backend project structure under `backend/` with Maven wrapper and Java 21 toolchain.
  - Scaffolded containerized environment via `docker-compose.yml` for PostgreSQL 17.
  - Initialized modern frontend under `frontend/` using Vite 6, React 19, TypeScript, and Tailwind CSS 4.
  - Set up Python 3.12 virtual environment for synthetic data generation tooling.
- **Status**: COMPLETED

---

## Phase 2 — Relational Schema & JPA Persistence
- **Date**: 2026-10-30
- **Author**: Engineering Lead
- **Decisions & Scope**:
  - Implemented 21 relational tables in versioned Flyway migrations (`V2__initial_schema.sql` and `V3__seed_data.sql`).
  - Enforced DB-level foreign key cascades, unique business constraints, and composite indexes on hot query paths (e.g., `(employee_id, review_cycle_id)`).
  - Mapped JPA entities with Hibernate 7, establishing lazy-loading semantics and clean detached entity serialization.
  - Authored domain repositories with custom JPQL queries and pagination support.
- **Artifacts**: `docs/database_schema.md` and `docs/data_dictionary.md`.
- **Status**: COMPLETED

---

## Phase 3 — Synthetic Data Generation & Topological Graph Ordering
- **Date**: 2026-10-30
- **Author**: Engineering Lead (with LLM algorithmic consultation)
- **Consultation Topic**:
  - Designing a topological Directed Acyclic Graph (DAG) dependency resolver in Python Faker to generate parent-child hierarchies in valid relational sequence without foreign key violations.
  - Formulating 10 calibrated anomaly injection rules (`CYCLE_WEIGHT_OVERFLOW`, `SELF_FEEDBACK_AS_PEER`, `CIRCULAR_MANAGER_HIERARCHY`, etc.) with cryptographically verifiable SHA-256 manifest signing.
- **Output**:
  - `data-generator/generator/` suite generating 7,489 benchmark records with deterministic seed reproducibility.
  - `manifest.json` with SHA-256 hash validation for batch auditing.
- **Status**: COMPLETED

---

## Phase 4 — Batch Ingestion & Data Validation Engine
- **Date**: 2026-10-30
- **Author**: Engineering Lead
- **Decisions & Scope**:
  - Implemented `DataImportService` in Spring Boot featuring streaming CSV parsing and cache-assisted entity resolution to prevent $O(N^2)$ lookup overhead.
  - Built `ValidationService` to execute multi-point business integrity audits: detecting circular manager hierarchies, boundary violations, and orphan records.
  - Exposed secured administrative endpoints (`/api/v1/admin/import`, `/api/v1/admin/validation/report`).
- **Artifacts**: `docs/data_validation.md`.
- **Status**: COMPLETED

---

## Phase 5 — Core Business Logic & Metric Mathematics
- **Date**: 2026-10-30
- **Author**: Engineering Lead (with LLM algorithmic consultation)
- **Consultation Topic**:
  - Verification of mathematical formulas for Key Result progress tracking with dual metric orientations:
    - **INCREASE**: $P = \frac{V_{\text{current}} - V_{\text{start}}}{V_{\text{target}} - V_{\text{start}}} \times 100$
    - **DECREASE**: $P = \frac{V_{\text{start}} - V_{\text{current}}}{V_{\text{start}} - V_{\text{target}}} \times 100$
  - Edge-case proofs for zero-division safeguards when $V_{\text{start}} = V_{\text{target}}$, scale rounding, and $[0, 100]\%$ boundary clamping.
  - Multi-tiered review cycle state machine (`DRAFT -> ACTIVE -> UNDER_REVIEW -> CLOSED`) and plan approval transitions.
- **Output**:
  - `KeyResultService`, `GoalService`, `CheckInService`, `ReviewCycleService`, `FeedbackService`, `ReviewService`, `DevelopmentPlanService`.
  - Comprehensive unit test suite `BusinessLogicCalculationTest` and integration suite `BusinessLogicIntegrationTest` (11/11 tests passing).
- **Artifacts**: `docs/business_logic.md`.
- **Status**: COMPLETED

---

## Phase 6 — REST API Architecture & Stateless JWT Security
- **Date**: 2026-10-30
- **Author**: Engineering Lead
- **Decisions & Scope**:
  - Configured Spring Security with stateless session management and custom `JwtAuthenticationFilter` verifying HMAC-SHA256 bearer tokens.
  - Implemented Role-Based Access Control (RBAC) across `ADMIN`, `MANAGER`, and `EMPLOYEE` tiers.
  - Configured SpringDoc OpenAPI 3 documentation with Bearer authentication scheme.
  - Built RESTful controllers with structured error envelopes (`GlobalExceptionHandler`) and RFC-7807 problem details.
  - Authored automated integration suite `RestApiSecurityAndEndpointsTest` (15/15 tests passing).
- **Artifacts**: `docs/api_specification.md`.
- **Status**: COMPLETED

---

## Phase 7 — Enterprise Frontend Implementation
- **Date**: 2026-10-30
- **Author**: Engineering Lead
- **Decisions & Scope**:
  - Built 12 responsive enterprise views in React 19 and Tailwind CSS 4 with zero generic template feel.
  - Implemented interactive Key Result sliders with instantaneous client-side preview and real-time parent goal cascade.
  - Built visual state machine pipelines for review cycle control and multi-step plan approvals with mandatory rejection coaching logs.
  - Implemented tamper-evident audit viewer with JSON state diff inspector (`previousValue` vs. `newValue`).
  - Added quick demo persona switching for seamless role demonstration (`admin`, `sarah.chen`, `marcus.vance`, `elena.rostova`, `alex.rivera`).
- **Build Verification**: Clean production build via `npm run build` (2,181 modules transformed in 3.89s, 0 errors).
- **Status**: COMPLETED

---

## Phase 8 — End-to-End System Verification
- **Date**: 2026-10-30
- **Author**: Engineering Lead
- **Decisions & Scope**:
  - Designed zero-dependency `local` dev profile using in-memory H2 in PostgreSQL compatibility mode with automatic entity bootstrap (`LocalProfileDataInitializer.java`).
  - Executed automated PowerShell end-to-end lifecycle verification suite testing authentication, OKR progress mutation, check-in recording, manager approvals, and audit trail generation (100% pass rate).
- **Status**: COMPLETED

---

## Phase 9 — Documentation & Release Packaging
- **Date**: 2026-10-30
- **Author**: Engineering Lead
- **Decisions & Scope**:
  - Prepared production-ready `README.md` with system architecture diagrams, mathematical proofs, evaluation credentials, and Docker / zero-Docker quick starts.
  - Cleaned repository build artifacts, temporary scratch scripts, and verified clean repository state ready for version control.
- **Status**: COMPLETED
