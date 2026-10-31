"""
PerformIQ - Employee & Hierarchy Generator
Generates realistic employees, managerial reporting hierarchies, and authentication accounts.
"""

import random
from datetime import date, timedelta
from typing import Any, Dict, List, Tuple
from faker import Faker

from . import config, id_generator

DEFAULT_PASSWORD_HASH = "$2b$10$pwQ14mbhxYbXMMhT3AN7yuqvQmLkI6AIfsFcvp44.KX7fvucYeQQO"  # Emp@12345


def generate_employees_and_hierarchy(
    fake: Faker, count: int
) -> Tuple[List[Dict[str, Any]], List[Dict[str, Any]], List[Dict[str, Any]], List[Dict[str, Any]]]:
    """
    Generates:
    1. Employees
    2. Managers
    3. ManagerAssignmentHistory
    4. AppUsers
    """
    employees: List[Dict[str, Any]] = []
    managers: List[Dict[str, Any]] = []
    assignments: List[Dict[str, Any]] = []
    users: List[Dict[str, Any]] = []

    used_emails = set()
    start_date_range = date(2021, 1, 1)

    # 1. Generate baseline employees
    for i in range(1, count + 1):
        emp_id = id_generator.employee_id(i)
        first_name = fake.first_name()
        last_name = fake.last_name()

        clean_first = "".join(c for c in first_name.lower() if c.isalnum())
        clean_last = "".join(c for c in last_name.lower() if c.isalnum())
        base_email = f"{clean_first}.{clean_last}@performiq.com"
        email = base_email
        suffix = 1
        while email in used_emails:
            email = f"{clean_first}.{clean_last}{suffix}@performiq.com"
            suffix += 1
        used_emails.add(email)

        department = random.choice(config.DEPARTMENTS)
        dept_designations = config.DESIGNATIONS[department]

        # Seniority distribution: top designations are rarer
        weights = [35, 25, 20, 10, 5, 3, 2][: len(dept_designations)]
        if len(weights) < len(dept_designations):
            weights += [1] * (len(dept_designations) - len(weights))
        designation = random.choices(dept_designations, weights=weights, k=1)[0]

        days_employed = random.randint(30, 1800)
        joining_date = start_date_range + timedelta(days=days_employed)
        if joining_date > date(2026, 1, 1):
            joining_date = date(2025, 11, random.randint(1, 28))

        # Status: 92% active, 5% inactive, 3% terminated
        status_choice = random.choices(
            ["ACTIVE", "INACTIVE", "TERMINATED"], weights=[92, 5, 3], k=1
        )[0]

        emp = {
            "business_id": emp_id,
            "first_name": first_name,
            "last_name": last_name,
            "email": email,
            "department": department,
            "designation": designation,
            "joining_date": joining_date.isoformat(),
            "status": status_choice,
        }
        employees.append(emp)

    # 2. Select Managers (approx 1 manager for every 5-7 employees)
    num_managers = max(3, count // 6)
    # Sort candidates by seniority/joining date
    candidates = sorted(
        employees,
        key=lambda e: (
            "Director" in e["designation"]
            or "VP" in e["designation"]
            or "Manager" in e["designation"]
            or "Lead" in e["designation"],
            e["joining_date"],
        ),
        reverse=True,
    )
    selected_managers = candidates[:num_managers]

    mgr_records_by_emp_id = {}
    for m_seq, m_emp in enumerate(selected_managers, 1):
        mgr_id = id_generator.manager_id(m_seq)
        mgr_rec = {
            "business_id": mgr_id,
            "employee_business_id": m_emp["business_id"],
            "effective_from": m_emp["joining_date"],
            "effective_to": None,
            "status": "ACTIVE" if m_emp["status"] == "ACTIVE" else "INACTIVE",
        }
        managers.append(mgr_rec)
        mgr_records_by_emp_id[m_emp["business_id"]] = mgr_rec

    # 3. Create Manager Assignment Hierarchy (department aligned where possible)
    # Group managers by department
    dept_managers: Dict[str, List[Dict[str, Any]]] = {}
    for m in selected_managers:
        dept = m["department"]
        dept_managers.setdefault(dept, []).append(m)

    assignment_id_counter = 1
    for emp in employees:
        emp_id = emp["business_id"]
        # If this employee is a top manager/director, they might report to an executive or none
        is_mgr = emp_id in mgr_records_by_emp_id

        # Find matching department managers (excluding self)
        available_mgrs = [m for m in dept_managers.get(emp["department"], []) if m["business_id"] != emp_id]
        if not available_mgrs:
            # Fallback to any manager excluding self
            available_mgrs = [m for m in selected_managers if m["business_id"] != emp_id]

        if available_mgrs:
            chosen_mgr = random.choice(available_mgrs)
            mgr_rec = mgr_records_by_emp_id[chosen_mgr["business_id"]]
            assignments.append(
                {
                    "assignment_id": f"MAH-{assignment_id_counter:06d}",
                    "employee_business_id": emp_id,
                    "manager_business_id": mgr_rec["business_id"],
                    "effective_from": emp["joining_date"],
                    "effective_to": None,
                    "reason": "Direct organizational placement",
                }
            )
            assignment_id_counter += 1

    # 4. Create AppUser accounts for all employees
    for emp in employees:
        email = emp["email"]
        dept = emp["department"]
        desig = emp["designation"]

        if email == "admin@performiq.com" or "Platform Administrator" in desig:
            role = "ADMIN"
        elif dept == "Human Resources" or "HR" in desig:
            role = "HR_PARTNER"
        elif dept == "Operations":
            role = "OPERATIONS"
        elif emp["business_id"] in mgr_records_by_emp_id:
            role = "MANAGER"
        else:
            role = "EMPLOYEE"

        users.append(
            {
                "username": email,
                "employee_business_id": emp["business_id"],
                "password_hash": DEFAULT_PASSWORD_HASH,
                "role": role,
                "enabled": emp["status"] != "TERMINATED",
            }
        )

    return employees, managers, assignments, users
