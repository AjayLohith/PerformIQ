"""
PerformIQ - Check-in & Evidence Generator
Generates periodic progress check-ins with attached supporting artifacts.
"""

import random
from datetime import datetime, timedelta
from decimal import Decimal, ROUND_HALF_UP
from typing import Any, Dict, List, Tuple
from faker import Faker

from . import config, id_generator

EVIDENCE_TEMPLATES = [
    ("SonarQube Quality Gate Report", "https://sonar.internal/dashboard?id=service-core", "METRIC"),
    ("GitHub Pull Request Benchmark", "https://github.internal/org/repo/pull/482", "URL"),
    ("Grafana Performance Dashboard", "https://grafana.internal/d/api-latency-p99", "METRIC"),
    ("Architecture Decision Record (ADR)", "https://confluence.internal/display/ARCH/ADR-042", "DOCUMENT"),
    ("Figma Interactive Prototype", "https://figma.com/file/system-design-v2", "URL"),
    ("Data Dog Error Rate Tracker", "https://app.datadoghq.com/monitors/104928", "METRIC"),
    ("Security Penetration Audit Signoff", "https://compliance.internal/audits/2026-q1-soc2.pdf", "DOCUMENT"),
]

CHECKIN_NOTES = [
    "Completed initial discovery and benchmarked current pipeline latency.",
    "Refactored database query execution plan; indexed hotspot columns.",
    "Implemented automated unit tests and integrated linting checks into CI workflow.",
    "Conducted team code review and addressed performance review comments.",
    "Deployed staging release and verified zero regression in staging traffic.",
    "Finalized feature flags rollout to 100% of production traffic with positive telemetry.",
]


def generate_checkins_and_evidence(
    fake: Faker,
    goals: List[Dict[str, Any]],
    review_cycles_map: Dict[str, Dict[str, Any]],
    scale_config: config.GeneratorScale,
) -> Tuple[List[Dict[str, Any]], List[Dict[str, Any]]]:
    """
    Generates:
    1. CheckIns
    2. Evidence
    """
    check_ins: List[Dict[str, Any]] = []
    evidence_list: List[Dict[str, Any]] = []

    checkin_seq = 1
    evidence_seq = 1

    for goal in goals:
        g_id = goal["business_id"]
        emp_id = goal["employee_business_id"]
        cycle = review_cycles_map[goal["review_cycle_business_id"]]

        start_dt = datetime.strptime(cycle["start_date"], "%Y-%m-%d")
        end_dt = datetime.strptime(cycle["end_date"], "%Y-%m-%d")
        total_days = (end_dt - start_dt).days

        final_prog = float(goal["progress"])
        num_checkins = random.randint(
            scale_config.checkins_per_goal_min, scale_config.checkins_per_goal_max
        )

        # Distribute dates evenly across cycle duration
        step = max(5, total_days // (num_checkins + 1))

        for c_idx in range(1, num_checkins + 1):
            cin_id = id_generator.checkin_id(checkin_seq)
            offset_days = min(total_days - 2, c_idx * step + random.randint(-2, 2))
            checkin_date = (start_dt + timedelta(days=max(1, offset_days))).strftime("%Y-%m-%d")

            # Progress snapshot increases towards final_prog
            prog_ratio = (c_idx / num_checkins)
            snapshot_val = Decimal(str(final_prog * prog_ratio)).quantize(
                Decimal("0.01"), rounding=ROUND_HALF_UP
            )

            note = CHECKIN_NOTES[(c_idx - 1) % len(CHECKIN_NOTES)]

            checkin_record = {
                "business_id": cin_id,
                "goal_business_id": g_id,
                "employee_business_id": emp_id,
                "notes": f"Sprint {c_idx} update: {note}",
                "progress_snapshot": str(snapshot_val),
                "check_in_date": checkin_date,
            }
            check_ins.append(checkin_record)

            # Generate Evidence with probability
            if random.random() < scale_config.evidence_probability:
                ev_id = id_generator.evidence_id(evidence_seq)
                ev_template = random.choice(EVIDENCE_TEMPLATES)
                evidence_record = {
                    "business_id": ev_id,
                    "check_in_business_id": cin_id,
                    "title": ev_template[0],
                    "url": ev_template[1],
                    "description": f"Verification artifact for {cin_id} progress milestone.",
                    "evidence_type": ev_template[2],
                }
                evidence_list.append(evidence_record)
                evidence_seq += 1

            checkin_seq += 1

    return check_ins, evidence_list
