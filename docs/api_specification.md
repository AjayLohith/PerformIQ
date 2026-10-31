# PerformIQ — REST API Specification & Security Architecture

## 1. Executive Summary
This document specifies the RESTful API contracts, authentication mechanisms, role-based access control (RBAC), and OpenAPI documentation for **PerformIQ**. All endpoints adhere to standard HTTP verbs, status codes, standard validation constraints, and JSON request/response formats.

- **Base Context Path**: `/api/v1`
- **Interactive Documentation**: Swagger UI at `/api/v1/swagger-ui.html`
- **OpenAPI 3 Schema**: `/api/v1/v3/api-docs`

---

## 2. Authentication & RBAC Architecture

### 2.1 JSON Web Tokens (JWT)
PerformIQ implements stateless authentication using HMAC-SHA256 signed JSON Web Tokens (`io.jsonwebtoken` JJWT 0.13.0).
- **Header**: `Authorization: Bearer <token>`
- **Token Claims**:
  - `sub`: Username (e.g., `admin`, `alex.rivera`)
  - `role`: Role string (`ADMIN`, `MANAGER`, `EMPLOYEE`, `HR_PARTNER`, `OPERATIONS`)
  - `userId`: AppUser UUID
  - `employeeId`: Linked Employee UUID (if assigned)
  - `employeeBusinessId`: Linked Employee Business ID (e.g., `EMP-000001`)
  - `email`: Employee corporate email address
  - `exp`: Expiration timestamp (default: 24 hours / 86,400,000 ms)

### 2.2 Roles & Permissions Matrix
| Endpoint Pattern | Allowed Roles | Description |
| :--- | :--- | :--- |
| `POST /auth/login` | **Public** | Authenticate credentials and acquire JWT |
| `GET /auth/me` | **Authenticated** | Get current session user profile & employee details |
| `GET /employees/**` | **Authenticated** | Employee directory & manager hierarchy |
| `GET /review-cycles/**` | **Authenticated** | View active/scheduled review cycles |
| `POST /review-cycles/**` | `ADMIN` | Create review cycle |
| `PUT /review-cycles/{id}/transition` | `ADMIN` | Advance review cycle state machine |
| `/goals/**`, `/key-results/**`, `/check-ins/**` | **Authenticated** | Manage OKR goals, measurable targets, check-ins |
| `/feedbacks/**` | **Authenticated** | Request 360 feedback, submit evaluations, compute summaries |
| `POST /reviews/{id}/score` | `MANAGER`, `ADMIN` | Score review goals and finalize composite rating |
| `POST /development-plans/{id}/approve` | `MANAGER`, `ADMIN` | Approve employee development plan |
| `POST /development-plans/{id}/reject` | `MANAGER`, `ADMIN` | Reject development plan (mandatory coaching comments) |
| `/admin/**` | `ADMIN` | Dataset ingestion, anomaly audits, system audit logs |

### 2.3 Demo User Credentials (Pre-seeded)
| Username | Password | Role | Persona |
| :--- | :--- | :--- | :--- |
| `admin` | `password123` | `ADMIN` | Platform & HR Operations Administrator |
| `sarah.chen` | `password123` | `MANAGER` | VP of Engineering |
| `marcus.vance` | `password123` | `MANAGER` | Director of Engineering |
| `alex.rivera` | `password123` | `EMPLOYEE` | Senior Staff Software Engineer |
| `elena.rostova` | `password123` | `EMPLOYEE` | Staff Software Engineer |

---

## 3. Core REST API Endpoints

### 3.1 Authentication
- **`POST /api/v1/auth/login`**
  - **Request**: `{ "username": "admin", "password": "password123" }`
  - **Response (200 OK)**:
    ```json
    {
      "token": "eyJhbGciOiJIUzI1NiJ9...",
      "type": "Bearer",
      "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "username": "admin",
      "role": "ADMIN",
      "employee": {
        "id": "e9324d45-5601-4966-ba39-ffea0f51950e",
        "businessId": "EMP-000001",
        "firstName": "Sarah",
        "lastName": "Chen",
        "email": "sarah.chen@performiq.com",
        "department": "Engineering",
        "designation": "VP of Engineering"
      }
    }
    ```
- **`GET /api/v1/auth/me`**
  - Returns authenticated user details and linked employee record.

### 3.2 Employee Directory
- **`GET /api/v1/employees`**: Query employees with pagination (`page`, `size`, `sortBy`, `direction`) and filters (`department`, `status`).
- **`GET /api/v1/employees/{id}`**: Detailed profile including current manager assignment.
- **`GET /api/v1/employees/{id}/direct-reports`**: Direct reports reporting to this manager.

### 3.3 Review Cycles
- **`GET /api/v1/review-cycles`**: List all cycles.
- **`GET /api/v1/review-cycles/active`**: Currently active cycle.
- **`POST /api/v1/review-cycles`** *(Admin)*: Create new review cycle.
- **`PUT /api/v1/review-cycles/{id}/transition`** *(Admin)*: Advance state machine (`DRAFT -> ACTIVE -> UNDER_REVIEW -> CLOSED`).

### 3.4 Goals, Key Results & Check-Ins
- **`GET /api/v1/goals?employeeId={id}&reviewCycleId={id}`**: Retrieve employee goals for a cycle.
- **`GET /api/v1/goals/{id}`**: Detailed goal view with child key results and chronological check-ins.
- **`POST /api/v1/goals`**: Create goal (validates weight sum $\le 100\%$).
- **`PATCH /api/v1/goals/{id}/status`**: Transition goal status (`DRAFT -> ACTIVE -> COMPLETED / CANCELLED`).
- **`PATCH /api/v1/goals/{id}/progress`**: Update direct goal progress.
- **`POST /api/v1/key-results`**: Attach measurable key result with `INCREASE` or `DECREASE` metric.
- **`PATCH /api/v1/key-results/{id}/value`**: Update current value; triggers formula calculation and cascades progress to parent goal.
- **`POST /api/v1/check-ins`**: Record periodic check-in with snapshot progress and attached evidence links.
- **`GET /api/v1/check-ins/goal/{goalId}`**: Chronological check-in history.

### 3.5 360° Feedback & Competencies
- **`POST /api/v1/feedbacks/request`**: Request evaluation (`SELF`, `MANAGER`, `PEER`, `UPWARD`).
- **`POST /api/v1/feedbacks/{id}/submit`**: Submit rating (1-5), comments, and per-competency rubric scores.
- **`GET /api/v1/feedbacks/employee/{employeeId}/cycle/{cycleId}`**: All feedback for an employee in a cycle.
- **`GET /api/v1/feedbacks/summary?employeeId={id}&reviewCycleId={id}`**: Multi-dimensional summary (average overall rating, by feedback type, by competency).
- **`GET /api/v1/feedbacks/competencies`**: List standard organizational competencies.

### 3.6 Performance Reviews
- **`GET /api/v1/reviews/cycle/{cycleId}`**: List reviews in cycle.
- **`GET /api/v1/reviews/{id}`**: Full review details including goal evaluations.
- **`POST /api/v1/reviews/{id}/score`** *(Manager/Admin)*: Score goals (1-5) and seal review with composite rating ($60\%$ Goal Score $+ 40\%$ Feedback Score).

### 3.7 Development Plans & Approvals
- **`GET /api/v1/development-plans/employee/{employeeId}`**: Growth plans for employee.
- **`GET /api/v1/development-plans/{id}`**: Plan details with approval history trail.
- **`POST /api/v1/development-plans`**: Create plan in `DRAFT`.
- **`POST /api/v1/development-plans/{id}/submit`**: Submit for manager approval.
- **`POST /api/v1/development-plans/{id}/approve`** *(Manager/Admin)*: Approve plan.
- **`POST /api/v1/development-plans/{id}/reject`** *(Manager/Admin)*: Reject plan with mandatory coaching reason.
- **`POST /api/v1/development-plans/{id}/start`**: Begin execution (`APPROVED -> IN_PROGRESS`).
- **`POST /api/v1/development-plans/{id}/complete`**: Mark plan as completed.

### 3.8 Administration & Auditing
- **`POST /api/v1/admin/import`**: Batch dataset ingestion with topological DAG and SHA-256 manifest verification.
- **`GET /api/v1/admin/validation/report`**: Run data quality rule checks and view anomaly summary.
- **`GET /api/v1/admin/audit-logs`**: Paginated security and mutation audit trail.
