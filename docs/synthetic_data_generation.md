# PerformIQ — Synthetic Data Generation Pipeline

## 1. Overview & Architectural Philosophy

The **Synthetic Data Generator** is an independent, deterministic Python CLI component engineered to populate PerformIQ with realistic enterprise organizational hierarchies, performance cycles, OKRs, check-in histories, feedback rubrics, and formal performance reviews.

### Guiding Principles:
1. **Decoupled Producer**: The generator writes purely to disk (`output/` directory as CSV / JSON). It does NOT write directly to the database. This maintains strict separation of concerns — the Spring Boot backend owns all ingestion, schema validation, foreign key integrity checks, and transactional persistence.
2. **Deterministic Reproducibility**: Using pinned dependencies (`faker==36.1.1`) and explicit seeds (`random.seed(seed)` + `Faker.seed(seed)`), identical execution commands produce identical byte-for-byte datasets and identical bundle checksums.
3. **Cryptographic Manifest**: Every run generates a signed `manifest.json` containing table-by-table record counts, export formats, individual file SHA-256 hashes, and a composite bundle checksum.
4. **Controlled Anomaly Injection**: Injects labeled, reproducible data quality anomalies (orphan references, date boundary violations, formula mismatches, circular reporting) with ground-truth records in `anomalies.csv` to benchmark the backend validation engine.

---

## 2. Generator Scales

| Scale | Target Employees | Review Cycles | Goals | Key Results | Check-ins | Feedbacks | Reviews | Anomaly Cases | Use Case |
|-------|------------------|---------------|-------|-------------|-----------|-----------|---------|---------------|----------|
| **Starter** | 50 | 2 | ~400 | ~1,000 | ~1,600 | ~350 | ~65 | 10 | Rapid local developer iteration, automated integration testing, unit verification |
| **Full** | 3,000 | 3 | ~18,000 | ~45,000 | ~120,000 | ~18,000 | ~6,000 | 50 | Recruiter evaluation, batch ingestion stress testing, performance indexing benchmarks |

---

## 3. Component Architecture

```
data-generator/
├── generator/
│   ├── __init__.py
│   ├── config.py                 # Scale parameters, departments, designations, competencies
│   ├── id_generator.py           # Business ID formatters (EMP-*, GOAL-*, KR-*, etc.)
│   ├── employee_generator.py     # Employee directory, tree hierarchy, manager assignments, user accounts
│   ├── review_cycle_generator.py # Closed historical and active current review cycles
│   ├── goal_generator.py         # OKRs with accurate progress math: Σ(kr_prog * weight) / Σ(weight)
│   ├── checkin_generator.py      # Chronological check-ins and evidence attachments
│   ├── feedback_generator.py     # Self, Manager, and Peer multi-rater rubric feedback
│   ├── review_generator.py       # Formal performance reviews and goal evaluations
│   ├── development_plan_generator.py # Post-review growth plans and approval history
│   ├── anomaly_injector.py       # Controlled, labeled data quality failure injections
│   ├── exporter.py               # CSV/JSON serializers and SHA-256 manifest calculator
│   └── main.py                   # CLI entry point
├── output/                       # Destination directory for CSV/JSON files and manifest.json
├── requirements.txt              # Pinned Python dependencies
└── README.md                     # CLI usage and execution documentation
```

---

## 4. Execution & CLI Options

To run the generator:

```bash
cd data-generator
python -m generator.main --scale starter --seed 42 --format both
```

### CLI Arguments:
- `--scale`: Dataset scale (`starter` or `full`). Default: `starter`.
- `--seed`: Integer seed for random number generation. Default: `42`.
- `--format`: File output format (`csv`, `json`, or `both`). Default: `both`.
- `--output-dir`: Output directory path. Default: `output`.
- `--no-anomalies`: Flag to disable anomaly injection.

---

## 5. Manifest & Verification

Example `manifest.json`:
```json
{
  "manifest_version": "1.0",
  "generator_version": "1.0.0",
  "scale": "STARTER",
  "seed": "42",
  "total_tables": 18,
  "total_records": 7489,
  "bundle_checksum": "c5ec5655d5797ea4706cd8db3eabaf4ee51c5d3f0e5efc76497d14b16e38bec0",
  "record_counts": {
    "employees": 50,
    "managers": 8,
    "goals": 402,
    "key_results": 1017,
    "check_ins": 1635,
    "feedbacks": 356,
    "reviews": 66,
    "development_plans": 48,
    "anomalies": 10
  }
}
```
