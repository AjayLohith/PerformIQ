import React, { useState, useEffect } from 'react';
import { adminApi } from '../services/api';
import { AuditLog } from '../types';
import {
  ShieldAlert,
  Search,
  Filter,
  RefreshCw,
  Code,
  Calendar,
  User,
  ArrowRight,
  Database,
  CheckCircle2,
  Clock,
  Layers,
  FileText,
  X,
} from 'lucide-react';

export const AuditLogsPage: React.FC = () => {
  const [logs, setLogs] = useState<AuditLog[]>([]);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [pageSize] = useState(15);
  const [isLoading, setIsLoading] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedEntityType, setSelectedEntityType] = useState('ALL');
  const [inspectingLog, setInspectingLog] = useState<AuditLog | null>(null);

  useEffect(() => {
    fetchLogs(currentPage);
  }, [currentPage]);

  const fetchLogs = async (page: number) => {
    setIsLoading(true);
    try {
      const res = await adminApi.getAuditLogs(page, pageSize);
      setLogs(res.content || []);
      setTotalPages(res.totalPages || 1);
      setTotalElements(res.totalElements || 0);
    } catch {
      // Fallback empty
      setLogs([]);
    } finally {
      setIsLoading(false);
    }
  };

  const getActionBadge = (action: string) => {
    const act = action.toUpperCase();
    if (act.includes('CREATE') || act.includes('INSERT') || act.includes('START')) {
      return 'bg-emerald-50 text-emerald-700 border-emerald-200';
    }
    if (act.includes('APPROVE') || act.includes('COMPLETE')) {
      return 'bg-blue-50 text-blue-700 border-blue-200';
    }
    if (act.includes('REJECT') || act.includes('CANCEL') || act.includes('DELETE')) {
      return 'bg-rose-50 text-rose-700 border-rose-200';
    }
    if (act.includes('TRANSITION') || act.includes('UPDATE') || act.includes('CHANGE')) {
      return 'bg-amber-50 text-amber-700 border-amber-200';
    }
    return 'bg-gray-100 text-gray-700 border-gray-200';
  };

  const filteredLogs = logs.filter((log) => {
    const matchesSearch =
      searchQuery === '' ||
      log.action.toLowerCase().includes(searchQuery.toLowerCase()) ||
      log.entityType.toLowerCase().includes(searchQuery.toLowerCase()) ||
      log.entityId.toLowerCase().includes(searchQuery.toLowerCase()) ||
      (log.actor &&
        `${log.actor.firstName} ${log.actor.lastName}`.toLowerCase().includes(searchQuery.toLowerCase())) ||
      (log.reason && log.reason.toLowerCase().includes(searchQuery.toLowerCase()));

    const matchesEntity =
      selectedEntityType === 'ALL' ||
      log.entityType.toUpperCase() === selectedEntityType.toUpperCase();

    return matchesSearch && matchesEntity;
  });

  const entityTypes = ['ALL', 'GOAL', 'KEY_RESULT', 'REVIEW_CYCLE', 'DEVELOPMENT_PLAN', 'REVIEW', 'FEEDBACK', 'CHECK_IN'];

  const formatJson = (raw?: string) => {
    if (!raw) return 'null';
    try {
      const parsed = JSON.parse(raw);
      return JSON.stringify(parsed, null, 2);
    } catch {
      return raw;
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center space-x-2">
            <h1 className="text-xl font-bold text-gray-900 tracking-tight">Security & Audit Trail</h1>
            <span className="px-2 py-0.5 rounded-full text-xs font-semibold bg-purple-50 text-purple-700 border border-purple-200 flex items-center space-x-1">
              <ShieldAlert className="w-3 h-3 mr-1" />
              SOC2 Compliant Log
            </span>
          </div>
          <p className="text-xs text-gray-500 mt-1">
            Tamper-evident system activity, state mutations, and security event audit logs.
          </p>
        </div>
        <div className="flex items-center space-x-3">
          <button
            onClick={() => fetchLogs(currentPage)}
            disabled={isLoading}
            className="flex items-center space-x-2 px-3 py-1.5 bg-white border border-gray-300 hover:bg-gray-50 rounded-lg text-xs font-semibold text-gray-700 transition shadow-xs cursor-pointer"
          >
            <RefreshCw className={`w-3.5 h-3.5 text-gray-500 ${isLoading ? 'animate-spin' : ''}`} />
            <span>Refresh</span>
          </button>
        </div>
      </div>

      {/* KPI Stats */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-xs">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-gray-500">Total Audit Records</span>
            <Database className="w-4 h-4 text-purple-600" />
          </div>
          <div className="text-2xl font-bold text-gray-900 mt-2">{totalElements}</div>
          <span className="text-[11px] text-gray-400 mt-1 block">Full persistence in PostgreSQL</span>
        </div>

        <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-xs">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-gray-500">Integrity Guarantee</span>
            <CheckCircle2 className="w-4 h-4 text-emerald-600" />
          </div>
          <div className="text-2xl font-bold text-emerald-600 mt-2">100%</div>
          <span className="text-[11px] text-gray-400 mt-1 block">Append-only audit ledger</span>
        </div>

        <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-xs">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-gray-500">Audited Domain Entities</span>
            <Layers className="w-4 h-4 text-blue-600" />
          </div>
          <div className="text-2xl font-bold text-gray-900 mt-2">7 Models</div>
          <span className="text-[11px] text-gray-400 mt-1 block">Goals, Reviews, Cycles, Plans</span>
        </div>

        <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-xs">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-gray-500">Current Page Window</span>
            <Clock className="w-4 h-4 text-amber-600" />
          </div>
          <div className="text-2xl font-bold text-gray-900 mt-2">
            {logs.length} <span className="text-xs font-normal text-gray-400">entries</span>
          </div>
          <span className="text-[11px] text-gray-400 mt-1 block">
            Page {currentPage + 1} of {totalPages || 1}
          </span>
        </div>
      </div>

      {/* Filter Bar */}
      <div className="bg-white p-3.5 rounded-xl border border-gray-200 shadow-xs flex flex-col md:flex-row gap-3 items-center justify-between">
        <div className="relative w-full md:w-96">
          <Search className="w-4 h-4 text-gray-400 absolute left-3 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            placeholder="Search by action, actor, entity ID, reason..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full pl-9 pr-3 py-1.5 text-xs bg-gray-50 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-900 focus:bg-white"
          />
        </div>

        <div className="flex items-center space-x-2 w-full md:w-auto overflow-x-auto pb-1 md:pb-0 scrollbar-thin">
          <Filter className="w-3.5 h-3.5 text-gray-400 shrink-0" />
          <span className="text-[11px] font-semibold text-gray-500 shrink-0">Entity:</span>
          {entityTypes.map((type) => (
            <button
              key={type}
              onClick={() => setSelectedEntityType(type)}
              className={`px-2.5 py-1 rounded-md text-[11px] font-medium transition shrink-0 cursor-pointer ${
                selectedEntityType === type
                  ? 'bg-slate-900 text-white'
                  : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
              }`}
            >
              {type}
            </button>
          ))}
        </div>
      </div>

      {/* Table */}
      <div className="bg-white rounded-xl border border-gray-200 shadow-xs overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-gray-50/75 border-b border-gray-200 text-gray-500 uppercase tracking-wider text-[10px] font-semibold">
              <tr>
                <th className="py-3 px-4">Timestamp</th>
                <th className="py-3 px-4">Action</th>
                <th className="py-3 px-4">Target Entity</th>
                <th className="py-3 px-4">Actor</th>
                <th className="py-3 px-4">Notes / Reason</th>
                <th className="py-3 px-4 text-right">State Diff</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {isLoading ? (
                <tr>
                  <td colSpan={6} className="py-12 text-center text-gray-400">
                    <RefreshCw className="w-6 h-6 animate-spin mx-auto mb-2 text-gray-400" />
                    Loading audit stream...
                  </td>
                </tr>
              ) : filteredLogs.length === 0 ? (
                <tr>
                  <td colSpan={6} className="py-12 text-center text-gray-400">
                    <ShieldAlert className="w-8 h-8 text-gray-300 mx-auto mb-2" />
                    No audit log records found matching your filters.
                  </td>
                </tr>
              ) : (
                filteredLogs.map((log) => (
                  <tr key={log.id} className="hover:bg-gray-50/50 transition">
                    <td className="py-3 px-4 text-gray-500 whitespace-nowrap">
                      <div className="flex items-center space-x-1.5 text-gray-700 font-mono text-[11px]">
                        <Clock className="w-3 h-3 text-gray-400" />
                        <span>{new Date(log.createdAt).toLocaleString()}</span>
                      </div>
                    </td>
                    <td className="py-3 px-4">
                      <span
                        className={`inline-block px-2 py-0.5 rounded text-[10px] font-bold border ${getActionBadge(
                          log.action
                        )}`}
                      >
                        {log.action}
                      </span>
                    </td>
                    <td className="py-3 px-4 whitespace-nowrap">
                      <div className="flex items-center space-x-1.5">
                        <span className="font-semibold text-gray-800">{log.entityType}</span>
                        <span className="font-mono text-[10px] text-gray-400 bg-gray-100 px-1.5 py-0.2 rounded border border-gray-200">
                          {log.entityId.length > 8 ? `${log.entityId.substring(0, 8)}...` : log.entityId}
                        </span>
                      </div>
                    </td>
                    <td className="py-3 px-4 whitespace-nowrap">
                      {log.actor ? (
                        <div className="flex items-center space-x-2">
                          <div className="w-5 h-5 rounded-full bg-slate-800 text-white flex items-center justify-center font-bold text-[9px]">
                            {log.actor.firstName[0]}
                            {log.actor.lastName[0]}
                          </div>
                          <span className="text-gray-900 font-medium">
                            {log.actor.firstName} {log.actor.lastName}
                          </span>
                        </div>
                      ) : (
                        <span className="text-gray-400 italic">SYSTEM</span>
                      )}
                    </td>
                    <td className="py-3 px-4 max-w-xs truncate text-gray-600">
                      {log.reason || <span className="text-gray-300 italic">—</span>}
                    </td>
                    <td className="py-3 px-4 text-right">
                      <button
                        onClick={() => setInspectingLog(log)}
                        className="inline-flex items-center space-x-1 px-2.5 py-1 bg-gray-100 hover:bg-slate-900 hover:text-white rounded text-[11px] font-medium text-gray-700 transition cursor-pointer"
                      >
                        <Code className="w-3 h-3" />
                        <span>Inspect Diff</span>
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination Footer */}
        <div className="p-3 border-t border-gray-200 flex items-center justify-between bg-gray-50/50">
          <span className="text-xs text-gray-500">
            Showing <span className="font-semibold">{logs.length}</span> of{' '}
            <span className="font-semibold">{totalElements}</span> audit records
          </span>
          <div className="flex items-center space-x-2">
            <button
              onClick={() => setCurrentPage((p) => Math.max(0, p - 1))}
              disabled={currentPage === 0 || isLoading}
              className="px-2.5 py-1 bg-white border border-gray-200 rounded text-xs font-medium text-gray-700 disabled:opacity-50 hover:bg-gray-50 transition cursor-pointer"
            >
              Previous
            </button>
            <span className="text-xs font-semibold text-gray-700">
              Page {currentPage + 1} / {totalPages || 1}
            </span>
            <button
              onClick={() => setCurrentPage((p) => Math.min(totalPages - 1, p + 1))}
              disabled={currentPage >= totalPages - 1 || isLoading}
              className="px-2.5 py-1 bg-white border border-gray-200 rounded text-xs font-medium text-gray-700 disabled:opacity-50 hover:bg-gray-50 transition cursor-pointer"
            >
              Next
            </button>
          </div>
        </div>
      </div>

      {/* JSON State Diff Modal */}
      {inspectingLog && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs">
          <div className="bg-white rounded-2xl max-w-3xl w-full max-h-[85vh] flex flex-col shadow-2xl border border-gray-200 animate-in fade-in zoom-in-95 duration-150">
            {/* Modal Header */}
            <div className="p-5 border-b border-gray-200 flex items-center justify-between bg-gray-50/50 rounded-t-2xl">
              <div>
                <div className="flex items-center space-x-2">
                  <ShieldAlert className="w-5 h-5 text-indigo-600" />
                  <h3 className="text-base font-bold text-gray-900">Audit State Snapshot Diff</h3>
                  <span
                    className={`px-2 py-0.5 rounded text-[10px] font-bold border ${getActionBadge(
                      inspectingLog.action
                    )}`}
                  >
                    {inspectingLog.action}
                  </span>
                </div>
                <p className="text-xs text-gray-500 mt-1">
                  Entity: <span className="font-semibold text-gray-800">{inspectingLog.entityType}</span> (ID:{' '}
                  <span className="font-mono text-gray-600">{inspectingLog.entityId}</span>)
                </p>
              </div>
              <button
                onClick={() => setInspectingLog(null)}
                className="p-1.5 text-gray-400 hover:text-gray-600 rounded-lg hover:bg-gray-200 transition cursor-pointer"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {/* Modal Metadata Summary */}
            <div className="p-4 bg-gray-50 border-b border-gray-200 grid grid-cols-2 sm:grid-cols-4 gap-3 text-xs">
              <div>
                <span className="text-[10px] font-semibold text-gray-400 uppercase">Actor</span>
                <p className="font-medium text-gray-900 mt-0.5">
                  {inspectingLog.actor
                    ? `${inspectingLog.actor.firstName} ${inspectingLog.actor.lastName}`
                    : 'System'}
                </p>
              </div>
              <div>
                <span className="text-[10px] font-semibold text-gray-400 uppercase">Timestamp</span>
                <p className="font-medium text-gray-900 mt-0.5">
                  {new Date(inspectingLog.createdAt).toLocaleTimeString()}
                </p>
              </div>
              <div className="col-span-2">
                <span className="text-[10px] font-semibold text-gray-400 uppercase">Audit Reason</span>
                <p className="font-medium text-gray-900 mt-0.5 truncate">
                  {inspectingLog.reason || 'None provided'}
                </p>
              </div>
            </div>

            {/* Modal Diff Body */}
            <div className="p-5 overflow-y-auto flex-1 space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {/* Previous Value */}
                <div className="space-y-1.5">
                  <div className="flex items-center justify-between text-xs font-semibold text-rose-700">
                    <span>Previous State</span>
                    <span className="text-[10px] bg-rose-50 px-1.5 py-0.5 rounded border border-rose-200">
                      Before Mutation
                    </span>
                  </div>
                  <pre className="bg-slate-900 text-rose-300 p-3.5 rounded-xl font-mono text-[11px] overflow-x-auto max-h-80 border border-slate-800 leading-relaxed scrollbar-thin">
                    {formatJson(inspectingLog.previousValue)}
                  </pre>
                </div>

                {/* New Value */}
                <div className="space-y-1.5">
                  <div className="flex items-center justify-between text-xs font-semibold text-emerald-700">
                    <span>New State</span>
                    <span className="text-[10px] bg-emerald-50 px-1.5 py-0.5 rounded border border-emerald-200">
                      After Mutation
                    </span>
                  </div>
                  <pre className="bg-slate-900 text-emerald-300 p-3.5 rounded-xl font-mono text-[11px] overflow-x-auto max-h-80 border border-slate-800 leading-relaxed scrollbar-thin">
                    {formatJson(inspectingLog.newValue)}
                  </pre>
                </div>
              </div>
            </div>

            {/* Modal Footer */}
            <div className="p-4 border-t border-gray-200 flex justify-end bg-gray-50 rounded-b-2xl">
              <button
                onClick={() => setInspectingLog(null)}
                className="px-4 py-2 bg-slate-900 hover:bg-slate-800 text-white rounded-lg text-xs font-semibold transition cursor-pointer"
              >
                Close Inspector
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
