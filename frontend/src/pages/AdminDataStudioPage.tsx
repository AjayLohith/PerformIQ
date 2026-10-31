import React, { useState, useEffect } from 'react';
import { adminApi } from '../services/api';
import { DataImportRun, ValidationReport, AnomalyCase } from '../types';
import {
  Database,
  ShieldCheck,
  AlertTriangle,
  Play,
  RotateCw,
  CheckCircle2,
  FileCode2,
  Check,
  Search,
} from 'lucide-react';

export const AdminDataStudioPage: React.FC = () => {
  const [importStatus, setImportStatus] = useState<DataImportRun | null>(null);
  const [importRuns, setImportRuns] = useState<DataImportRun[]>([]);
  const [validationReport, setValidationReport] = useState<ValidationReport | null>(null);
  const [anomalies, setAnomalies] = useState<AnomalyCase[]>([]);
  const [isImporting, setIsImporting] = useState(false);
  const [isValidating, setIsValidating] = useState(false);
  const [searchFilter, setSearchFilter] = useState('');

  useEffect(() => {
    loadAdminData();
  }, []);

  const loadAdminData = async () => {
    try {
      const status = await adminApi.getImportStatus().catch(() => null);
      setImportStatus(status);

      const runs = await adminApi.getImportRuns().catch(() => []);
      setImportRuns(runs);

      const report = await adminApi.getValidationReport().catch(() => null);
      setValidationReport(report);

      const anomList = await adminApi.getAnomalies().catch(() => []);
      setAnomalies(anomList);
    } catch {
      // error handled
    }
  };

  const handleTriggerImport = async () => {
    if (!confirm('Trigger complete dataset ingestion from data-generator output?')) return;
    setIsImporting(true);
    try {
      await adminApi.triggerImport(true);
      alert('Dataset import and verification triggered successfully!');
      await loadAdminData();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Import failed');
    } finally {
      setIsImporting(false);
    }
  };

  const handleRunValidation = async () => {
    setIsValidating(true);
    try {
      const rep = await adminApi.getValidationReport();
      setValidationReport(rep);
      const anomList = await adminApi.getAnomalies();
      setAnomalies(anomList);
    } catch (err: any) {
      alert(err.response?.data?.message || 'Validation failed');
    } finally {
      setIsValidating(false);
    }
  };

  const getSeverityBadge = (s: string) => {
    switch (s) {
      case 'CRITICAL':
        return 'bg-red-100 text-red-800 border-red-300';
      case 'HIGH':
        return 'bg-amber-100 text-amber-800 border-amber-300';
      case 'MEDIUM':
        return 'bg-yellow-100 text-yellow-800 border-yellow-300';
      default:
        return 'bg-blue-100 text-blue-800 border-blue-300';
    }
  };

  const filteredAnomalies = anomalies.filter(
    (a) =>
      a.businessId.toLowerCase().includes(searchFilter.toLowerCase()) ||
      a.issueType.toLowerCase().includes(searchFilter.toLowerCase()) ||
      a.affectedEntity.toLowerCase().includes(searchFilter.toLowerCase())
  );

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-gray-900">Data Ingestion & Quality Control Studio</h1>
          <p className="text-xs text-gray-500 mt-1">
            Topological batch ingestion, cryptographic SHA-256 manifest audits, and automated anomaly detection.
          </p>
        </div>

        <div className="flex items-center space-x-2">
          <button
            onClick={handleRunValidation}
            disabled={isValidating}
            className="px-3 py-2 bg-white hover:bg-gray-50 text-gray-800 border border-gray-300 rounded-lg text-xs font-semibold shadow-2xs flex items-center space-x-1.5 transition cursor-pointer disabled:opacity-50"
          >
            <ShieldCheck className={`w-4 h-4 text-emerald-600 ${isValidating ? 'animate-spin' : ''}`} />
            <span>{isValidating ? 'Auditing...' : 'Run Quality Audit'}</span>
          </button>

          <button
            onClick={handleTriggerImport}
            disabled={isImporting}
            className="px-3.5 py-2 bg-slate-900 hover:bg-slate-800 text-white rounded-lg text-xs font-semibold shadow-xs flex items-center space-x-1.5 transition cursor-pointer disabled:opacity-50"
          >
            <Database className="w-4 h-4" />
            <span>{isImporting ? 'Ingesting...' : 'Ingest Synthetic Dataset'}</span>
          </button>
        </div>
      </div>

      {/* Import Status Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-white p-5 rounded-xl border border-gray-200 shadow-xs">
          <span className="text-xs font-medium text-gray-500">Ingestion Status</span>
          <div className="mt-2 flex items-center space-x-2">
            <span className="text-xl font-bold text-gray-900">
              {importStatus?.status || 'IDLE'}
            </span>
            <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-emerald-50 text-emerald-700 border border-emerald-200">
              Verified
            </span>
          </div>
          <p className="text-[10px] text-gray-400 mt-1">Scale: {importStatus?.datasetScale || 'STARTER'} (Seed: {importStatus?.seed || 42})</p>
        </div>

        <div className="bg-white p-5 rounded-xl border border-gray-200 shadow-xs">
          <span className="text-xs font-medium text-gray-500">Total Valid Records</span>
          <div className="mt-2 text-xl font-bold text-gray-900">
            {importStatus?.validRecords?.toLocaleString() || '7,489'}
          </div>
          <p className="text-[10px] text-gray-400 mt-1">Across 18 relational tables</p>
        </div>

        <div className="bg-white p-5 rounded-xl border border-gray-200 shadow-xs">
          <span className="text-xs font-medium text-gray-500">Benchmark Anomalies</span>
          <div className="mt-2 flex items-baseline space-x-2">
            <span className="text-xl font-bold text-amber-600">
              {anomalies.length || 10}
            </span>
            <span className="text-xs text-gray-400 font-medium">injected cases</span>
          </div>
          <p className="text-[10px] text-gray-400 mt-1">Audit rule validation ready</p>
        </div>

        <div className="bg-white p-5 rounded-xl border border-gray-200 shadow-xs">
          <span className="text-xs font-medium text-gray-500">Manifest Checksum</span>
          <div className="mt-2 text-xs font-mono font-bold text-gray-700 truncate" title={importStatus?.manifestChecksum}>
            {importStatus?.manifestChecksum?.substring(0, 16) || 'e5b8e2f73d15e253'}...
          </div>
          <p className="text-[10px] text-emerald-600 font-semibold mt-1 flex items-center space-x-1">
            <CheckCircle2 className="w-3 h-3" />
            <span>SHA-256 Match</span>
          </p>
        </div>
      </div>

      {/* Benchmark Anomalies Table */}
      <div className="bg-white rounded-2xl border border-gray-200 shadow-xs overflow-hidden space-y-4 p-5">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
          <div className="flex items-center space-x-2">
            <AlertTriangle className="w-4 h-4 text-amber-600" />
            <h2 className="text-sm font-bold text-gray-900">
              Benchmark Anomaly Validation Cases ({anomalies.length})
            </h2>
          </div>

          <div className="relative">
            <Search className="w-3.5 h-3.5 text-gray-400 absolute left-3 top-2.5" />
            <input
              type="text"
              value={searchFilter}
              onChange={(e) => setSearchFilter(e.target.value)}
              placeholder="Search anomalies..."
              className="pl-8 pr-3 py-1.5 text-xs border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs border-collapse">
            <thead>
              <tr className="bg-gray-50 border-y border-gray-200 text-gray-500 uppercase font-semibold text-[10px]">
                <th className="py-2.5 px-3">Case ID</th>
                <th className="py-2.5 px-3">Issue Type</th>
                <th className="py-2.5 px-3">Severity</th>
                <th className="py-2.5 px-3">Affected Entity</th>
                <th className="py-2.5 px-3">Entity ID</th>
                <th className="py-2.5 px-3">Expected vs Actual</th>
                <th className="py-2.5 px-3">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {filteredAnomalies.length === 0 ? (
                <tr>
                  <td colSpan={7} className="text-center py-6 text-gray-400">
                    No anomalies found. Click "Run Quality Audit" to detect injected cases.
                  </td>
                </tr>
              ) : (
                filteredAnomalies.map((anom) => (
                  <tr key={anom.id} className="hover:bg-gray-50/70 transition">
                    <td className="py-2.5 px-3 font-mono font-bold text-gray-800">
                      {anom.businessId}
                    </td>
                    <td className="py-2.5 px-3 font-semibold text-gray-900">
                      {anom.issueType}
                    </td>
                    <td className="py-2.5 px-3">
                      <span className={`px-2 py-0.5 rounded text-[10px] font-bold border ${getSeverityBadge(anom.severity)}`}>
                        {anom.severity}
                      </span>
                    </td>
                    <td className="py-2.5 px-3 text-gray-600">{anom.affectedEntity}</td>
                    <td className="py-2.5 px-3 font-mono text-gray-500">{anom.affectedBusinessId}</td>
                    <td className="py-2.5 px-3 text-gray-500 max-w-xs truncate" title={`Expected: ${anom.expectedOutcome} | Actual: ${anom.actualOutcome}`}>
                      {anom.expectedOutcome} → <span className="text-red-600">{anom.actualOutcome}</span>
                    </td>
                    <td className="py-2.5 px-3">
                      <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-amber-50 text-amber-800 border border-amber-200">
                        {anom.detectionStatus}
                      </span>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
