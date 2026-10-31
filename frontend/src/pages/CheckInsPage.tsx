import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { goalApi, checkInApi, reviewCycleApi } from '../services/api';
import { Goal, CheckIn, ReviewCycle } from '../types';
import {
  Clock,
  Plus,
  ExternalLink,
  Calendar,
  FileText,
  CheckCircle,
  Paperclip,
} from 'lucide-react';

export const CheckInsPage: React.FC = () => {
  const { user } = useAuth();
  const [goals, setGoals] = useState<Goal[]>([]);
  const [selectedGoalId, setSelectedGoalId] = useState<string>('');
  const [checkIns, setCheckIns] = useState<CheckIn[]>([]);
  const [activeCycle, setActiveCycle] = useState<ReviewCycle | null>(null);
  const [showLogModal, setShowLogModal] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  // Form states
  const [modalGoalId, setModalGoalId] = useState('');
  const [notes, setNotes] = useState('');
  const [progressSnapshot, setProgressSnapshot] = useState(50);
  const [evidenceTitle, setEvidenceTitle] = useState('');
  const [evidenceUrl, setEvidenceUrl] = useState('');

  useEffect(() => {
    const loadInitial = async () => {
      if (!user?.employee?.id) return;
      try {
        const cycle = await reviewCycleApi.getActiveCycle().catch(() => null);
        setActiveCycle(cycle);
        const gList = await goalApi.getGoals(user.employee.id, cycle?.id);
        setGoals(gList);
        if (gList.length > 0) {
          setSelectedGoalId(gList[0].id);
          setModalGoalId(gList[0].id);
          loadCheckIns(gList[0].id);
        }
      } catch {
        // error handled
      }
    };
    loadInitial();
  }, [user]);

  const loadCheckIns = async (gId: string) => {
    setIsLoading(true);
    try {
      const list = await checkInApi.getCheckInsForGoal(gId);
      setCheckIns(list);
    } catch {
      setCheckIns([]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleSelectGoal = (gId: string) => {
    setSelectedGoalId(gId);
    loadCheckIns(gId);
  };

  const handleSubmitCheckIn = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!modalGoalId || !notes.trim()) return;
    try {
      const evidenceList = evidenceTitle.trim()
        ? [{ title: evidenceTitle, url: evidenceUrl, evidenceType: 'URL' }]
        : [];

      await checkInApi.recordCheckIn({
        goalId: modalGoalId,
        notes,
        progressSnapshot,
        checkInDate: new Date().toISOString().split('T')[0],
        evidenceList,
      });

      setShowLogModal(false);
      setNotes('');
      setEvidenceTitle('');
      setEvidenceUrl('');
      await loadCheckIns(selectedGoalId);
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to record check-in');
    }
  };

  const currentGoal = goals.find((g) => g.id === selectedGoalId);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-gray-900">Check-in Timeline & Deliverables</h1>
          <p className="text-xs text-gray-500 mt-1">
            Chronological reflection notes, progress snapshots, and attached deliverables.
          </p>
        </div>

        <button
          onClick={() => setShowLogModal(true)}
          className="px-3.5 py-2 bg-slate-900 hover:bg-slate-800 text-white rounded-lg text-xs font-semibold shadow-xs flex items-center space-x-1.5 transition cursor-pointer"
        >
          <Plus className="w-4 h-4" />
          <span>Record Check-in</span>
        </button>
      </div>

      {/* Goal Selector Tabs */}
      <div className="flex items-center space-x-2 overflow-x-auto pb-2 scrollbar-thin">
        {goals.map((g) => (
          <button
            key={g.id}
            onClick={() => handleSelectGoal(g.id)}
            className={`px-3.5 py-2 rounded-xl text-xs font-medium shrink-0 transition cursor-pointer border ${
              selectedGoalId === g.id
                ? 'bg-slate-900 text-white border-slate-900 shadow-xs'
                : 'bg-white text-gray-700 border-gray-200 hover:bg-gray-50'
            }`}
          >
            <div className="flex items-center space-x-2">
              <span className="font-bold">{g.businessId}</span>
              <span className="truncate max-w-[140px]">{g.title}</span>
              <span
                className={`text-[10px] px-1.5 py-0.2 rounded font-bold ${
                  selectedGoalId === g.id ? 'bg-slate-800 text-emerald-300' : 'bg-gray-100 text-gray-700'
                }`}
              >
                {g.progress}%
              </span>
            </div>
          </button>
        ))}
      </div>

      {/* Timeline Entries */}
      <div className="bg-white p-6 rounded-2xl border border-gray-200 shadow-xs">
        <div className="flex items-center justify-between pb-4 mb-6 border-b border-gray-100">
          <div>
            <h3 className="text-sm font-bold text-gray-900">
              {currentGoal?.title || 'Selected Goal'}
            </h3>
            <p className="text-xs text-gray-500 mt-0.5">
              Category: {currentGoal?.category} • Current Progress: {currentGoal?.progress}%
            </p>
          </div>
          <span className="text-xs text-gray-400 font-medium">{checkIns.length} check-ins logged</span>
        </div>

        {isLoading ? (
          <div className="text-center py-8 text-xs text-gray-400">Loading timeline...</div>
        ) : checkIns.length === 0 ? (
          <div className="text-center py-12 text-xs text-gray-400">
            No check-ins logged for this goal yet. Click "Record Check-in" above to log your first milestone update.
          </div>
        ) : (
          <div className="relative pl-6 border-l-2 border-indigo-100 space-y-6">
            {checkIns.map((cin) => (
              <div key={cin.id} className="relative group">
                {/* Timeline node */}
                <div className="absolute -left-[31px] top-1 w-3.5 h-3.5 rounded-full bg-white border-2 border-indigo-600 group-hover:scale-125 transition-transform" />

                <div className="bg-gray-50 p-4 rounded-xl border border-gray-200 shadow-2xs space-y-2">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center space-x-2">
                      <span className="text-[10px] font-bold text-gray-400 uppercase tracking-wider">
                        {cin.businessId}
                      </span>
                      <span className="text-[10px] text-gray-500 flex items-center space-x-1">
                        <Calendar className="w-3 h-3 text-gray-400" />
                        <span>{cin.checkInDate}</span>
                      </span>
                    </div>

                    <div className="flex items-center space-x-1.5 px-2 py-0.5 bg-emerald-50 border border-emerald-200 rounded text-[11px] font-bold text-emerald-700">
                      <CheckCircle className="w-3 h-3" />
                      <span>Snapshot: {cin.progressSnapshot}%</span>
                    </div>
                  </div>

                  <p className="text-xs text-gray-800 whitespace-pre-wrap leading-relaxed">
                    {cin.notes}
                  </p>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Record Check-in Modal */}
      {showLogModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-xs p-4">
          <div className="bg-white rounded-2xl max-w-lg w-full p-6 shadow-2xl border border-gray-200 space-y-4">
            <h3 className="text-sm font-bold text-gray-900">Record Progress Check-in</h3>
            <form onSubmit={handleSubmitCheckIn} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">Target Goal</label>
                <select
                  value={modalGoalId}
                  onChange={(e) => setModalGoalId(e.target.value)}
                  className="w-full text-xs px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                >
                  {goals.map((g) => (
                    <option key={g.id} value={g.id}>
                      {g.businessId} — {g.title}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">
                  Progress Snapshot ({progressSnapshot}%)
                </label>
                <input
                  type="range"
                  min="0"
                  max="100"
                  step="5"
                  value={progressSnapshot}
                  onChange={(e) => setProgressSnapshot(Number(e.target.value))}
                  className="w-full accent-indigo-600 cursor-pointer"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">
                  Milestone Notes & Reflections
                </label>
                <textarea
                  required
                  rows={3}
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
                  placeholder="Detail milestones reached, blockers resolved, or engineering deliverables..."
                  className="w-full text-xs px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                />
              </div>

              <div className="p-3 bg-gray-50 border border-gray-200 rounded-xl space-y-2">
                <span className="text-[11px] font-bold text-gray-600 uppercase tracking-wider flex items-center space-x-1">
                  <Paperclip className="w-3.5 h-3.5" />
                  <span>Attach Evidence / Deliverable URL (Optional)</span>
                </span>
                <div className="grid grid-cols-2 gap-2">
                  <input
                    type="text"
                    value={evidenceTitle}
                    onChange={(e) => setEvidenceTitle(e.target.value)}
                    placeholder="Deliverable Title (e.g. PR #123)"
                    className="w-full text-xs px-2.5 py-1.5 border border-gray-300 rounded bg-white"
                  />
                  <input
                    type="url"
                    value={evidenceUrl}
                    onChange={(e) => setEvidenceUrl(e.target.value)}
                    placeholder="URL (e.g. https://github.com/...)"
                    className="w-full text-xs px-2.5 py-1.5 border border-gray-300 rounded bg-white"
                  />
                </div>
              </div>

              <div className="flex justify-end space-x-2 pt-2 border-t border-gray-200">
                <button
                  type="button"
                  onClick={() => setShowLogModal(false)}
                  className="px-3 py-2 bg-gray-100 hover:bg-gray-200 text-gray-700 rounded-lg text-xs font-semibold transition cursor-pointer"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-slate-900 hover:bg-slate-800 text-white rounded-lg text-xs font-semibold transition cursor-pointer"
                >
                  Save Check-in
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
