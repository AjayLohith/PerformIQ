"""
PerformIQ - Controlled Anomaly Injector
Injects labeled benchmark data quality anomalies for validation testing and audit evaluation.
"""

import random
from typing import Any, Dict, List, Tuple

from . import id_generator


def inject_anomalies(
    goals: List[Dict[str, Any]],
    key_results: List[Dict[str, Any]],
    check_ins: List[Dict[str, Any]],
    feedbacks: List[Dict[str, Any]],
    employees: List[Dict[str, Any]],
    manager_assignments: List[Dict[str, Any]],
    count: int = 10,
) -> List[Dict[str, Any]]:
    """
    Injects realistic data quality issues and creates labeled anomaly case records.
    """
    anomalies: List[Dict[str, Any]] = []
    anom_seq = 1

    # Anomaly 1: Progress Calculation Mismatch on Goal
    if goals and anom_seq <= count:
        target_goal = random.choice(goals)
        original_prog = target_goal["progress"]
        corrupted_prog = "99.99" if original_prog != "99.99" else "12.34"
        target_goal["progress"] = corrupted_prog
        anomalies.append(
            {
                "business_id": id_generator.anomaly_id(anom_seq),
                "issue_type": "PROGRESS_CALCULATION_MISMATCH",
                "affected_entity": "GOAL",
                "affected_business_id": target_goal["business_id"],
                "severity": "HIGH",
                "expected_outcome": f"Goal progress must equal weighted sum of KRs ({original_prog}%).",
                "actual_outcome": f"Found corrupted progress value: {corrupted_prog}%.",
                "detection_status": "DETECTED",
            }
        )
        anom_seq += 1

    # Anomaly 2: Check-in Date Outside Review Cycle Boundary
    if check_ins and anom_seq <= count:
        target_cin = random.choice(check_ins)
        target_cin["check_in_date"] = "2024-01-01"  # Far in the past
        anomalies.append(
            {
                "business_id": id_generator.anomaly_id(anom_seq),
                "issue_type": "CHECKIN_DATE_OUT_OF_BOUNDS",
                "affected_entity": "CHECK_IN",
                "affected_business_id": target_cin["business_id"],
                "severity": "MEDIUM",
                "expected_outcome": "Check-in date must fall within its associated review cycle dates.",
                "actual_outcome": "Found check-in dated 2024-01-01 outside valid cycle range.",
                "detection_status": "DETECTED",
            }
        )
        anom_seq += 1

    # Anomaly 3: Feedback Rating Out of Range (Rating = 6)
    if feedbacks and anom_seq <= count:
        target_fb = random.choice(feedbacks)
        target_fb["overall_rating"] = 6
        anomalies.append(
            {
                "business_id": id_generator.anomaly_id(anom_seq),
                "issue_type": "OUT_OF_RANGE_RATING",
                "affected_entity": "FEEDBACK",
                "affected_business_id": target_fb["business_id"],
                "severity": "HIGH",
                "expected_outcome": "Feedback overall_rating must be within [1, 5].",
                "actual_outcome": "Found rating value of 6 exceeding the allowed rubric scale.",
                "detection_status": "DETECTED",
            }
        )
        anom_seq += 1

    # Anomaly 4: Inactive / Terminated Employee with Active Goal
    terminated_emps = [e for e in employees if e["status"] == "TERMINATED"]
    if terminated_emps and goals and anom_seq <= count:
        term_emp = terminated_emps[0]
        # Find a goal owned by this terminated employee
        emp_goals = [g for g in goals if g["employee_business_id"] == term_emp["business_id"]]
        if emp_goals:
            target_goal = emp_goals[0]
            target_goal["status"] = "ACTIVE"
            anomalies.append(
                {
                    "business_id": id_generator.anomaly_id(anom_seq),
                    "issue_type": "TERMINATED_EMPLOYEE_ACTIVE_GOAL",
                    "affected_entity": "GOAL",
                    "affected_business_id": target_goal["business_id"],
                    "severity": "CRITICAL",
                    "expected_outcome": "Terminated employees cannot possess active in-progress goals.",
                    "actual_outcome": f"Employee {term_emp['business_id']} is TERMINATED but goal status is ACTIVE.",
                    "detection_status": "DETECTED",
                }
            )
            anom_seq += 1

    # Anomaly 5: Self-Supervising Manager Assignment
    if manager_assignments and anom_seq <= count:
        target_assign = random.choice(manager_assignments)
        target_assign["manager_business_id"] = target_assign["employee_business_id"]
        anomalies.append(
            {
                "business_id": id_generator.anomaly_id(anom_seq),
                "issue_type": "CIRCULAR_REPORTING_HIERARCHY",
                "affected_entity": "MANAGER_ASSIGNMENT_HISTORY",
                "affected_business_id": target_assign["assignment_id"],
                "severity": "CRITICAL",
                "expected_outcome": "Employee cannot be assigned as their own direct manager.",
                "actual_outcome": f"Self-assignment detected on employee {target_assign['employee_business_id']}.",
                "detection_status": "DETECTED",
            }
        )
        anom_seq += 1

    # Anomaly 6: Key Result Progress Out of Bounds
    if key_results and anom_seq <= count:
        target_kr = random.choice(key_results)
        target_kr["progress"] = "150.00"
        anomalies.append(
            {
                "business_id": id_generator.anomaly_id(anom_seq),
                "issue_type": "KEY_RESULT_PROGRESS_OUT_OF_BOUNDS",
                "affected_entity": "KEY_RESULT",
                "affected_business_id": target_kr["business_id"],
                "severity": "MEDIUM",
                "expected_outcome": "Key result progress percentage must be clamped between [0.00, 100.00].",
                "actual_outcome": "Found un-clamped progress value of 150.00% without approved justification.",
                "detection_status": "DETECTED",
            }
        )
        anom_seq += 1

    # Anomaly 7: Empty Notes on Check-in
    if check_ins and anom_seq <= count:
        target_cin = random.choice([c for c in check_ins if c["business_id"] != target_cin["business_id"]])
        target_cin["notes"] = ""
        anomalies.append(
            {
                "business_id": id_generator.anomaly_id(anom_seq),
                "issue_type": "CHECKIN_EMPTY_NOTES",
                "affected_entity": "CHECK_IN",
                "affected_business_id": target_cin["business_id"],
                "severity": "LOW",
                "expected_outcome": "Check-in notes must contain non-empty narrative of accomplishments.",
                "actual_outcome": "Found empty string in check-in notes field.",
                "detection_status": "DETECTED",
            }
        )
        anom_seq += 1

    # Anomaly 8: Orphan Goal Reference
    if goals and anom_seq <= count:
        target_goal = random.choice([g for g in goals if g["business_id"] != target_goal["business_id"]])
        target_goal["review_cycle_business_id"] = "CYCLE-1999-H1"
        anomalies.append(
            {
                "business_id": id_generator.anomaly_id(anom_seq),
                "issue_type": "ORPHAN_CYCLE_REFERENCE",
                "affected_entity": "GOAL",
                "affected_business_id": target_goal["business_id"],
                "severity": "HIGH",
                "expected_outcome": "Goal review_cycle_id must resolve to a valid existing ReviewCycle entity.",
                "actual_outcome": "Goal references non-existent cycle CYCLE-1999-H1.",
                "detection_status": "DETECTED",
            }
        )
        anom_seq += 1

    # Fill remaining count with additional instances if requested
    while anom_seq <= count and goals:
        g = random.choice(goals)
        g["progress"] = "105.00"
        anomalies.append(
            {
                "business_id": id_generator.anomaly_id(anom_seq),
                "issue_type": "EXCESSIVE_PROGRESS_CLAMP",
                "affected_entity": "GOAL",
                "affected_business_id": g["business_id"],
                "severity": "LOW",
                "expected_outcome": "Goal progress must be clamped to maximum 100.00%.",
                "actual_outcome": "Found 105.00% without override annotation.",
                "detection_status": "DETECTED",
            }
        )
        anom_seq += 1

    return anomalies
