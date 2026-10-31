"""
PerformIQ - Deterministic Business ID Formatter
Generates consistent, traceable identifiers adhering to application architecture.
"""


def employee_id(seq: int) -> str:
    return f"EMP-{seq:06d}"


def manager_id(seq: int) -> str:
    return f"MGR-{seq:06d}"


def review_cycle_id(year: int, period: str) -> str:
    return f"CYCLE-{year}-{period}"


def goal_id(emp_id: str, seq: int) -> str:
    return f"GOAL-{emp_id}-{seq:03d}"


def key_result_id(g_id: str, seq: int) -> str:
    return f"KR-{g_id}-{seq:02d}"


def checkin_id(seq: int) -> str:
    return f"CHECKIN-{seq:06d}"


def evidence_id(seq: int) -> str:
    return f"EVID-{seq:06d}"


def feedback_id(seq: int) -> str:
    return f"FEEDBACK-{seq:06d}"


def review_id(year: int, seq: int) -> str:
    return f"REV-{year}-{seq:06d}"


def dev_plan_id(seq: int) -> str:
    return f"DEVP-{seq:06d}"


def anomaly_id(seq: int) -> str:
    return f"ANOM-{seq:06d}"
