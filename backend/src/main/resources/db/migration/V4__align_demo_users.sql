-- ============================================================================
-- PerformIQ - Phase 3 Demo Personas Alignment
-- V4__align_demo_users.sql
-- Aligns demo personas, employees, and app users with password123
-- ============================================================================

-- 1. Update all existing users to password123 ($2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG)
UPDATE app_users SET password_hash = '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG';

-- 2. Ensure demo employees exist (Sarah Chen, Elena Rostova, Alex Rivera)
INSERT INTO employees (id, business_id, first_name, last_name, email, department, designation, joining_date, status, created_at, updated_at)
VALUES
    ('e0000001-0000-0000-0000-000000000010', 'EMP-000010', 'Sarah', 'Chen', 'sarah.chen@performiq.com', 'Engineering', 'VP of Engineering', '2022-03-15', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('e0000001-0000-0000-0000-000000000011', 'EMP-000011', 'Elena', 'Rostova', 'elena.rostova@performiq.com', 'Engineering', 'Staff Software Engineer', '2023-02-10', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('e0000001-0000-0000-0000-000000000012', 'EMP-000012', 'Alex', 'Rivera', 'alex.rivera@performiq.com', 'Engineering', 'Senior Staff Engineer', '2023-05-20', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (email) DO NOTHING;

-- 3. Ensure Sarah Chen is a Manager
INSERT INTO managers (id, business_id, employee_id, effective_from, effective_to, status, created_at, updated_at)
VALUES
    ('b0000001-0000-0000-0000-000000000010', 'MGR-000010', 'e0000001-0000-0000-0000-000000000010', '2022-03-15', NULL, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (employee_id) DO NOTHING;

-- 4. Seed and update demo app_users for frontend personas (password123)
-- Admin
INSERT INTO app_users (id, employee_id, username, password_hash, role, enabled, created_at)
VALUES
    ('f0000001-0000-0000-0000-000000000010', 'e0000001-0000-0000-0000-000000000001', 'admin', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'ADMIN', TRUE, CURRENT_TIMESTAMP)
ON CONFLICT (username) DO UPDATE SET password_hash = EXCLUDED.password_hash, role = 'ADMIN', enabled = TRUE;

-- Sarah Chen
INSERT INTO app_users (id, employee_id, username, password_hash, role, enabled, created_at)
VALUES
    ('f0000001-0000-0000-0000-000000000011', 'e0000001-0000-0000-0000-000000000010', 'sarah.chen', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'MANAGER', TRUE, CURRENT_TIMESTAMP)
ON CONFLICT (username) DO UPDATE SET password_hash = EXCLUDED.password_hash, role = 'MANAGER', enabled = TRUE;

-- Marcus Vance
INSERT INTO app_users (id, employee_id, username, password_hash, role, enabled, created_at)
VALUES
    ('f0000001-0000-0000-0000-000000000012', 'e0000001-0000-0000-0000-000000000004', 'marcus.vance', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'MANAGER', TRUE, CURRENT_TIMESTAMP)
ON CONFLICT (username) DO UPDATE SET password_hash = EXCLUDED.password_hash, role = 'MANAGER', enabled = TRUE;

-- Elena Rostova
INSERT INTO app_users (id, employee_id, username, password_hash, role, enabled, created_at)
VALUES
    ('f0000001-0000-0000-0000-000000000013', 'e0000001-0000-0000-0000-000000000011', 'elena.rostova', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'EMPLOYEE', TRUE, CURRENT_TIMESTAMP)
ON CONFLICT (username) DO UPDATE SET password_hash = EXCLUDED.password_hash, role = 'EMPLOYEE', enabled = TRUE;

-- Alex Rivera
INSERT INTO app_users (id, employee_id, username, password_hash, role, enabled, created_at)
VALUES
    ('f0000001-0000-0000-0000-000000000014', 'e0000001-0000-0000-0000-000000000012', 'alex.rivera', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'EMPLOYEE', TRUE, CURRENT_TIMESTAMP)
ON CONFLICT (username) DO UPDATE SET password_hash = EXCLUDED.password_hash, role = 'EMPLOYEE', enabled = TRUE;
