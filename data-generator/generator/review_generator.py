"""
PerformIQ - Performance Review & Goal Score Generator
Generates formal appraisals linking employee goals with review scores.
"""

import random
from decimal import Decimal, ROUND_HALF_UP
from typing import Any, Dict, List, Tuple
from faker import Faker

from . import id_generator

RECOMMENDATIONS = [
    "Ready for promotion to next engineering grade.",
    "Recommended for accelerated leadership development and management path.",
    "Recommend compensation calibration and equity refresh based on top performance.",
    "Continue current strong trajectory; focus on cross-functional domain breadth.",
    "Provide dedicated mentoring on architectural documentation and communication.",
]


def generate_reviews_and_scores(
    fake: Faker,
    employees: List[Dict[str, Any]],
    review_cycles: List[Dict[str, Any]],
    goals: List[Dict[str, Any]],
    manager_assignments: List[Dict[str, Any]],
) -> Tuple[List[Dict[str, Any]], List[Dict[str, Any]]]:
    """
    Generates:
    1. Reviews
    2. ReviewGoals
    """
    reviews: List[Dict[str, Any]] = []
    review_goals: List[Dict[str, Any]] = []

    emp_to_mgr = {a["employee_business_id"]: a["manager_business_id"] for a in manager_assignments}

    # Group goals by (employee_id, review_cycle_id)
    emp_cycle_goals: Dict[Tuple[str, str], List[Dict[str, Any]]] = {}
    for g in goals:
        key = (g["employee_business_id"], g["review_cycle_business_id"])
        emp_cycle_goals.setdefault(key, []).append(g)

    rev_seq = 1

    for cycle in review_cycles:
        cycle_id = cycle["business_id"]
        is_closed = cycle["status"] == "CLOSED"
        year = int(cycle_id.split("-")[1])

        # Generate reviews primarily for closed cycles, and subset for active cycles
        candidates = employees if is_closed else employees[: len(employees) // 3]

        for emp in candidates:
            emp_id = emp["business_id"]
            mgr_id = emp_to_mgr.get(emp_id, emp_id)

            cycle_goals = emp_cycle_goals.get((emp_id, cycle_id), [])
            if not cycle_goals:
                continue

            rev_id = id_generator.review_id(year, rev_seq)
            rev_seq += 1

            # Evaluate each goal in the cycle
            goal_scores = []
            for g in cycle_goals:
                progress = float(g["progress"])
                # Goal score correlates with progress with small variance
                score_val = max(50.0, min(100.0, progress + random.uniform(-5.0, 5.0)))
                dec_score = Decimal(str(score_val)).quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)
                goal_scores.append(dec_score)

                review_goals.append(
                    {
                        "review_business_id": rev_id,
                        "goal_business_id": g["business_id"],
                        "goal_score": str(dec_score),
                        "comments": f"Performance assessment on goal: {g['title']}",
                    }
                )

            # Overall review score = average of goal scores
            if goal_scores:
                avg_score = sum(goal_scores) / len(goal_scores)
                overall_score = avg_score.quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)
            else:
                overall_score = Decimal("85.00")

            status = "COMPLETED" if is_closed else random.choice(["PENDING", "IN_PROGRESS", "COMPLETED"])

            review_record = {
                "business_id": rev_id,
                "employee_business_id": emp_id,
                "reviewer_business_id": mgr_id,
                "review_cycle_business_id": cycle_id,
                "status": status,
                "overall_score": str(overall_score),
                "summary": f"Performance evaluation for {emp['first_name']} {emp['last_name']} in {cycle['name']}.",
                "recommendation": random.choice(RECOMMENDATIONS),
                "completed_at": cycle["end_date"] + "T18:00:00Z" if status == "COMPLETED" else None,
            }
            reviews.append(review_record)

    return reviews, review_goals
