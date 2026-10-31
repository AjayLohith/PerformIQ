# PerformIQ — Synthetic Data Generator

This Python component generates deterministic, reproducible synthetic datasets for PerformIQ.

## Prerequisites
- Python 3.12+ / 3.13+ / 3.14+
- `pip install -r requirements.txt`

## Quick Start

### 1. Generate Starter Dataset (~7.5K records)
```bash
python -m generator.main --scale starter --seed 42 --format both
```

### 2. Generate Full Scale Dataset (~150K records)
```bash
python -m generator.main --scale full --seed 42 --format csv
```

### 3. Generate Clean Dataset Without Injected Anomalies
```bash
python -m generator.main --scale starter --seed 42 --format csv --no-anomalies
```

## Generated Files (`output/`)
- `employees.csv` / `.json`
- `managers.csv` / `.json`
- `manager_assignment_history.csv` / `.json`
- `app_users.csv` / `.json`
- `review_cycles.csv` / `.json`
- `goals.csv` / `.json`
- `key_results.csv` / `.json`
- `goal_history.csv` / `.json`
- `key_result_history.csv` / `.json`
- `check_ins.csv` / `.json`
- `evidence.csv` / `.json`
- `feedbacks.csv` / `.json`
- `feedback_competencies.csv` / `.json`
- `reviews.csv` / `.json`
- `review_goals.csv` / `.json`
- `development_plans.csv` / `.json`
- `approval_history.csv` / `.json`
- `anomalies.csv` / `.json`
- `manifest.json` (Cryptographic verification manifest with SHA-256 hashes)
