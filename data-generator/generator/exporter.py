"""
PerformIQ - Dataset Exporter & Cryptographic Manifest Generator
Exports tabular collections to CSV/JSON and creates tamper-evident SHA-256 manifests.
"""

import csv
import hashlib
import json
import os
from datetime import datetime, timezone
from typing import Any, Dict, List


def compute_sha256(filepath: str) -> str:
    hasher = hashlib.sha256()
    with open(filepath, "rb") as f:
        while chunk := f.read(65536):
            hasher.update(chunk)
    return hasher.hexdigest()


def export_to_csv(data: List[Dict[str, Any]], filepath: str) -> None:
    if not data:
        with open(filepath, "w", newline="", encoding="utf-8") as f:
            pass
        return

    fieldnames = list(data[0].keys())
    with open(filepath, "w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(data)


def export_to_json(data: List[Dict[str, Any]], filepath: str) -> None:
    with open(filepath, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)


def export_dataset(
    tables: Dict[str, List[Dict[str, Any]]],
    output_dir: str,
    export_format: str,
    scale: str,
    seed: int,
    generator_version: str = "1.0.0",
) -> Dict[str, Any]:
    """
    Exports all dataset tables and returns the generated manifest dictionary.
    """
    os.makedirs(output_dir, exist_ok=True)

    file_metadata: List[Dict[str, Any]] = []
    record_counts: Dict[str, int] = {}

    for table_name, records in tables.items():
        count = len(records)
        record_counts[table_name] = count

        if export_format in ["csv", "both"]:
            csv_path = os.path.join(output_dir, f"{table_name}.csv")
            export_to_csv(records, csv_path)
            checksum = compute_sha256(csv_path)
            file_metadata.append(
                {
                    "file_name": f"{table_name}.csv",
                    "format": "csv",
                    "record_count": count,
                    "sha256": checksum,
                }
            )

        if export_format in ["json", "both"]:
            json_path = os.path.join(output_dir, f"{table_name}.json")
            export_to_json(records, json_path)
            checksum = compute_sha256(json_path)
            file_metadata.append(
                {
                    "file_name": f"{table_name}.json",
                    "format": "json",
                    "record_count": count,
                    "sha256": checksum,
                }
            )

    # Compute overall bundle checksum from all sorted file hashes
    combined_hashes = "".join(f["sha256"] for f in sorted(file_metadata, key=lambda x: x["file_name"]))
    manifest_checksum = hashlib.sha256(combined_hashes.encode("utf-8")).hexdigest()

    manifest = {
        "manifest_version": "1.0",
        "generator_version": generator_version,
        "scale": scale.upper(),
        "seed": str(seed),
        "generated_at": datetime.now(timezone.utc).isoformat(),
        "total_tables": len(tables),
        "total_records": sum(record_counts.values()),
        "record_counts": record_counts,
        "files": file_metadata,
        "bundle_checksum": manifest_checksum,
    }

    manifest_path = os.path.join(output_dir, "manifest.json")
    with open(manifest_path, "w", encoding="utf-8") as f:
        json.dump(manifest, f, indent=2, ensure_ascii=False)

    return manifest
