-- ============================================================================
-- PerformIQ - Phase 2 Seed Data Migration
-- V3__seed_data.sql
-- Pre-populates core competencies, initial review cycle, and demo users
-- ============================================================================

-- 1. SEED COMPETENCIES
INSERT INTO competencies (id, business_id, name, description, category, created_at)
VALUES
    ('c0000001-0000-0000-0000-000000000001', 'COMP-000001', 'Leadership & Influence', 'Guides and inspires teams to achieve organizational goals.', 'LEADERSHIP', CURRENT_TIMESTAMP),
    ('c0000001-0000-0000-0000-000000000002', 'COMP-000002', 'Technical Excellence', 'Demonstrates deep domain knowledge, code quality, and engineering craft.', 'TECHNICAL', CURRENT_TIMESTAMP),
    ('c0000001-0000-0000-0000-000000000003', 'COMP-000003', 'Communication & Clarity', 'Communicates transparently, listens actively, and articulates complex concepts clearly.', 'INTERPERSONAL', CURRENT_TIMESTAMP),
    ('c0000001-0000-0000-0000-000000000004', 'COMP-000004', 'Problem Solving & Critical Thinking', 'Breaks down complex problems and arrives at structured, data-driven solutions.', 'COGNITIVE', CURRENT_TIMESTAMP),
    ('c0000001-0000-0000-0000-000000000005', 'COMP-000005', 'Collaboration & Teamwork', 'Works across functional silos to support team goals and build psychological safety.', 'INTERPERSONAL', CURRENT_TIMESTAMP),
    ('c0000001-0000-0000-0000-000000000006', 'COMP-000006', 'Delivery & Execution', 'Consistently delivers high quality outcomes on schedule with high accountability.', 'EXECUTION', CURRENT_TIMESTAMP),
    ('c0000001-0000-0000-0000-000000000007', 'COMP-000007', 'Innovation & Creativity', 'Proactively proposes and tests novel ideas that drive organizational improvement.', 'COGNITIVE', CURRENT_TIMESTAMP),
    ('c0000001-0000-0000-0000-000000000008', 'COMP-000008', 'Adaptability & Resilience', 'Thrives amid ambiguity and pivots effectively when business context shifts.', 'CORE', CURRENT_TIMESTAMP)
ON CONFLICT (business_id) DO NOTHING;

-- 2. SEED INITIAL REVIEW CYCLE
INSERT INTO review_cycles (id, business_id, name, description, start_date, end_date, status, version, created_at, updated_at)
VALUES
    ('c2026001-0000-0000-0000-000000000001', 'CYCLE-2026-H1', '2026 First Half Performance Cycle', 'Bi-annual performance and growth evaluation cycle for all departments.', '2026-01-01', '2026-06-30', 'ACTIVE', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (business_id) DO NOTHING;

-- 3. SEED DEMO EMPLOYEES
-- Admin employee
INSERT INTO employees (id, business_id, first_name, last_name, email, department, designation, joining_date, status, created_at, updated_at)
VALUES
    ('e0000001-0000-0000-0000-000000000001', 'EMP-000001', 'System', 'Administrator', 'admin@performiq.com', 'Operations', 'Platform Administrator', '2023-01-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('e0000001-0000-0000-0000-000000000002', 'EMP-000002', 'Helen', 'Reed', 'hr@performiq.com', 'Human Resources', 'HR Business Partner', '2023-03-15', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('e0000001-0000-0000-0000-000000000003', 'EMP-000003', 'Oliver', 'Stone', 'ops@performiq.com', 'Operations', 'Operations Specialist', '2023-06-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('e0000001-0000-0000-0000-000000000004', 'EMP-000004', 'Marcus', 'Vance', 'manager1@performiq.com', 'Engineering', 'Engineering Manager', '2022-09-01', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('e0000001-0000-0000-0000-000000000005', 'EMP-000005', 'Emma', 'Watson', 'employee1@performiq.com', 'Engineering', 'Senior Software Engineer', '2023-04-10', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (business_id) DO NOTHING;

-- 4. SEED MANAGER RECORD FOR MARCUS VANCE
INSERT INTO managers (id, business_id, employee_id, effective_from, effective_to, status, created_at, updated_at)
VALUES
    ('b0000001-0000-0000-0000-000000000001', 'MGR-000001', 'e0000001-0000-0000-0000-000000000004', '2022-09-01', NULL, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (business_id) DO NOTHING;

-- 5. SEED MANAGER ASSIGNMENT (Marcus manages Emma)
INSERT INTO manager_assignment_history (id, employee_id, manager_id, effective_from, effective_to, reason, created_at)
VALUES
    ('a0000001-0000-0000-0000-000000000001', 'e0000001-0000-0000-0000-000000000005', 'b0000001-0000-0000-0000-000000000001', '2023-04-10', NULL, 'Initial team placement', CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- 6. SEED APP_USERS WITH BCrypt PASSWORDS
INSERT INTO app_users (id, employee_id, username, password_hash, role, enabled, created_at)
VALUES
    ('f0000001-0000-0000-0000-000000000001', 'e0000001-0000-0000-0000-000000000001', 'admin@performiq.com', '$2b$10$FPuG2aP33fur6X5ThsNNyuy8aNeFIpL1RtVM35WhoZ/tnOoT4qppG', 'ADMIN', TRUE, CURRENT_TIMESTAMP),
    ('f0000001-0000-0000-0000-000000000002', 'e0000001-0000-0000-0000-000000000002', 'hr@performiq.com', '$2b$10$FZi4k.yN429LcF1lGo0M/eloALrSdW6njMNK8L65idCzZhbXY/j86', 'HR_PARTNER', TRUE, CURRENT_TIMESTAMP),
    ('f0000001-0000-0000-0000-000000000003', 'e0000001-0000-0000-0000-000000000003', 'ops@performiq.com', '$2b$10$517UcabxFkLrC3iF35dmbOOr0n.MnXH3IfwnFCsI/vLjrawmvveSy', 'OPERATIONS', TRUE, CURRENT_TIMESTAMP),
    ('f0000001-0000-0000-0000-000000000004', 'e0000001-0000-0000-0000-000000000004', 'manager1@performiq.com', '$2b$10$1MqHyec/pRNjZfEH.Zu/BeroVoBVLmW3NuNfjCX3o.Z8JUzYK8GOm', 'MANAGER', TRUE, CURRENT_TIMESTAMP),
    ('f0000001-0000-0000-0000-000000000005', 'e0000001-0000-0000-0000-000000000005', 'employee1@performiq.com', '$2b$10$pwQ14mbhxYbXMMhT3AN7yuqvQmLkI6AIfsFcvp44.KX7fvucYeQQO', 'EMPLOYEE', TRUE, CURRENT_TIMESTAMP)
ON CONFLICT (username) DO NOTHING;
