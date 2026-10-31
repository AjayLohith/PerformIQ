"""
PerformIQ - Review Cycle Generator
Generates historical and current performance evaluation cycles.
"""

from typing import Any, Dict, List
from . import id_generator


def generate_review_cycles(num_cycles: int = 2) -> List[Dict[str, Any]]:
    cycle_definitions = [
        {
            "year": 2025,
            "period": "H1",
            "name": "2025 First Half Performance Cycle",
            "description": "Bi-annual review and development planning for 2025 H1.",
            "start_date": "2025-01-01",
            "end_date": "2025-06-30",
            "status": "CLOSED",
        },
        {
            "year": 2025,
            "period": "H2",
            "name": "2025 Second Half Performance Cycle",
            "description": "End-of-year comprehensive review and appraisal cycle for 2025.",
            "start_date": "2025-07-01",
            "end_date": "2025-12-31",
            "status": "CLOSED",
        },
        {
            "year": 2026,
            "period": "H1",
            "name": "2026 First Half Performance Cycle",
            "description": "Active bi-annual performance evaluation and goal execution cycle.",
            "start_date": "2026-01-01",
            "end_date": "2026-06-30",
            "status": "ACTIVE",
        },
    ]

    selected = cycle_definitions[-num_cycles:]
    results = []
    for c in selected:
        b_id = id_generator.review_cycle_id(c["year"], c["period"])
        results.append(
            {
                "business_id": b_id,
                "name": c["name"],
                "description": c["description"],
                "start_date": c["start_date"],
                "end_date": c["end_date"],
                "status": c["status"],
            }
        )
    return results
