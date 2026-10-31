"""
PerformIQ - Feedback & Competency Rating Generator
Generates multi-rater 360 feedback (Self, Manager, Peer) with rubric evaluations.
"""

import random
from typing import Any, Dict, List, Tuple
from faker import Faker

from . import config, id_generator

FEEDBACK_COMMENTS = {
    5: "Exceptional mastery demonstrated across high-ambiguity projects. Role model for the team.",
    4: "Consistently exceeds expectations with strong ownership, technical craft, and clear communication.",
    3: "Meets performance expectations reliably; delivers assigned milestones within expected timelines.",
    2: "Partially meets requirements; needs closer guidance on cross-team collaboration and execution velocity.",
    1: "Requires significant development and structured performance improvement coaching.",
}


def generate_feedbacks_and_ratings(
    fake: Faker,
    employees: List[Dict[str, Any]],
    review_cycles: List[Dict[str, Any]],
    manager_assignments: List[Dict[str, Any]],
    scale_config: config.GeneratorScale,
) -> Tuple[List[Dict[str, Any]], List[Dict[str, Any]]]:
    """
    Generates:
    1. Feedbacks
    2. FeedbackCompetencies
    """
    feedbacks: List[Dict[str, Any]] = []
    fb_competencies: List[Dict[str, Any]] = []

    # Map employee_id -> manager_id
    emp_to_mgr = {a["employee_business_id"]: a["manager_business_id"] for a in manager_assignments}

    # Group employees by department for peer feedback
    dept_employees: Dict[str, List[str]] = {}
    for emp in employees:
        dept_employees.setdefault(emp["department"], []).append(emp["business_id"])

    fb_seq = 1

    for cycle in review_cycles:
        cycle_id = cycle["business_id"]
        is_closed = cycle["status"] == "CLOSED"

        for emp in employees:
            emp_id = emp["business_id"]

            # 1. Self Feedback
            self_fb_id = id_generator.feedback_id(fb_seq)
            fb_seq += 1
            self_rating = random.choices([3, 4, 5], weights=[20, 60, 20], k=1)[0]
            self_fb = {
                "business_id": self_fb_id,
                "employee_business_id": emp_id,
                "reviewer_business_id": emp_id,
                "review_cycle_business_id": cycle_id,
                "feedback_type": "SELF",
                "comments": f"Self-reflection: {FEEDBACK_COMMENTS[self_rating]}",
                "overall_rating": self_rating,
                "status": "SUBMITTED" if is_closed else random.choice(["SUBMITTED", "DRAFT"]),
                "submitted_at": cycle["end_date"] + "T16:00:00Z" if is_closed else None,
            }
            feedbacks.append(self_fb)

            # Add ratings for 3-5 competencies
            sampled_comps = random.sample(config.COMPETENCIES, random.randint(3, 5))
            for comp in sampled_comps:
                comp_rating = max(1, min(5, self_rating + random.choice([-1, 0, 1])))
                fb_competencies.append(
                    {
                        "feedback_business_id": self_fb_id,
                        "competency_business_id": comp[0],
                        "rating": comp_rating,
                        "comments": f"Self assessment on {comp[1]}: {FEEDBACK_COMMENTS[comp_rating]}",
                    }
                )

            # 2. Manager Feedback (if manager exists)
            mgr_id = emp_to_mgr.get(emp_id)
            if mgr_id:
                mgr_fb_id = id_generator.feedback_id(fb_seq)
                fb_seq += 1
                mgr_rating = random.choices([2, 3, 4, 5], weights=[5, 30, 50, 15], k=1)[0]
                mgr_fb = {
                    "business_id": mgr_fb_id,
                    "employee_business_id": emp_id,
                    "reviewer_business_id": mgr_id,
                    "review_cycle_business_id": cycle_id,
                    "feedback_type": "MANAGER",
                    "comments": f"Manager evaluation: {FEEDBACK_COMMENTS[mgr_rating]}",
                    "overall_rating": mgr_rating,
                    "status": "SUBMITTED" if is_closed else random.choice(["SUBMITTED", "DRAFT"]),
                    "submitted_at": cycle["end_date"] + "T17:00:00Z" if is_closed else None,
                }
                feedbacks.append(mgr_fb)

                for comp in sampled_comps:
                    comp_rating = max(1, min(5, mgr_rating + random.choice([-1, 0, 0, 1])))
                    fb_competencies.append(
                        {
                            "feedback_business_id": mgr_fb_id,
                            "competency_business_id": comp[0],
                            "rating": comp_rating,
                            "comments": f"Manager notes on {comp[1]}: {FEEDBACK_COMMENTS[comp_rating]}",
                        }
                    )

            # 3. Peer Feedback (1-2 peers)
            peers = [p for p in dept_employees.get(emp["department"], []) if p != emp_id]
            if peers:
                peer_sample = random.sample(peers, min(len(peers), random.randint(1, 2)))
                for peer_id in peer_sample:
                    peer_fb_id = id_generator.feedback_id(fb_seq)
                    fb_seq += 1
                    peer_rating = random.choices([3, 4, 5], weights=[20, 60, 20], k=1)[0]
                    peer_fb = {
                        "business_id": peer_fb_id,
                        "employee_business_id": emp_id,
                        "reviewer_business_id": peer_id,
                        "review_cycle_business_id": cycle_id,
                        "feedback_type": "PEER",
                        "comments": f"Peer feedback: {FEEDBACK_COMMENTS[peer_rating]}",
                        "overall_rating": peer_rating,
                        "status": "SUBMITTED" if is_closed else random.choice(["SUBMITTED", "DRAFT"]),
                        "submitted_at": cycle["end_date"] + "T18:00:00Z" if is_closed else None,
                    }
                    feedbacks.append(peer_fb)

                    for comp in sampled_comps[:3]:
                        comp_rating = max(1, min(5, peer_rating + random.choice([-1, 0, 1])))
                        fb_competencies.append(
                            {
                                "feedback_business_id": peer_fb_id,
                                "competency_business_id": comp[0],
                                "rating": comp_rating,
                                "comments": f"Peer assessment on {comp[1]}.",
                            }
                        )

    return feedbacks, fb_competencies
