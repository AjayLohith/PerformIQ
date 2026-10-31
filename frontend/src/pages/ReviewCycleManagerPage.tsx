import React, { useState, useEffect } from 'react';
import { reviewCycleApi } from '../services/api';
import { ReviewCycle, ReviewCycleStatus } from '../types';
import {
  CalendarCheck2,
  Plus,
  ArrowRight,
  CheckCircle,
  AlertCircle,
  Clock,
  Lock,
  Layers,
} from 'lucide-react';

export const ReviewCycleManagerPage: React.FC = () => {
  const [cycles, setCycles] = useState<ReviewCycle[]>([]);
  const [activeCycle, setActiveCycle] = useState<ReviewCycle | null>(null);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Form states
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [startDate, setStartDate] = useState('2026-07-01');
  const [endDate, setEndDate] = useState('2026-12-31');

  useEffect(() => {
    loadCycles();
  }, []);

  const loadCycles = async () => {
    try {
      const pageRes = await reviewCycleApi.getReviewCycles();
      setCycles(pageRes.content);
      const active = pageRes.content.find((c) => c.status === 'ACTIVE') || null;
      setActiveCycle(active);
    } catch {
      // error handled
    }
  };

  const getNextStatus = (current: ReviewCycleStatus): ReviewCycleStatus | null => {
    switch (current) {
      case 'DRAFT':
        return 'ACTIVE';
      case 'ACTIVE':
        return 'UNDER_REVIEW';
      case 'UNDER_REVIEW':
        return 'CLOSED';
      default:
        return null;
    }
  };

  const handleAdvanceStatus = async (cycle: ReviewCycle) => {
    const nextStatus = getNextStatus(cycle.status);
    if (!nextStatus) return;

    const confirmed = confirm(
      `Advance Review Cycle '${cycle.name}' from ${cycle.status} to ${nextStatus}?\n\n` +
      (nextStatus === 'ACTIVE' ? 'Note: DRAFT employee goals will be locked into ACTIVE state.' : '')
    );
    if (!confirmed) return;

    try {
      await reviewCycleApi.transitionStatus(cycle.id, nextStatus, 'Manual admin state advance');
      await loadCycles();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Transition failed');
    }
  };

  const handleCreateCycle = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
    try {
      await reviewCycleApi.createCycle({
        name,
        description,
        startDate,
        endDate,
      });
      setShowCreateModal(false);
      setName('');
      setDescription('');
      await loadCycles();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to create review cycle');
    } finally {
      setIsSubmitting(false);
    }
  };

  const states: ReviewCycleStatus[] = ['DRAFT', 'ACTIVE', 'UNDER_REVIEW', 'CLOSED'];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-gray-900">Review Cycle State Machine Control</h1>
          <p className="text-xs text-gray-500 mt-1">
            Orchestrate enterprise performance review cycles with atomic state machine transitions.
          </p>
        </div>

        <button
          onClick={() => setShowCreateModal(true)}
          className="px-3.5 py-2 bg-slate-900 hover:bg-slate-800 text-white rounded-lg text-xs font-semibold shadow-xs flex items-center space-x-1.5 transition cursor-pointer"
        >
          <Plus className="w-4 h-4" />
          <span>New Review Cycle</span>
        </button>
      </div>

      {/* State Machine Visualizer for Active/Selected Cycle */}
      {activeCycle && (
        <div className="bg-white p-6 rounded-2xl border border-gray-200 shadow-xs space-y-4">
          <div className="flex items-center justify-between pb-3 border-b border-gray-100">
            <div>
              <span className="text-[10px] font-bold text-gray-400 uppercase tracking-wider">
                Active Cycle Orchestration
              </span>
              <h2 className="text-base font-bold text-gray-900">{activeCycle.name}</h2>
              <p className="text-xs text-gray-500">
                {activeCycle.startDate} to {activeCycle.endDate}
              </p>
            </div>

            {getNextStatus(activeCycle.status) && (
              <button
                onClick={() => handleAdvanceStatus(activeCycle)}
                className="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl text-xs font-bold shadow-xs flex items-center space-x-2 transition cursor-pointer"
              >
                <span>Advance to {getNextStatus(activeCycle.status)}</span>
                <ArrowRight className="w-4 h-4" />
              </button>
            )}
          </div>

          {/* Stepper Pipeline */}
          <div className="grid grid-cols-4 gap-2 pt-2">
            {states.map((st, idx) => {
              const isCurrent = activeCycle.status === st;
              const isPassed = states.indexOf(activeCycle.status) > idx;

              return (
                <div
                  key={st}
                  className={`p-3.5 rounded-xl border text-center transition ${
                    isCurrent
                      ? 'bg-indigo-50 border-indigo-300 ring-2 ring-indigo-400/20'
                      : isPassed
                      ? 'bg-emerald-50/70 border-emerald-200 text-emerald-800'
                      : 'bg-gray-50 border-gray-200 text-gray-400'
                  }`}
                >
                  <div className="flex items-center justify-center mb-1">
                    {isPassed ? (
                      <CheckCircle className="w-4 h-4 text-emerald-600" />
                    ) : isCurrent ? (
                      <Clock className="w-4 h-4 text-indigo-600 animate-pulse" />
                    ) : (
                      <Lock className="w-4 h-4 text-gray-300" />
                    )}
                  </div>
                  <span className={`text-xs font-bold block ${isCurrent ? 'text-indigo-900' : ''}`}>
                    {st}
                  </span>
                  <span className="text-[10px] text-gray-500 block mt-0.5">
                    {st === 'DRAFT'
                      ? 'Goal Setup'
                      : st === 'ACTIVE'
                      ? 'Execution'
                      : st === 'UNDER_REVIEW'
                      ? '360 Reviews'
                      : 'Archived'}
                  </span>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* Review Cycles Table */}
      <div className="bg-white rounded-2xl border border-gray-200 shadow-xs overflow-hidden">
        <div className="p-4 border-b border-gray-100 flex items-center justify-between">
          <h3 className="text-xs font-bold text-gray-900 uppercase tracking-wider">
            All Configured Review Cycles
          </h3>
          <span className="text-xs text-gray-400">{cycles.length} cycles total</span>
        </div>

        <table className="w-full text-left text-xs">
          <thead className="bg-gray-50 border-b border-gray-200 text-gray-500 font-semibold text-[10px] uppercase">
            <tr>
              <th className="py-2.5 px-4">Cycle ID</th>
              <th className="py-2.5 px-4">Name</th>
              <th className="py-2.5 px-4">Timeline</th>
              <th className="py-2.5 px-4">Status</th>
              <th className="py-2.5 px-4 text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {cycles.map((c) => (
              <tr key={c.id} className="hover:bg-gray-50/70 transition">
                <td className="py-3 px-4 font-mono font-bold text-gray-800">{c.businessId}</td>
                <td className="py-3 px-4 font-semibold text-gray-900">{c.name}</td>
                <td className="py-3 px-4 text-gray-500">
                  {c.startDate} → {c.endDate}
                </td>
                <td className="py-3 px-4">
                  <span
                    className={`px-2 py-0.5 rounded text-[10px] font-bold border ${
                      c.status === 'ACTIVE'
                        ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
                        : c.status === 'CLOSED'
                        ? 'bg-gray-100 text-gray-700 border-gray-300'
                        : 'bg-blue-50 text-blue-700 border-blue-200'
                    }`}
                  >
                    {c.status}
                  </span>
                </td>
                <td className="py-3 px-4 text-right">
                  {getNextStatus(c.status) && (
                    <button
                      onClick={() => handleAdvanceStatus(c)}
                      className="px-2.5 py-1 bg-gray-100 hover:bg-slate-900 hover:text-white rounded text-[11px] font-semibold transition cursor-pointer"
                    >
                      Advance to {getNextStatus(c.status)}
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Create Cycle Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-xs p-4">
          <div className="bg-white rounded-2xl max-w-lg w-full p-6 shadow-2xl border border-gray-200 space-y-4">
            <h3 className="text-sm font-bold text-gray-900">Create Review Cycle</h3>
            <form onSubmit={handleCreateCycle} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">Cycle Name</label>
                <input
                  type="text"
                  required
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="e.g., 2026 H2 Annual Performance Review"
                  className="w-full text-xs px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">Description</label>
                <textarea
                  rows={2}
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  placeholder="Cycle scope and evaluation guidelines..."
                  className="w-full text-xs px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">Start Date</label>
                  <input
                    type="date"
                    required
                    value={startDate}
                    onChange={(e) => setStartDate(e.target.value)}
                    className="w-full text-xs px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">End Date</label>
                  <input
                    type="date"
                    required
                    value={endDate}
                    onChange={(e) => setEndDate(e.target.value)}
                    className="w-full text-xs px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                  />
                </div>
              </div>

              <div className="flex justify-end space-x-2 pt-2 border-t border-gray-200">
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  className="px-3 py-2 bg-gray-100 hover:bg-gray-200 text-gray-700 rounded-lg text-xs font-semibold transition cursor-pointer"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={isSubmitting}
                  className="px-4 py-2 bg-slate-900 hover:bg-slate-800 text-white rounded-lg text-xs font-semibold transition cursor-pointer disabled:opacity-50"
                >
                  {isSubmitting ? 'Scheduling...' : 'Schedule Cycle'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
