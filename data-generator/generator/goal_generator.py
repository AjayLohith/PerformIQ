"""
PerformIQ - Goal & Key Result Generator
Generates OKRs with precise metric calculations and derived goal progress.
"""

import random
from decimal import Decimal, ROUND_HALF_UP
from typing import Any, Dict, List, Tuple
from faker import Faker

from . import config, id_generator

GOAL_TEMPLATES: Dict[str, List[str]] = {
    "Engineering": [
        "Improve Service Reliability & Uptime to 99.95%",
        "Refactor Monolith Payment Service to Event-Driven Architecture",
        "Reduce CI/CD Pipeline Duration by 40%",
        "Achieve 85% Code Coverage Across Core Repositories",
        "Adopt Zero-Trust Network Architecture for Internal APIs",
        "Accelerate Database Migration with Zero Customer Downtime",
    ],
    "Product": [
        "Launch Mobile App V2 with 4.5+ App Store Rating",
        "Increase Monthly Active User Retention by 15%",
        "Deliver Self-Service Onboarding Funnel",
        "Conduct 30 User Research Interviews for H2 Roadmap",
        "Reduce Feature Time-to-Market from 8 Weeks to 4 Weeks",
    ],
    "Design": [
        "Unify Enterprise Design System Component Library",
        "Redesign Navigation IA to Improve Usability Score",
        "Publish Accessible Color Contrast Rubric (WCAG 2.1 AA)",
        "Deliver High-Fidelity Prototypes for Billing Workflow",
    ],
    "Marketing": [
        "Increase Inbound Organic Pipeline Growth by 25%",
        "Execute Global User Conference with 1,500+ Attendees",
        "Improve Lead Conversion Rate from Demo to Opportunity",
        "Launch Thought Leadership Podcast Series",
    ],
    "Sales": [
        "Achieve $1.2M in Net New ARR in Target Territory",
        "Increase Average Deal Size by 20% Through Enterprise Upselling",
        "Improve Sales Cycle Velocity from 60 Days to 45 Days",
        "Expand Partner Channel Ecosystem with 5 Strategic Integrations",
    ],
    "Human Resources": [
        "Reduce Time-to-Hire for Senior Engineering Roles to < 35 Days",
        "Roll Out Engineering Career Ladders Across All Grades",
        "Achieve 90% Employee Net Promoter Score (eNPS)",
        "Implement Structured Manager Coaching Program",
    ],
    "Finance": [
        "Automate Monthly Revenue Recognition Pipeline",
        "Reduce Operating Expenditure by 8% via Vendor Consolidation",
        "Deliver Clean SOC2 Type II Audit Compliance",
        "Modernize SaaS Metric Dashboards (CAC, LTV, Net Retention)",
    ],
    "Operations": [
        "Automate Data Ingestion Pipeline & Quality Validation",
        "Ensure 99.9% Infrastructure Availability across Cloud Clusters",
        "Reduce MTTR for High Severity Incidents to < 15 Minutes",
        "Establish Automated Synthetic Test Data Environment",
    ],
}


def round_decimal(val: float) -> Decimal:
    return Decimal(str(val)).quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)


def calculate_kr_progress(metric_type: str, start: float, target: float, current: float) -> Decimal:
    if metric_type == "INCREASE":
        denominator = target - start
        if denominator == 0:
            prog = 100.0 if current >= target else 0.0
        else:
            prog = ((current - start) / denominator) * 100.0
    else:  # DECREASE
        denominator = start - target
        if denominator == 0:
            prog = 100.0 if current <= target else 0.0
        else:
            prog = ((start - current) / denominator) * 100.0

    clamped = max(0.0, min(100.0, prog))
    return round_decimal(clamped)


def generate_goals_and_key_results(
    fake: Faker,
    employees: List[Dict[str, Any]],
    review_cycles: List[Dict[str, Any]],
    scale_config: config.GeneratorScale,
) -> Tuple[List[Dict[str, Any]], List[Dict[str, Any]], List[Dict[str, Any]], List[Dict[str, Any]]]:
    """
    Generates:
    1. Goals
    2. Key Results
    3. GoalHistory
    4. KeyResultHistory
    """
    goals: List[Dict[str, Any]] = []
    key_results: List[Dict[str, Any]] = []
    goal_history: List[Dict[str, Any]] = []
    kr_history: List[Dict[str, Any]] = []

    gh_seq = 1
    krh_seq = 1

    for emp in employees:
        emp_id = emp["business_id"]
        dept = emp["department"]
        templates = GOAL_TEMPLATES.get(dept, GOAL_TEMPLATES["Engineering"])

        g_seq = 1
        for cycle in review_cycles:
            cycle_id = cycle["business_id"]
            is_closed = cycle["status"] == "CLOSED"

            num_goals = random.randint(
                scale_config.goals_per_employee_min, scale_config.goals_per_employee_max
            )

            # Pick distinct titles
            sampled_titles = random.sample(templates, min(num_goals, len(templates)))
            while len(sampled_titles) < num_goals:
                sampled_titles.append(f"{random.choice(templates)} - Phase {len(sampled_titles)+1}")

            for title in sampled_titles:
                g_id = id_generator.goal_id(emp_id, g_seq)
                g_seq += 1
                weight = Decimal(str(random.choice([1.00, 1.50, 2.00, 2.50]))).quantize(Decimal("0.01"))
                category = random.choice(config.GOAL_CATEGORIES)

                if is_closed:
                    goal_status = random.choices(["COMPLETED", "CANCELLED"], weights=[90, 10], k=1)[0]
                else:
                    goal_status = random.choices(["ACTIVE", "DRAFT", "COMPLETED"], weights=[85, 10, 5], k=1)[0]

                # Generate 2 to 3 Key Results for this Goal
                num_krs = random.randint(scale_config.krs_per_goal_min, scale_config.krs_per_goal_max)
                goal_krs = []

                for k_seq in range(1, num_krs + 1):
                    kr_id = id_generator.key_result_id(g_id, k_seq)
                    metric_type = random.choice(["INCREASE", "DECREASE"])
                    kr_weight = Decimal(str(random.choice([1.00, 1.00, 2.00]))).quantize(Decimal("0.01"))
                    unit = random.choice(config.METRIC_UNITS)

                    if metric_type == "INCREASE":
                        start_val = float(random.choice([0, 10, 20, 50]))
                        target_val = float(random.choice([80, 100, 150, 200]))
                        if target_val <= start_val:
                            target_val = start_val + 100.0

                        if is_closed and goal_status == "COMPLETED":
                            current_val = target_val
                        elif goal_status == "DRAFT":
                            current_val = start_val
                        else:
                            # In-progress realistic value
                            factor = random.uniform(0.3, 0.95)
                            current_val = start_val + (target_val - start_val) * factor
                    else:  # DECREASE
                        start_val = float(random.choice([100, 150, 200, 500]))
                        target_val = float(random.choice([20, 30, 50]))
                        if start_val <= target_val:
                            start_val = target_val + 100.0

                        if is_closed and goal_status == "COMPLETED":
                            current_val = target_val
                        elif goal_status == "DRAFT":
                            current_val = start_val
                        else:
                            factor = random.uniform(0.3, 0.95)
                            current_val = start_val - (start_val - target_val) * factor

                    kr_progress = calculate_kr_progress(metric_type, start_val, target_val, current_val)

                    kr_record = {
                        "business_id": kr_id,
                        "goal_business_id": g_id,
                        "title": f"Deliver metric {k_seq} for {title[:40]}",
                        "metric_type": metric_type,
                        "target_value": str(round_decimal(target_val)),
                        "current_value": str(round_decimal(current_val)),
                        "start_value": str(round_decimal(start_val)),
                        "weight": str(kr_weight),
                        "unit": unit,
                        "progress": str(kr_progress),
                    }
                    key_results.append(kr_record)
                    goal_krs.append(kr_record)

                    # Initial KR history
                    kr_history.append(
                        {
                            "history_id": f"KRH-{krh_seq:06d}",
                            "key_result_business_id": kr_id,
                            "old_value": str(round_decimal(start_val)),
                            "new_value": str(round_decimal(current_val)),
                            "changed_by_employee_id": emp_id,
                            "changed_at": cycle["start_date"] + "T10:00:00Z",
                        }
                    )
                    krh_seq += 1

                # Calculate Goal Progress: Σ(kr_progress * weight) / Σ(weight)
                total_weighted_progress = sum(
                    Decimal(kr["progress"]) * Decimal(kr["weight"]) for kr in goal_krs
                )
                total_kr_weight = sum(Decimal(kr["weight"]) for kr in goal_krs)

                if total_kr_weight > 0:
                    goal_progress = (total_weighted_progress / total_kr_weight).quantize(
                        Decimal("0.01"), rounding=ROUND_HALF_UP
                    )
                else:
                    goal_progress = Decimal("0.00")

                goal_record = {
                    "business_id": g_id,
                    "employee_business_id": emp_id,
                    "review_cycle_business_id": cycle_id,
                    "title": title,
                    "description": f"Key initiative for {emp['department']} in {cycle['name']}.",
                    "category": category,
                    "status": goal_status,
                    "weight": str(weight),
                    "progress": str(goal_progress),
                }
                goals.append(goal_record)

                # Goal History
                goal_history.append(
                    {
                        "history_id": f"GH-{gh_seq:06d}",
                        "goal_business_id": g_id,
                        "field_changed": "status",
                        "old_value": "DRAFT",
                        "new_value": goal_status,
                        "changed_by_employee_id": emp_id,
                        "changed_at": cycle["start_date"] + "T09:00:00Z",
                    }
                )
                gh_seq += 1

    return goals, key_results, goal_history, kr_history
