"""
PerformIQ - Development Plan & Approval Workflow Generator
Generates individual growth plans and managerial approval histories.
"""

import random
from typing import Any, Dict, List, Tuple
from faker import Faker

from . import config, id_generator

DEV_TITLES = [
    "Staff Engineer Technical Leadership & Architecture Mastery",
    "Domain Deep-Dive into Distributed Systems & Fault Tolerance",
    "Transition to Engineering Management & People Leadership",
    "Cross-Functional Stakeholder Management & Strategy",
    "Cloud Native Infrastructure Security & Governance",
]

SKILLS_TO_IMPROVE = [
    "System design presentation under executive scrutiny",
    "Mentoring junior and mid-level engineers",
    "Data-driven product analytics and experimentation",
    "Public speaking and tech conference presentations",
    "Cost optimization and FinOps best practices",
]

ACTION_ITEMS = [
    "Lead technical architecture design reviews for the Q3 customer migration.",
    "Deliver 3 internal tech-talks on event-driven architectures.",
    "Complete AWS Certified Solutions Architect - Professional certification.",
    "Partner with Product team to co-author PRDs for upcoming initiatives.",
]


def generate_development_plans_and_history(
    fake: Faker,
    reviews: List[Dict[str, Any]],
    manager_assignments: List[Dict[str, Any]],
    scale_config: config.GeneratorScale,
) -> Tuple[List[Dict[str, Any]], List[Dict[str, Any]]]:
    """
    Generates:
    1. DevelopmentPlans
    2. ApprovalHistory
    """
    plans: List[Dict[str, Any]] = []
    approval_history: List[Dict[str, Any]] = []

    emp_to_mgr = {a["employee_business_id"]: a["manager_business_id"] for a in manager_assignments}

    plan_seq = 1
    appr_seq = 1

    for rev in reviews:
        if random.random() > scale_config.dev_plan_probability:
            continue

        emp_id = rev["employee_business_id"]
        mgr_id = emp_to_mgr.get(emp_id, emp_id)

        dev_id = id_generator.dev_plan_id(plan_seq)
        plan_seq += 1

        status = random.choices(
            ["APPROVED", "IN_PROGRESS", "COMPLETED", "SUBMITTED"],
            weights=[40, 30, 20, 10],
            k=1,
        )[0]

        plan_record = {
            "business_id": dev_id,
            "employee_business_id": emp_id,
            "review_business_id": rev["business_id"],
            "title": random.choice(DEV_TITLES),
            "description": f"Targeted growth plan following {rev['business_id']} evaluation.",
            "objectives": "Accelerate technical domain expertise and team leadership capabilities.",
            "skills_to_improve": random.choice(SKILLS_TO_IMPROVE),
            "action_items": random.choice(ACTION_ITEMS),
            "target_date": "2026-12-31",
            "status": status,
        }
        plans.append(plan_record)

        # 1. Submission Action
        approval_history.append(
            {
                "history_id": f"APPR-{appr_seq:06d}",
                "development_plan_business_id": dev_id,
                "approver_employee_id": emp_id,
                "action": "SUBMITTED",
                "comments": "Submitted development roadmap for manager review.",
                "action_date": "2026-01-10T10:00:00Z",
            }
        )
        appr_seq += 1

        # 2. Manager Approval (if not just submitted)
        if status in ["APPROVED", "IN_PROGRESS", "COMPLETED"]:
            approval_history.append(
                {
                    "history_id": f"APPR-{appr_seq:06d}",
                    "development_plan_business_id": dev_id,
                    "approver_employee_id": mgr_id,
                    "action": "APPROVED",
                    "comments": "Approved. Roadmap aligns with organizational expectations.",
                    "action_date": "2026-01-15T14:30:00Z",
                }
            )
            appr_seq += 1

    return plans, approval_history
