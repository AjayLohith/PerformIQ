# PerformIQ — Data Dictionary

## 1. Table Definitions

### 1.1 `employees`
Core table storing employee directory and personnel records.
| Column | Type | Nullable | Default | Description |
|--------|------|----------|---------|-------------|
| `id` | UUID | NO | gen_random_uuid() | Primary key |
| `business_id` | VARCHAR(32) | NO | - | Unique business ID (`EMP-000001`) |
| `first_name` | VARCHAR(100) | NO | - | Legal first name |
| `last_name` | VARCHAR(100) | NO | - | Legal last name |
| `email` | VARCHAR(255) | NO | - | Unique corporate email address |
| `department` | VARCHAR(100) | NO | - | Department / division |
| `designation` | VARCHAR(100) | NO | - | Job title / rank |
| `joining_date` | DATE | NO | - | Date of joining |
| `status` | VARCHAR(32) | NO | 'ACTIVE' | Lifecycle state: `ACTIVE`, `INACTIVE`, `TERMINATED` |
| `created_at` | TIMESTAMPTZ | NO | CURRENT_TIMESTAMP | Audit creation timestamp |
| `updated_at` | TIMESTAMPTZ | NO | CURRENT_TIMESTAMP | Audit modification timestamp |

---

### 1.2 `managers`
Manager role records mapped 1:1 to qualifying employees.
| Column | Type | Nullable | Default | Description |
|--------|------|----------|---------|-------------|
| `id` | UUID | NO | gen_random_uuid() | Primary key |
| `business_id` | VARCHAR(32) | NO | - | Unique business ID (`MGR-000001`) |
| `employee_id` | UUID | NO | - | FK to `employees.id` (Unique) |
| `effective_from`| DATE | NO | - | Start date of management role |
| `effective_to` | DATE | YES | NULL | End date of management role (NULL = active) |
| `status` | VARCHAR(32) | NO | 'ACTIVE' | `ACTIVE`, `INACTIVE` |
| `created_at` | TIMESTAMPTZ | NO | CURRENT_TIMESTAMP | Record creation timestamp |
| `updated_at` | TIMESTAMPTZ | NO | CURRENT_TIMESTAMP | Record update timestamp |

---

### 1.3 `manager_assignment_history`
Temporal tracking of employee-to-manager supervisory relationships.
| Column | Type | Nullable | Default | Description |
|--------|------|----------|---------|-------------|
| `id` | UUID | NO | gen_random_uuid() | Primary key |
| `employee_id` | UUID | NO | - | FK to `employees.id` (Direct report) |
| `manager_id` | UUID | NO | - | FK to `managers.id` (Assigned manager) |
| `effective_from`| DATE | NO | - | Assignment start date |
| `effective_to` | DATE | YES | NULL | Assignment end date (NULL = current) |
| `reason` | VARCHAR(255) | YES | NULL | Reason for assignment or transfer |
| `created_at` | TIMESTAMPTZ | NO | CURRENT_TIMESTAMP | Record creation timestamp |

---

### 1.4 `review_cycles`
Performance evaluation periods governing review timelines.
| Column | Type | Nullable | Default | Description |
|--------|------|----------|---------|-------------|
| `id` | UUID | NO | gen_random_uuid() | Primary key |
| `business_id` | VARCHAR(64) | NO | - | Unique business ID (`CYCLE-2026-H1`) |
| `name` | VARCHAR(255) | NO | - | Descriptive cycle name |
| `description` | TEXT | YES | NULL | Scope and guidelines |
| `start_date` | DATE | NO | - | Cycle start date |
| `end_date` | DATE | NO | - | Cycle end date |
| `status` | VARCHAR(32) | NO | 'DRAFT' | `DRAFT`, `ACTIVE`, `UNDER_REVIEW`, `CLOSED` |
| `version` | INT | NO | 0 | Optimistic concurrency version |
| `created_at` | TIMESTAMPTZ | NO | CURRENT_TIMESTAMP | Record creation timestamp |
| `updated_at` | TIMESTAMPTZ | NO | CURRENT_TIMESTAMP | Record update timestamp |

---

### 1.5 `goals`
Objectives defined by employees within a review cycle.
| Column | Type | Nullable | Default | Description |
|--------|------|----------|---------|-------------|
| `id` | UUID | NO | gen_random_uuid() | Primary key |
| `business_id` | VARCHAR(64) | NO | - | Unique business ID (`GOAL-EMP-000001-001`) |
| `employee_id` | UUID | NO | - | FK to `employees.id` (Owner) |
| `review_cycle_id`| UUID | NO | - | FK to `review_cycles.id` |
| `title` | VARCHAR(255) | NO | - | Goal title |
| `description` | TEXT | YES | NULL | Detailed description |
| `category` | VARCHAR(64) | NO | 'INDIVIDUAL' | Category (Individual, Team, Company) |
| `status` | VARCHAR(32) | NO | 'DRAFT' | `DRAFT`, `ACTIVE`, `COMPLETED`, `CANCELLED` |
| `weight` | NUMERIC(5,2)| NO | 1.00 | Goal weight relative to other goals |
| `progress` | NUMERIC(5,2)| NO | 0.00 | Derived progress % (0 - 100) from key results |
| `created_at` | TIMESTAMPTZ | NO | CURRENT_TIMESTAMP | Record creation timestamp |
| `updated_at` | TIMESTAMPTZ | NO | CURRENT_TIMESTAMP | Record update timestamp |

---

### 1.6 `key_results`
Measurable quantitative outcomes defining a goal's progress.
| Column | Type | Nullable | Default | Description |
|--------|------|----------|---------|-------------|
| `id` | UUID | NO | gen_random_uuid() | Primary key |
| `business_id` | VARCHAR(64) | NO | - | Unique business ID (`KR-GOAL-EMP-000001-001-01`) |
| `goal_id` | UUID | NO | - | FK to `goals.id` |
| `title` | VARCHAR(255) | NO | - | Key result description |
| `metric_type` | VARCHAR(32) | NO | - | `INCREASE`, `DECREASE` |
| `target_value` | NUMERIC(12,2)| NO| - | Target metric value |
| `current_value`| NUMERIC(12,2)| NO| - | Current achieved value |
| `start_value` | NUMERIC(12,2)| NO| - | Baseline starting value |
| `weight` | NUMERIC(5,2)| NO | 1.00 | Relative weight within the goal |
| `unit` | VARCHAR(32) | NO | '%' | Metric unit (%, ms, USD, count) |
| `progress` | NUMERIC(5,2)| NO | 0.00 | Calculated progress % (0 - 100) |
| `created_at` | TIMESTAMPTZ | NO | CURRENT_TIMESTAMP | Record creation timestamp |
| `updated_at` | TIMESTAMPTZ | NO | CURRENT_TIMESTAMP | Record update timestamp |

---

### 1.7 `check_ins`
Periodic status check-ins tracking progress towards goals.
| Column | Type | Nullable | Default | Description |
|--------|------|----------|---------|-------------|
| `id` | UUID | NO | gen_random_uuid() | Primary key |
| `business_id` | VARCHAR(64) | NO | - | Unique business ID (`CHECKIN-000001`) |
| `goal_id` | UUID | NO | - | FK to `goals.id` |
| `employee_id` | UUID | NO | - | FK to `employees.id` |
| `notes` | TEXT | NO | - | Check-in narrative and blockers |
| `progress_snapshot`| NUMERIC(5,2)| NO | - | Snapshot of goal progress % at check-in time |
| `check_in_date`| DATE | NO | - | Date check-in took place |
| `created_at` | TIMESTAMPTZ | NO | CURRENT_TIMESTAMP | Check-in submission timestamp |

---

### 1.8 `evidence`
Artifacts and proof documents supporting a check-in.
| Column | Type | Nullable | Default | Description |
|--------|------|----------|---------|-------------|
| `id` | UUID | NO | gen_random_uuid() | Primary key |
| `business_id` | VARCHAR(64) | NO | - | Unique business ID (`EVID-000001`) |
| `check_in_id` | UUID | NO | - | FK to `check_ins.id` |
| `title` | VARCHAR(255) | NO | - | Artifact title |
| `url` | TEXT | YES | NULL | Link to document or dashboard |
| `description` | TEXT | YES | NULL | Context or summary of proof |
| `evidence_type`| VARCHAR(32) | NO | 'URL' | `URL`, `DOCUMENT`, `METRIC` |
| `created_at` | TIMESTAMPTZ | NO | CURRENT_TIMESTAMP | Evidence attachment timestamp |

---

### 1.9 `competencies`
Organizational competency rubric catalog.
| Column | Type | Nullable | Default | Description |
|--------|------|----------|---------|-------------|
| `id` | UUID | NO | gen_random_uuid() | Primary key |
| `business_id` | VARCHAR(32) | NO | - | Unique business ID (`COMP-000001`) |
| `name` | VARCHAR(100) | NO | - | Competency name (e.g. Leadership & Influence) |
| `description` | TEXT | YES | NULL | Behavioral rubric description |
| `category` | VARCHAR(64) | NO | 'CORE' | Category (Core, Technical, Leadership, etc.) |
| `created_at` | TIMESTAMPTZ | NO | CURRENT_TIMESTAMP | Creation timestamp |

---

### 1.10 `feedbacks`
Multi-rater feedback submissions (Self, Manager, Peer).
| Column | Type | Nullable | Default | Description |
|--------|------|----------|---------|-------------|
| `id` | UUID | NO | gen_random_uuid() | Primary key |
| `business_id` | VARCHAR(64) | NO | - | Unique business ID (`FEEDBACK-000001`) |
| `employee_id` | UUID | NO | - | FK to `employees.id` (Subject of feedback) |
| `reviewer_id` | UUID | NO | - | FK to `employees.id` (Author of feedback) |
| `review_cycle_id`| UUID | NO | - | FK to `review_cycles.id` |
| `feedback_type`| VARCHAR(32) | NO | - | `SELF`, `MANAGER`, `PEER` |
| `comments` | TEXT | YES | NULL | Qualitative qualitative observations |
| `overall_rating`| INT | YES | NULL | Scale 1 to 5 |
| `status` | VARCHAR(32) | NO | 'DRAFT' | `DRAFT`, `SUBMITTED` |
| `submitted_at` | TIMESTAMPTZ | YES | NULL | Submission timestamp |
| `created_at` | TIMESTAMPTZ | NO | CURRENT_TIMESTAMP | Creation timestamp |

---

### 1.11 `feedback_competencies`
Bridge table capturing competency-specific ratings within feedback.
| Column | Type | Nullable | Default | Description |
|--------|------|----------|---------|-------------|
| `id` | UUID | NO | gen_random_uuid() | Primary key |
| `feedback_id` | UUID | NO | - | FK to `feedbacks.id` |
| `competency_id`| UUID | NO | - | FK to `competencies.id` |
| `rating` | INT | NO | - | Rating (1 - 5) |
| `comments` | TEXT | YES | NULL | Specific competency behavioral feedback |

---

### 1.12 `reviews`
Formal performance review records.
| Column | Type | Nullable | Default | Description |
|--------|------|----------|---------|-------------|
| `id` | UUID | NO | gen_random_uuid() | Primary key |
| `business_id` | VARCHAR(64) | NO | - | Unique business ID (`REV-2026-000001`) |
| `employee_id` | UUID | NO | - | FK to `employees.id` |
| `reviewer_id` | UUID | NO | - | FK to `employees.id` (Manager/HR) |
| `review_cycle_id`| UUID | NO | - | FK to `review_cycles.id` |
| `status` | VARCHAR(32) | NO | 'PENDING' | `PENDING`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED` |
| `overall_score`| NUMERIC(5,2)| YES| NULL | Final performance rating score (0 - 100) |
| `summary` | TEXT | YES | NULL | Executive performance summary |
| `recommendation`| TEXT | YES | NULL | Promotion or compensation recommendations |
| `completed_at` | TIMESTAMPTZ | YES | NULL | Completion timestamp |
| `created_at` | TIMESTAMPTZ | NO | CURRENT_TIMESTAMP | Creation timestamp |
| `updated_at` | TIMESTAMPTZ | NO | CURRENT_TIMESTAMP | Update timestamp |

---

### 1.13 `review_goals`
Bridge table linking performance reviews to goals with individual scores.
| Column | Type | Nullable | Default | Description |
|--------|------|----------|---------|-------------|
| `id` | UUID | NO | gen_random_uuid() | Primary key |
| `review_id` | UUID | NO | - | FK to `reviews.id` |
| `goal_id` | UUID | NO | - | FK to `goals.id` |
| `goal_score` | NUMERIC(5,2)| YES| NULL | Goal evaluation score (0 - 100) |
| `comments` | TEXT | YES | NULL | Evaluator comments for the goal |

---

### 1.14 `development_plans`
Post-review growth plans and target developmental competencies.
| Column | Type | Nullable | Default | Description |
|--------|------|----------|---------|-------------|
| `id` | UUID | NO | gen_random_uuid() | Primary key |
| `business_id` | VARCHAR(64) | NO | - | Unique business ID (`DEVP-000001`) |
| `employee_id` | UUID | NO | - | FK to `employees.id` |
| `review_id` | UUID | YES | NULL | FK to `reviews.id` (Optional) |
| `title` | VARCHAR(255) | NO | - | Growth plan title |
| `description` | TEXT | YES | NULL | Plan summary |
| `objectives` | TEXT | YES | NULL | Developmental milestones |
| `skills_to_improve`| TEXT | YES | NULL | Specific skills and focus areas |
| `action_items` | TEXT | YES | NULL | Concrete steps to execute |
| `target_date` | DATE | YES | NULL | Projected completion date |
| `status` | VARCHAR(32) | NO | 'DRAFT' | `DRAFT`, `SUBMITTED`, `APPROVED`, `REJECTED`, `IN_PROGRESS`, `COMPLETED` |
| `created_at` | TIMESTAMPTZ | NO | CURRENT_TIMESTAMP | Creation timestamp |
| `updated_at` | TIMESTAMPTZ | NO | CURRENT_TIMESTAMP | Update timestamp |

---

### 1.15 `approval_history`
Audit trail of approval decisions on development plans.
| Column | Type | Nullable | Default | Description |
|--------|------|----------|---------|-------------|
| `id` | UUID | NO | gen_random_uuid() | Primary key |
| `development_plan_id`| UUID | NO| - | FK to `development_plans.id` |
| `approver_id` | UUID | NO | - | FK to `employees.id` |
| `action` | VARCHAR(32) | NO | - | `SUBMITTED`, `APPROVED`, `REJECTED` |
| `comments` | TEXT | YES | NULL | Approval or rejection notes |
| `action_date` | TIMESTAMPTZ | NO | CURRENT_TIMESTAMP | Timestamp of action |

---

### 1.16 `audit_logs`
System-wide audit trail for critical security and operational events.
| Column | Type | Nullable | Default | Description |
|--------|------|----------|---------|-------------|
| `id` | UUID | NO | gen_random_uuid() | Primary key |
| `actor_id` | UUID | YES | NULL | FK to `employees.id` (Actor who performed action) |
| `action` | VARCHAR(64) | NO | - | Action name (e.g., `CYCLE_STATUS_CHANGE`) |
| `entity_type` | VARCHAR(64) | NO | - | Target entity name (e.g., `REVIEW_CYCLE`) |
| `entity_id` | VARCHAR(64) | NO | - | Target entity identifier |
| `previous_value`| TEXT | YES | NULL | Serialized JSON of previous state |
| `new_value` | TEXT | YES | NULL | Serialized JSON of new state |
| `reason` | VARCHAR(255)| YES | NULL | Explicit reason for change |
| `created_at` | TIMESTAMPTZ | NO | CURRENT_TIMESTAMP | Event timestamp |

---

### 1.17 `anomaly_cases`
Data quality anomalies and benchmark cases tracked by the validation engine.
| Column | Type | Nullable | Default | Description |
|--------|------|----------|---------|-------------|
| `id` | UUID | NO | gen_random_uuid() | Primary key |
| `business_id` | VARCHAR(64) | NO | - | Unique business ID (`ANOM-000001`) |
| `issue_type` | VARCHAR(100)| NO | - | Anomaly category |
| `affected_entity`| VARCHAR(64)| NO| - | Entity type (e.g., `GOAL`, `CHECK_IN`) |
| `affected_business_id`| VARCHAR(64)| NO| - | Target business ID |
| `severity` | VARCHAR(32) | NO | 'MEDIUM' | `LOW`, `MEDIUM`, `HIGH`, `CRITICAL` |
| `expected_outcome`| TEXT | YES | NULL | Benchmark expected validation result |
| `actual_outcome`| TEXT | YES | NULL | Actual result found during validation |
| `detection_status`| VARCHAR(32)| NO| 'DETECTED' | `DETECTED`, `UNDETECTED`, `FALSE_POSITIVE` |
| `created_at` | TIMESTAMPTZ | NO | CURRENT_TIMESTAMP | Detection timestamp |

---

### 1.18 `goal_history`
Field-level mutation history for goals.
| Column | Type | Nullable | Default | Description |
|--------|------|----------|---------|-------------|
| `id` | UUID | NO | gen_random_uuid() | Primary key |
| `goal_id` | UUID | NO | - | FK to `goals.id` |
| `field_changed`| VARCHAR(64) | NO | - | Changed property name |
| `old_value` | TEXT | YES | NULL | Previous value |
| `new_value` | TEXT | YES | NULL | Updated value |
| `changed_by` | UUID | YES | NULL | FK to `employees.id` |
| `changed_at` | TIMESTAMPTZ | NO | CURRENT_TIMESTAMP | Mutation timestamp |

---

### 1.19 `key_result_history`
Numeric history tracking metric changes for key results.
| Column | Type | Nullable | Default | Description |
|--------|------|----------|---------|-------------|
| `id` | UUID | NO | gen_random_uuid() | Primary key |
| `key_result_id`| UUID | NO | - | FK to `key_results.id` |
| `old_value` | NUMERIC(12,2)| NO| - | Metric value prior to update |
| `new_value` | NUMERIC(12,2)| NO| - | Metric value after update |
| `changed_by` | UUID | YES | NULL | FK to `employees.id` |
| `changed_at` | TIMESTAMPTZ | NO | CURRENT_TIMESTAMP | Metric update timestamp |

---

### 1.20 `data_import_runs`
Synthetic and batch dataset import execution log.
| Column | Type | Nullable | Default | Description |
|--------|------|----------|---------|-------------|
| `id` | UUID | NO | gen_random_uuid() | Primary key |
| `seed` | VARCHAR(64) | NO | - | Deterministic random seed string |
| `scale` | VARCHAR(32) | NO | - | `STARTER` or `FULL` |
| `generator_version`| VARCHAR(32)| NO| - | Generator release version tag |
| `status` | VARCHAR(32) | NO | 'PENDING' | `PENDING`, `VALIDATING`, `IMPORTING`, `COMPLETED`, `FAILED` |
| `total_records`| INT | NO | 0 | Total entity records in import bundle |
| `valid_records`| INT | NO | 0 | Count of records successfully persisted |
| `invalid_records`| INT | NO| 0 | Count of records flagged or rejected |
| `manifest_checksum`| VARCHAR(128)| YES| NULL | SHA-256 hash of import manifest |
| `started_at` | TIMESTAMPTZ | NO | CURRENT_TIMESTAMP | Import start timestamp |
| `completed_at` | TIMESTAMPTZ | YES | NULL | Import completion timestamp |

---

### 1.21 `app_users`
Authentication credentials and Role-Based Access Control (RBAC).
| Column | Type | Nullable | Default | Description |
|--------|------|----------|---------|-------------|
| `id` | UUID | NO | gen_random_uuid() | Primary key |
| `employee_id` | UUID | YES | NULL | Optional FK to `employees.id` |
| `username` | VARCHAR(100)| NO | - | Unique login email / username |
| `password_hash`| VARCHAR(255)| NO | - | BCrypt hashed password |
| `role` | VARCHAR(32) | NO | - | `EMPLOYEE`, `MANAGER`, `HR_PARTNER`, `OPERATIONS`, `ADMIN` |
| `enabled` | BOOLEAN | NO | TRUE | Account active flag |
| `created_at` | TIMESTAMPTZ | NO | CURRENT_TIMESTAMP | Account creation timestamp |
