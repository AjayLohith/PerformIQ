-- ============================================================================
-- PerformIQ - Phase 2 Initial Database Schema
-- V2__initial_schema.sql
-- Complete relational schema for all 21 entities
-- PostgreSQL 17 compliant
-- ============================================================================

-- Ensure pgcrypto extension is available for UUID generation
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================================================
-- 1. EMPLOYEES
-- ============================================================================
CREATE TABLE employees (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_id VARCHAR(32) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    department VARCHAR(100) NOT NULL,
    designation VARCHAR(100) NOT NULL,
    joining_date DATE NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'TERMINATED')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_employees_business_id ON employees(business_id);
CREATE INDEX idx_employees_email ON employees(email);
CREATE INDEX idx_employees_department ON employees(department);
CREATE INDEX idx_employees_status ON employees(status);

-- ============================================================================
-- 2. MANAGERS
-- ============================================================================
CREATE TABLE managers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_id VARCHAR(32) NOT NULL UNIQUE,
    employee_id UUID NOT NULL UNIQUE REFERENCES employees(id) ON DELETE RESTRICT,
    effective_from DATE NOT NULL,
    effective_to DATE,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_managers_business_id ON managers(business_id);
CREATE INDEX idx_managers_employee_id ON managers(employee_id);
CREATE INDEX idx_managers_status ON managers(status);

-- ============================================================================
-- 3. MANAGER_ASSIGNMENT_HISTORY
-- ============================================================================
CREATE TABLE manager_assignment_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id UUID NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    manager_id UUID NOT NULL REFERENCES managers(id) ON DELETE RESTRICT,
    effective_from DATE NOT NULL,
    effective_to DATE,
    reason VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_mgr_history_employee ON manager_assignment_history(employee_id);
CREATE INDEX idx_mgr_history_manager ON manager_assignment_history(manager_id);
CREATE INDEX idx_mgr_history_dates ON manager_assignment_history(effective_from, effective_to);

-- ============================================================================
-- 4. REVIEW_CYCLES
-- ============================================================================
CREATE TABLE review_cycles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_id VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'ACTIVE', 'UNDER_REVIEW', 'CLOSED')),
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_review_cycle_dates CHECK (end_date >= start_date)
);

CREATE INDEX idx_review_cycles_business_id ON review_cycles(business_id);
CREATE INDEX idx_review_cycles_status ON review_cycles(status);
CREATE INDEX idx_review_cycles_dates ON review_cycles(start_date, end_date);

-- ============================================================================
-- 5. GOALS
-- ============================================================================
CREATE TABLE goals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_id VARCHAR(64) NOT NULL UNIQUE,
    employee_id UUID NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    review_cycle_id UUID NOT NULL REFERENCES review_cycles(id) ON DELETE RESTRICT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(64) NOT NULL DEFAULT 'INDIVIDUAL',
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'ACTIVE', 'COMPLETED', 'CANCELLED')),
    weight NUMERIC(5, 2) NOT NULL DEFAULT 1.00 CHECK (weight >= 0),
    progress NUMERIC(5, 2) NOT NULL DEFAULT 0.00 CHECK (progress >= 0 AND progress <= 100),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_goals_business_id ON goals(business_id);
CREATE INDEX idx_goals_employee ON goals(employee_id);
CREATE INDEX idx_goals_review_cycle ON goals(review_cycle_id);
CREATE INDEX idx_goals_status ON goals(status);

-- ============================================================================
-- 6. KEY_RESULTS
-- ============================================================================
CREATE TABLE key_results (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_id VARCHAR(64) NOT NULL UNIQUE,
    goal_id UUID NOT NULL REFERENCES goals(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    metric_type VARCHAR(32) NOT NULL CHECK (metric_type IN ('INCREASE', 'DECREASE')),
    target_value NUMERIC(12, 2) NOT NULL,
    current_value NUMERIC(12, 2) NOT NULL,
    start_value NUMERIC(12, 2) NOT NULL,
    weight NUMERIC(5, 2) NOT NULL DEFAULT 1.00 CHECK (weight > 0),
    unit VARCHAR(32) NOT NULL DEFAULT '%',
    progress NUMERIC(5, 2) NOT NULL DEFAULT 0.00 CHECK (progress >= 0 AND progress <= 100),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_key_results_business_id ON key_results(business_id);
CREATE INDEX idx_key_results_goal ON key_results(goal_id);

-- ============================================================================
-- 7. CHECK_INS
-- ============================================================================
CREATE TABLE check_ins (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_id VARCHAR(64) NOT NULL UNIQUE,
    goal_id UUID NOT NULL REFERENCES goals(id) ON DELETE CASCADE,
    employee_id UUID NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    notes TEXT NOT NULL,
    progress_snapshot NUMERIC(5, 2) NOT NULL CHECK (progress_snapshot >= 0 AND progress_snapshot <= 100),
    check_in_date DATE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_check_ins_business_id ON check_ins(business_id);
CREATE INDEX idx_check_ins_goal ON check_ins(goal_id);
CREATE INDEX idx_check_ins_employee ON check_ins(employee_id);
CREATE INDEX idx_check_ins_date ON check_ins(check_in_date);

-- ============================================================================
-- 8. EVIDENCE
-- ============================================================================
CREATE TABLE evidence (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_id VARCHAR(64) NOT NULL UNIQUE,
    check_in_id UUID NOT NULL REFERENCES check_ins(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    url TEXT,
    description TEXT,
    evidence_type VARCHAR(32) NOT NULL DEFAULT 'URL' CHECK (evidence_type IN ('URL', 'DOCUMENT', 'METRIC')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_evidence_business_id ON evidence(business_id);
CREATE INDEX idx_evidence_check_in ON evidence(check_in_id);

-- ============================================================================
-- 9. COMPETENCIES
-- ============================================================================
CREATE TABLE competencies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_id VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    category VARCHAR(64) NOT NULL DEFAULT 'CORE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_competencies_business_id ON competencies(business_id);
CREATE INDEX idx_competencies_category ON competencies(category);

-- ============================================================================
-- 10. FEEDBACK
-- ============================================================================
CREATE TABLE feedbacks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_id VARCHAR(64) NOT NULL UNIQUE,
    employee_id UUID NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    reviewer_id UUID NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    review_cycle_id UUID NOT NULL REFERENCES review_cycles(id) ON DELETE RESTRICT,
    feedback_type VARCHAR(32) NOT NULL CHECK (feedback_type IN ('SELF', 'MANAGER', 'PEER')),
    comments TEXT,
    overall_rating INT CHECK (overall_rating >= 1 AND overall_rating <= 5),
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'SUBMITTED')),
    submitted_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_feedbacks_business_id ON feedbacks(business_id);
CREATE INDEX idx_feedbacks_employee ON feedbacks(employee_id);
CREATE INDEX idx_feedbacks_reviewer ON feedbacks(reviewer_id);
CREATE INDEX idx_feedbacks_cycle ON feedbacks(review_cycle_id);
CREATE INDEX idx_feedbacks_type ON feedbacks(feedback_type);

-- ============================================================================
-- 11. FEEDBACK_COMPETENCIES (Bridge table)
-- ============================================================================
CREATE TABLE feedback_competencies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    feedback_id UUID NOT NULL REFERENCES feedbacks(id) ON DELETE CASCADE,
    competency_id UUID NOT NULL REFERENCES competencies(id) ON DELETE RESTRICT,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comments TEXT,
    CONSTRAINT uq_feedback_competency UNIQUE (feedback_id, competency_id)
);

CREATE INDEX idx_fb_comp_feedback ON feedback_competencies(feedback_id);
CREATE INDEX idx_fb_comp_competency ON feedback_competencies(competency_id);

-- ============================================================================
-- 12. REVIEWS
-- ============================================================================
CREATE TABLE reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_id VARCHAR(64) NOT NULL UNIQUE,
    employee_id UUID NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    reviewer_id UUID NOT NULL REFERENCES employees(id) ON DELETE RESTRICT,
    review_cycle_id UUID NOT NULL REFERENCES review_cycles(id) ON DELETE RESTRICT,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    overall_score NUMERIC(5, 2) CHECK (overall_score >= 0 AND overall_score <= 100),
    summary TEXT,
    recommendation TEXT,
    completed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_employee_review_cycle UNIQUE (employee_id, review_cycle_id)
);

CREATE INDEX idx_reviews_business_id ON reviews(business_id);
CREATE INDEX idx_reviews_employee ON reviews(employee_id);
CREATE INDEX idx_reviews_reviewer ON reviews(reviewer_id);
CREATE INDEX idx_reviews_cycle ON reviews(review_cycle_id);
CREATE INDEX idx_reviews_status ON reviews(status);

-- ============================================================================
-- 13. REVIEW_GOALS (Bridge table)
-- ============================================================================
CREATE TABLE review_goals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    review_id UUID NOT NULL REFERENCES reviews(id) ON DELETE CASCADE,
    goal_id UUID NOT NULL REFERENCES goals(id) ON DELETE RESTRICT,
    goal_score NUMERIC(5, 2) CHECK (goal_score >= 0 AND goal_score <= 100),
    comments TEXT,
    CONSTRAINT uq_review_goal UNIQUE (review_id, goal_id)
);

CREATE INDEX idx_review_goals_review ON review_goals(review_id);
CREATE INDEX idx_review_goals_goal ON review_goals(goal_id);

-- ============================================================================
-- 14. DEVELOPMENT_PLANS
-- ============================================================================
CREATE TABLE development_plans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_id VARCHAR(64) NOT NULL UNIQUE,
    employee_id UUID NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    review_id UUID REFERENCES reviews(id) ON DELETE SET NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    objectives TEXT,
    skills_to_improve TEXT,
    action_items TEXT,
    target_date DATE,
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'SUBMITTED', 'APPROVED', 'REJECTED', 'IN_PROGRESS', 'COMPLETED')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_dev_plans_business_id ON development_plans(business_id);
CREATE INDEX idx_dev_plans_employee ON development_plans(employee_id);
CREATE INDEX idx_dev_plans_review ON development_plans(review_id);
CREATE INDEX idx_dev_plans_status ON development_plans(status);

-- ============================================================================
-- 15. APPROVAL_HISTORY
-- ============================================================================
CREATE TABLE approval_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    development_plan_id UUID NOT NULL REFERENCES development_plans(id) ON DELETE CASCADE,
    approver_id UUID NOT NULL REFERENCES employees(id) ON DELETE RESTRICT,
    action VARCHAR(32) NOT NULL CHECK (action IN ('SUBMITTED', 'APPROVED', 'REJECTED')),
    comments TEXT,
    action_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_approval_history_plan ON approval_history(development_plan_id);
CREATE INDEX idx_approval_history_approver ON approval_history(approver_id);

-- ============================================================================
-- 16. AUDIT_LOGS
-- ============================================================================
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    actor_id UUID REFERENCES employees(id) ON DELETE SET NULL,
    action VARCHAR(64) NOT NULL,
    entity_type VARCHAR(64) NOT NULL,
    entity_id VARCHAR(64) NOT NULL,
    previous_value TEXT,
    new_value TEXT,
    reason VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_logs_actor ON audit_logs(actor_id);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);

-- ============================================================================
-- 17. ANOMALY_CASES
-- ============================================================================
CREATE TABLE anomaly_cases (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_id VARCHAR(64) NOT NULL UNIQUE,
    issue_type VARCHAR(100) NOT NULL,
    affected_entity VARCHAR(64) NOT NULL,
    affected_business_id VARCHAR(64) NOT NULL,
    severity VARCHAR(32) NOT NULL DEFAULT 'MEDIUM' CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    expected_outcome TEXT,
    actual_outcome TEXT,
    detection_status VARCHAR(32) NOT NULL DEFAULT 'DETECTED' CHECK (detection_status IN ('DETECTED', 'UNDETECTED', 'FALSE_POSITIVE')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_anomaly_cases_business_id ON anomaly_cases(business_id);
CREATE INDEX idx_anomaly_cases_type ON anomaly_cases(issue_type);
CREATE INDEX idx_anomaly_cases_severity ON anomaly_cases(severity);
CREATE INDEX idx_anomaly_cases_status ON anomaly_cases(detection_status);

-- ============================================================================
-- 18. GOAL_HISTORY
-- ============================================================================
CREATE TABLE goal_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    goal_id UUID NOT NULL REFERENCES goals(id) ON DELETE CASCADE,
    field_changed VARCHAR(64) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    changed_by UUID REFERENCES employees(id) ON DELETE SET NULL,
    changed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_goal_history_goal ON goal_history(goal_id);
CREATE INDEX idx_goal_history_changed_at ON goal_history(changed_at);

-- ============================================================================
-- 19. KEY_RESULT_HISTORY
-- ============================================================================
CREATE TABLE key_result_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    key_result_id UUID NOT NULL REFERENCES key_results(id) ON DELETE CASCADE,
    old_value NUMERIC(12, 2) NOT NULL,
    new_value NUMERIC(12, 2) NOT NULL,
    changed_by UUID REFERENCES employees(id) ON DELETE SET NULL,
    changed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_kr_history_kr ON key_result_history(key_result_id);
CREATE INDEX idx_kr_history_changed_at ON key_result_history(changed_at);

-- ============================================================================
-- 20. DATA_IMPORT_RUNS
-- ============================================================================
CREATE TABLE data_import_runs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    seed VARCHAR(64) NOT NULL,
    scale VARCHAR(32) NOT NULL CHECK (scale IN ('STARTER', 'FULL')),
    generator_version VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'VALIDATING', 'IMPORTING', 'COMPLETED', 'FAILED')),
    total_records INT NOT NULL DEFAULT 0,
    valid_records INT NOT NULL DEFAULT 0,
    invalid_records INT NOT NULL DEFAULT 0,
    manifest_checksum VARCHAR(128),
    started_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_data_import_runs_status ON data_import_runs(status);
CREATE INDEX idx_data_import_runs_started ON data_import_runs(started_at);

-- ============================================================================
-- 21. APP_USERS (Authentication & RBAC)
-- ============================================================================
CREATE TABLE app_users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id UUID REFERENCES employees(id) ON DELETE SET NULL,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(32) NOT NULL CHECK (role IN ('EMPLOYEE', 'MANAGER', 'HR_PARTNER', 'OPERATIONS', 'ADMIN')),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_app_users_username ON app_users(username);
CREATE INDEX idx_app_users_role ON app_users(role);
CREATE INDEX idx_app_users_employee ON app_users(employee_id);
