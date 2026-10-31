"""
PerformIQ - Synthetic Data Generator CLI
Entry point for generating deterministic, reproducible employee performance datasets.
"""

import argparse
import os
import random
import sys
from faker import Faker

from . import (
    config,
    employee_generator,
    review_cycle_generator,
    goal_generator,
    checkin_generator,
    feedback_generator,
    review_generator,
    development_plan_generator,
    anomaly_injector,
    exporter,
)


def run_generator(
    scale: str = "starter",
    seed: int = 42,
    export_format: str = "csv",
    output_dir: str = "output",
    inject_anomalies: bool = True,
) -> None:
    print("=" * 65)
    print(f"PerformIQ Synthetic Data Generator (Version 1.0.0)")
    print(f"Scale: {scale.upper()} | Seed: {seed} | Format: {export_format.upper()}")
    print(f"Target Directory: {os.path.abspath(output_dir)}")
    print("=" * 65)

    scale_cfg = config.SCALES.get(scale.lower())
    if not scale_cfg:
        raise ValueError(f"Unknown scale: {scale}. Choose 'starter' or 'full'.")

    # Initialize seeds for reproducibility
    random.seed(seed)
    Faker.seed(seed)
    fake = Faker()

    # 1. Employees & Hierarchies
    print(f"\n[1/7] Generating {scale_cfg.num_employees} employees and managerial reporting hierarchy...")
    employees, managers, assignments, users = employee_generator.generate_employees_and_hierarchy(
        fake, scale_cfg.num_employees
    )
    print(f"      [OK] {len(employees)} Employees | {len(managers)} Managers | {len(assignments)} Assignments | {len(users)} Users")

    # 2. Review Cycles
    print(f"[2/7] Generating {scale_cfg.num_cycles} review cycles...")
    cycles = review_cycle_generator.generate_review_cycles(scale_cfg.num_cycles)
    cycles_map = {c["business_id"]: c for c in cycles}
    print(f"      [OK] {len(cycles)} Cycles: {', '.join(c['business_id'] for c in cycles)}")

    # 3. Goals & Key Results
    print(f"[3/7] Generating Goals and measurable Key Results...")
    goals, key_results, goal_history, kr_history = goal_generator.generate_goals_and_key_results(
        fake, employees, cycles, scale_cfg
    )
    print(f"      [OK] {len(goals)} Goals | {len(key_results)} Key Results | {len(goal_history)} Goal Logs")

    # 4. Check-ins & Evidence
    print(f"[4/7] Generating periodic Check-ins and supporting Evidence artifacts...")
    checkins, evidence = checkin_generator.generate_checkins_and_evidence(
        fake, goals, cycles_map, scale_cfg
    )
    print(f"      [OK] {len(checkins)} Check-ins | {len(evidence)} Evidence Attachments")

    # 5. Multi-rater Feedbacks
    print(f"[5/7] Generating 360 Feedback (Self, Manager, Peer) and Competency Ratings...")
    feedbacks, fb_competencies = feedback_generator.generate_feedbacks_and_ratings(
        fake, employees, cycles, assignments, scale_cfg
    )
    print(f"      [OK] {len(feedbacks)} Feedbacks | {len(fb_competencies)} Competency Ratings")

    # 6. Formal Reviews & Development Plans
    print(f"[6/7] Generating Performance Reviews and Development Plans...")
    reviews, review_goals = review_generator.generate_reviews_and_scores(
        fake, employees, cycles, goals, assignments
    )
    dev_plans, approval_history = development_plan_generator.generate_development_plans_and_history(
        fake, reviews, assignments, scale_cfg
    )
    print(f"      [OK] {len(reviews)} Reviews | {len(dev_plans)} Development Plans | {len(approval_history)} Approvals")

    # 7. Anomaly Injection
    anomalies = []
    if inject_anomalies:
        print(f"[7/7] Injecting {scale_cfg.anomaly_count} controlled anomalies for validation benchmarking...")
        anomalies = anomaly_injector.inject_anomalies(
            goals=goals,
            key_results=key_results,
            check_ins=checkins,
            feedbacks=feedbacks,
            employees=employees,
            manager_assignments=assignments,
            count=scale_cfg.anomaly_count,
        )
        print(f"      [OK] {len(anomalies)} Anomaly cases injected and labeled")
    else:
        print(f"[7/7] Anomaly injection disabled.")

    # Tables dictionary
    tables = {
        "employees": employees,
        "managers": managers,
        "manager_assignment_history": assignments,
        "app_users": users,
        "review_cycles": cycles,
        "goals": goals,
        "key_results": key_results,
        "goal_history": goal_history,
        "key_result_history": kr_history,
        "check_ins": checkins,
        "evidence": evidence,
        "feedbacks": feedbacks,
        "feedback_competencies": fb_competencies,
        "reviews": reviews,
        "review_goals": review_goals,
        "development_plans": dev_plans,
        "approval_history": approval_history,
        "anomalies": anomalies,
    }

    # Exporting
    print(f"\n[+] Exporting datasets and generating cryptographic manifest...")
    manifest = exporter.export_dataset(
        tables=tables,
        output_dir=output_dir,
        export_format=export_format,
        scale=scale,
        seed=seed,
    )

    print("\n" + "=" * 65)
    print("GENERATION COMPLETE!")
    print(f"   Total Entities Generated : {manifest['total_records']:,}")
    print(f"   Total Files Written     : {len(manifest['files'])}")
    print(f"   Bundle Checksum (SHA256): {manifest['bundle_checksum']}")
    print("=" * 65)
    print("\nRecord Counts by Entity:")
    for name, cnt in manifest["record_counts"].items():
        print(f"  * {name:<28}: {cnt:>6,} records")
    print("\nManifest file created at:", os.path.join(output_dir, "manifest.json"))


def main():
    parser = argparse.ArgumentParser(description="PerformIQ Synthetic Data Generator")
    parser.add_argument(
        "--scale",
        choices=["starter", "full"],
        default="starter",
        help="Dataset scale: 'starter' (~50 employees) or 'full' (~3000 employees)",
    )
    parser.add_argument(
        "--seed",
        type=int,
        default=42,
        help="Random seed for reproducible dataset generation (default: 42)",
    )
    parser.add_argument(
        "--format",
        choices=["csv", "json", "both"],
        default="both",
        help="Output format: 'csv', 'json', or 'both' (default: 'both')",
    )
    parser.add_argument(
        "--output-dir",
        default="output",
        help="Output directory path for generated files (default: 'output')",
    )
    parser.add_argument(
        "--no-anomalies",
        action="store_true",
        help="Disable injection of deliberate data quality anomalies",
    )

    args = parser.parse_args()

    run_generator(
        scale=args.scale,
        seed=args.seed,
        export_format=args.format,
        output_dir=args.output_dir,
        inject_anomalies=not args.no_anomalies,
    )


if __name__ == "__main__":
    main()
