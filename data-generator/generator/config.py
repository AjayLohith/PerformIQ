"""
PerformIQ - Synthetic Data Generator Configuration
Defines scale parameters, organizational taxonomies, and generation constants.
"""

from dataclasses import dataclass
from typing import Dict, List, Tuple


@dataclass(frozen=True)
class GeneratorScale:
    name: str
    num_employees: int
    num_cycles: int
    goals_per_employee_min: int
    goals_per_employee_max: int
    krs_per_goal_min: int
    krs_per_goal_max: int
    checkins_per_goal_min: int
    checkins_per_goal_max: int
    evidence_probability: float
    feedback_per_employee_min: int
    feedback_per_employee_max: int
    dev_plan_probability: float
    anomaly_count: int


SCALES: Dict[str, GeneratorScale] = {
    "starter": GeneratorScale(
        name="starter",
        num_employees=50,
        num_cycles=2,
        goals_per_employee_min=3,
        goals_per_employee_max=5,
        krs_per_goal_min=2,
        krs_per_goal_max=3,
        checkins_per_goal_min=3,
        checkins_per_goal_max=5,
        evidence_probability=0.45,
        feedback_per_employee_min=3,
        feedback_per_employee_max=5,
        dev_plan_probability=0.75,
        anomaly_count=10,
    ),
    "full": GeneratorScale(
        name="full",
        num_employees=3000,
        num_cycles=3,
        goals_per_employee_min=3,
        goals_per_employee_max=5,
        krs_per_goal_min=2,
        krs_per_goal_max=4,
        checkins_per_goal_min=4,
        checkins_per_goal_max=6,
        evidence_probability=0.50,
        feedback_per_employee_min=3,
        feedback_per_employee_max=5,
        dev_plan_probability=0.65,
        anomaly_count=50,
    ),
}

DEPARTMENTS: List[str] = [
    "Engineering",
    "Product",
    "Design",
    "Marketing",
    "Sales",
    "Human Resources",
    "Finance",
    "Operations",
]

DESIGNATIONS: Dict[str, List[str]] = {
    "Engineering": [
        "Associate Software Engineer",
        "Software Engineer",
        "Senior Software Engineer",
        "Staff Software Engineer",
        "Principal Engineer",
        "Engineering Manager",
        "Director of Engineering",
    ],
    "Product": [
        "Associate Product Manager",
        "Product Manager",
        "Senior Product Manager",
        "Principal Product Manager",
        "Director of Product",
    ],
    "Design": [
        "UI/UX Designer",
        "Senior Product Designer",
        "Lead Designer",
        "Design Director",
    ],
    "Marketing": [
        "Marketing Specialist",
        "Growth Marketing Manager",
        "Content Strategist",
        "Director of Marketing",
    ],
    "Sales": [
        "Sales Development Rep",
        "Account Executive",
        "Senior Account Executive",
        "Sales Director",
    ],
    "Human Resources": [
        "HR Coordinator",
        "HR Business Partner",
        "Senior HRBP",
        "People Operations Director",
    ],
    "Finance": [
        "Financial Analyst",
        "Senior Financial Analyst",
        "Finance Controller",
        "VP of Finance",
    ],
    "Operations": [
        "Operations Specialist",
        "Operations Lead",
        "Platform Administrator",
        "Director of Operations",
    ],
}

GOAL_CATEGORIES: List[str] = [
    "INDIVIDUAL",
    "TEAM",
    "COMPANY",
    "PROFESSIONAL_DEVELOPMENT",
]

METRIC_UNITS: List[str] = [
    "%",
    "ms",
    "USD",
    "hours",
    "tasks",
    "users",
    "tickets",
    "points",
]

COMPETENCIES: List[Tuple[str, str, str]] = [
    ("COMP-000001", "Leadership & Influence", "LEADERSHIP"),
    ("COMP-000002", "Technical Excellence", "TECHNICAL"),
    ("COMP-000003", "Communication & Clarity", "INTERPERSONAL"),
    ("COMP-000004", "Problem Solving & Critical Thinking", "COGNITIVE"),
    ("COMP-000005", "Collaboration & Teamwork", "INTERPERSONAL"),
    ("COMP-000006", "Delivery & Execution", "EXECUTION"),
    ("COMP-000007", "Innovation & Creativity", "COGNITIVE"),
    ("COMP-000008", "Adaptability & Resilience", "CORE"),
]
