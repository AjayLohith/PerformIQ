import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { goalApi, keyResultApi, reviewCycleApi } from '../services/api';
import { Goal, KeyResult, ReviewCycle, MetricType } from '../types';
import {
  Target,
  Plus,
  ChevronDown,
  ChevronUp,
  TrendingUp,
  TrendingDown,
  CheckCircle2,
  Sliders,
  Sparkles,
} from 'lucide-react';

export const GoalsPage: React.FC = () => {
  const { user } = useAuth();
  const [goals, setGoals] = useState<Goal[]>([]);
  const [activeCycle, setActiveCycle] = useState<ReviewCycle | null>(null);
  const [expandedGoalId, setExpandedGoalId] = useState<string | null>(null);
  const [keyResultsMap, setKeyResultsMap] = useState<Record<string, KeyResult[]>>({});
  const [showCreateGoalModal, setShowCreateGoalModal] = useState(false);
  const [showAddKrModal, setShowAddKrModal] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  // New Goal Form
  const [newTitle, setNewTitle] = useState('');
  const [newDesc, setNewDesc] = useState('');
  const [newCategory, setNewCategory] = useState('TECHNICAL');
  const [newWeight, setNewWeight] = useState(25);

  // New Key Result Form
  const [krTitle, setKrTitle] = useState('');
  const [krMetric, setKrMetric] = useState<MetricType>('INCREASE');
  const [krStart, setKrStart] = useState(0);
  const [krTarget, setKrTarget] = useState(100);
  const [krCurrent, setKrCurrent] = useState(0);
  const [krWeight, setKrWeight] = useState(50);
  const [krUnit, setKrUnit] = useState('%');

  useEffect(() => {
    loadGoals();
  }, [user]);

  const loadGoals = async () => {
    if (!user?.employee?.id) return;
    try {
      const cycle = await reviewCycleApi.getActiveCycle().catch(() => null);
      setActiveCycle(cycle);
      const list = await goalApi.getGoals(user.employee.id, cycle?.id);
      setGoals(list);
      if (list.length > 0 && !expandedGoalId) {
        setExpandedGoalId(list[0].id);
        loadKeyResults(list[0].id);
      }
    } catch {
      // error handled
    }
  };

  const loadKeyResults = async (goalId: string) => {
    try {
      const krs = await keyResultApi.getKeyResults(goalId);
      setKeyResultsMap((prev) => ({ ...prev, [goalId]: krs }));
    } catch {
      // error handled
    }
  };

  const handleToggleGoal = (goalId: string) => {
    if (expandedGoalId === goalId) {
      setExpandedGoalId(null);
    } else {
      setExpandedGoalId(goalId);
      if (!keyResultsMap[goalId]) {
        loadKeyResults(goalId);
      }
    }
  };

  const handleUpdateKrValue = async (kr: KeyResult, newValue: number) => {
    try {
      await keyResultApi.updateCurrentValue(kr.id, newValue, 'Live slider update');
      // Reload key results and goals to see the mathematical progress cascade!
      if (expandedGoalId) {
        await loadKeyResults(expandedGoalId);
      }
      if (user?.employee?.id) {
        const refreshedGoals = await goalApi.getGoals(user.employee.id, activeCycle?.id);
        setGoals(refreshedGoals);
      }
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to update key result value');
    }
  };

  const handleCreateGoal = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user?.employee?.id) return;
    if (!activeCycle?.id) {
      setErrorMsg('No active review cycle found. An active cycle is required to create goals.');
      return;
    }
    setIsSubmitting(true);
    setErrorMsg(null);
    try {
      await goalApi.createGoal({
        employeeId: user.employee.id,
        reviewCycleId: activeCycle.id,
        title: newTitle,
        description: newDesc,
        category: newCategory,
        weight: newWeight,
      });
      setShowCreateGoalModal(false);
      setNewTitle('');
      setNewDesc('');
      await loadGoals();
    } catch (err: any) {
      setErrorMsg(err.response?.data?.message || 'Failed to create goal');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleAddKeyResult = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!showAddKrModal) return;
    setIsSubmitting(true);
    try {
      await keyResultApi.createKeyResult({
        goalId: showAddKrModal,
        title: krTitle,
        metricType: krMetric,
        startValue: Number(krStart),
        targetValue: Number(krTarget),
        currentValue: Number(krCurrent),
        weight: Number(krWeight),
        unit: krUnit,
      });
      const goalId = showAddKrModal;
      setShowAddKrModal(null);
      setKrTitle('');
      await loadKeyResults(goalId);
      if (user?.employee?.id) {
        const refreshedGoals = await goalApi.getGoals(user.employee.id, activeCycle?.id);
        setGoals(refreshedGoals);
      }
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to add key result');
    } finally {
      setIsSubmitting(false);
    }
  };

  const totalCommittedWeight = goals.reduce((acc, g) => acc + Number(g.weight || 0), 0);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-gray-900">Performance Goals & OKRs</h1>
          <p className="text-xs text-gray-500 mt-1">
            Track measurable outcomes. Updating Key Results cascades automatically into Goal progress.
          </p>
        </div>

        <div className="flex items-center space-x-3">
          <div className="px-3 py-1.5 bg-white border border-gray-200 rounded-lg text-xs font-semibold text-gray-700 shadow-2xs">
            Committed Weight:{' '}
            <span className={totalCommittedWeight > 100 ? 'text-red-600' : 'text-emerald-600'}>
              {totalCommittedWeight.toFixed(1)}% / 100.0%
            </span>
          </div>

          <button
            onClick={() => {
              setErrorMsg(null);
              setShowCreateGoalModal(true);
            }}
            className="px-3.5 py-2 bg-slate-900 hover:bg-slate-800 text-white rounded-lg text-xs font-semibold shadow-xs flex items-center space-x-1.5 transition cursor-pointer"
          >
            <Plus className="w-4 h-4" />
            <span>Create Goal</span>
          </button>
        </div>
      </div>

      {/* Goals List */}
      <div className="space-y-4">
        {goals.map((goal) => {
          const isExpanded = expandedGoalId === goal.id;
          const krs = keyResultsMap[goal.id] || [];

          return (
            <div
              key={goal.id}
              className="bg-white rounded-xl border border-gray-200 shadow-xs overflow-hidden transition"
            >
              {/* Goal Card Header */}
              <div
                onClick={() => handleToggleGoal(goal.id)}
                className="p-5 flex items-center justify-between cursor-pointer hover:bg-gray-50/70 select-none"
              >
                <div className="flex items-start space-x-3.5 flex-1 min-w-0 pr-4">
                  <div className="p-2.5 bg-indigo-50 text-indigo-600 rounded-xl mt-0.5 shrink-0">
                    <Target className="w-5 h-5" />
                  </div>
                  <div className="min-w-0 flex-1">
                    <div className="flex items-center space-x-2">
                      <span className="text-xs font-bold text-gray-500 uppercase tracking-wider">
                        {goal.businessId}
                      </span>
                      <span className="text-[10px] px-2 py-0.5 rounded font-bold bg-gray-100 text-gray-700 border border-gray-200">
                        {goal.category}
                      </span>
                      <span
                        className={`text-[10px] px-2 py-0.5 rounded font-bold border ${
                          goal.status === 'COMPLETED'
                            ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
                            : 'bg-blue-50 text-blue-700 border-blue-200'
                        }`}
                      >
                        {goal.status}
                      </span>
                    </div>
                    <h3 className="text-sm font-bold text-gray-900 mt-1">{goal.title}</h3>
                    {goal.description && (
                      <p className="text-xs text-gray-500 mt-0.5 line-clamp-1">{goal.description}</p>
                    )}
                  </div>
                </div>

                {/* Progress Bar & Weight */}
                <div className="flex items-center space-x-6 shrink-0">
                  <div className="text-right">
                    <div className="flex items-baseline justify-end space-x-1">
                      <span className="text-lg font-black text-gray-900">{goal.progress}%</span>
                    </div>
                    <span className="text-[10px] text-gray-400 font-medium">Weight: {goal.weight}%</span>
                  </div>

                  <div className="w-28 hidden md:block">
                    <div className="w-full bg-gray-100 rounded-full h-2 overflow-hidden">
                      <div
                        className="bg-indigo-600 h-2 rounded-full transition-all duration-500"
                        style={{ width: `${goal.progress}%` }}
                      />
                    </div>
                  </div>

                  <div className="text-gray-400">
                    {isExpanded ? <ChevronUp className="w-5 h-5" /> : <ChevronDown className="w-5 h-5" />}
                  </div>
                </div>
              </div>

              {/* Expandable Key Results Section */}
              {isExpanded && (
                <div className="bg-slate-50/70 border-t border-gray-200 p-5">
                  <div className="flex items-center justify-between mb-4">
                    <div className="flex items-center space-x-2">
                      <Sliders className="w-4 h-4 text-indigo-600" />
                      <h4 className="text-xs font-bold text-gray-900 uppercase tracking-wider">
                        Measurable Key Results (Formula Engine)
                      </h4>
                    </div>

                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        setShowAddKrModal(goal.id);
                      }}
                      className="px-2.5 py-1 bg-white hover:bg-gray-100 border border-gray-300 rounded text-xs font-semibold text-gray-700 shadow-2xs flex items-center space-x-1 cursor-pointer"
                    >
                      <Plus className="w-3.5 h-3.5" />
                      <span>Add Key Result</span>
                    </button>
                  </div>

                  {krs.length === 0 ? (
                    <div className="text-center py-6 text-xs text-gray-400">
                      No key results attached yet. Add one to enable weighted progress calculation.
                    </div>
                  ) : (
                    <div className="space-y-3">
                      {krs.map((kr) => {
                        const isIncrease = kr.metricType === 'INCREASE';
                        return (
                          <div
                            key={kr.id}
                            className="bg-white p-4 rounded-xl border border-gray-200 shadow-2xs space-y-3"
                          >
                            <div className="flex items-start justify-between">
                              <div>
                                <div className="flex items-center space-x-2">
                                  <span className="text-[10px] font-bold text-gray-400">
                                    {kr.businessId}
                                  </span>
                                  <span
                                    className={`text-[9px] font-bold px-1.5 py-0.2 rounded flex items-center space-x-1 ${
                                      isIncrease
                                        ? 'bg-blue-50 text-blue-700 border border-blue-200'
                                        : 'bg-amber-50 text-amber-700 border border-amber-200'
                                    }`}
                                  >
                                    {isIncrease ? (
                                      <TrendingUp className="w-2.5 h-2.5" />
                                    ) : (
                                      <TrendingDown className="w-2.5 h-2.5" />
                                    )}
                                    <span>{kr.metricType}</span>
                                  </span>
                                  <span className="text-[10px] text-gray-400">
                                    Weight: {kr.weight}
                                  </span>
                                </div>
                                <h5 className="text-xs font-bold text-gray-900 mt-1">{kr.title}</h5>
                              </div>

                              <div className="text-right">
                                <span className="text-sm font-extrabold text-indigo-600">
                                  {kr.progress}%
                                </span>
                                <p className="text-[10px] text-gray-400">
                                  {kr.currentValue} / {kr.targetValue} {kr.unit}
                                </p>
                              </div>
                            </div>

                            {/* Interactive Value Slider */}
                            <div className="space-y-1 pt-1 border-t border-gray-100">
                              <div className="flex items-center justify-between text-[11px] text-gray-500">
                                <span>Start: {kr.startValue} {kr.unit}</span>
                                <span className="font-bold text-gray-800">
                                  Current: {kr.currentValue} {kr.unit}
                                </span>
                                <span>Target: {kr.targetValue} {kr.unit}</span>
                              </div>

                              <input
                                type="range"
                                min={Math.min(kr.startValue, kr.targetValue)}
                                max={Math.max(kr.startValue, kr.targetValue)}
                                step="1"
                                value={kr.currentValue}
                                onChange={(e) => handleUpdateKrValue(kr, Number(e.target.value))}
                                className="w-full accent-indigo-600 cursor-pointer"
                              />
                            </div>
                          </div>
                        );
                      })}
                    </div>
                  )}
                </div>
              )}
            </div>
          );
        })}
      </div>

      {/* Create Goal Modal */}
      {showCreateGoalModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-xs p-4">
          <div className="bg-white rounded-2xl max-w-lg w-full p-6 shadow-2xl border border-gray-200 space-y-4">
            <h3 className="text-sm font-bold text-gray-900">Create New Goal</h3>

            {errorMsg && (
              <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-xs text-red-700">
                {errorMsg}
              </div>
            )}

            <form onSubmit={handleCreateGoal} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">Title</label>
                <input
                  type="text"
                  required
                  value={newTitle}
                  onChange={(e) => setNewTitle(e.target.value)}
                  placeholder="e.g., Deliver Zero-Trust Service Mesh"
                  className="w-full text-xs px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">Description</label>
                <textarea
                  rows={2}
                  value={newDesc}
                  onChange={(e) => setNewDesc(e.target.value)}
                  placeholder="Goal rationale and execution milestones"
                  className="w-full text-xs px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">Category</label>
                  <select
                    value={newCategory}
                    onChange={(e) => setNewCategory(e.target.value)}
                    className="w-full text-xs px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                  >
                    <option value="TECHNICAL">TECHNICAL</option>
                    <option value="DELIVERY">DELIVERY</option>
                    <option value="LEADERSHIP">LEADERSHIP</option>
                    <option value="INNOVATION">INNOVATION</option>
                    <option value="INDIVIDUAL">INDIVIDUAL</option>
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">
                    Weight ({newWeight}%)
                  </label>
                  <input
                    type="range"
                    min="5"
                    max="100"
                    step="5"
                    value={newWeight}
                    onChange={(e) => setNewWeight(Number(e.target.value))}
                    className="w-full accent-indigo-600 cursor-pointer mt-2"
                  />
                </div>
              </div>

              <div className="flex justify-end space-x-2 pt-2 border-t border-gray-200">
                <button
                  type="button"
                  onClick={() => setShowCreateGoalModal(false)}
                  className="px-3 py-2 bg-gray-100 hover:bg-gray-200 text-gray-700 rounded-lg text-xs font-semibold transition cursor-pointer"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={isSubmitting}
                  className="px-4 py-2 bg-slate-900 hover:bg-slate-800 text-white rounded-lg text-xs font-semibold transition cursor-pointer disabled:opacity-50"
                >
                  {isSubmitting ? 'Saving...' : 'Create Goal'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Add Key Result Modal */}
      {showAddKrModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-xs p-4">
          <div className="bg-white rounded-2xl max-w-lg w-full p-6 shadow-2xl border border-gray-200 space-y-4">
            <h3 className="text-sm font-bold text-gray-900">Add Measurable Key Result</h3>
            <form onSubmit={handleAddKeyResult} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">Key Result Title</label>
                <input
                  type="text"
                  required
                  value={krTitle}
                  onChange={(e) => setKrTitle(e.target.value)}
                  placeholder="e.g. Reduce P99 latency from 200ms to 50ms"
                  className="w-full text-xs px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">Metric Type</label>
                  <select
                    value={krMetric}
                    onChange={(e) => setKrMetric(e.target.value as MetricType)}
                    className="w-full text-xs px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                  >
                    <option value="INCREASE">INCREASE (Target &gt; Start)</option>
                    <option value="DECREASE">DECREASE (Target &lt; Start)</option>
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">Unit</label>
                  <input
                    type="text"
                    value={krUnit}
                    onChange={(e) => setKrUnit(e.target.value)}
                    placeholder="%, ms, PRs, bugs"
                    className="w-full text-xs px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                  />
                </div>
              </div>

              <div className="grid grid-cols-3 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">Start Value</label>
                  <input
                    type="number"
                    value={krStart}
                    onChange={(e) => setKrStart(Number(e.target.value))}
                    className="w-full text-xs px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">Target Value</label>
                  <input
                    type="number"
                    value={krTarget}
                    onChange={(e) => setKrTarget(Number(e.target.value))}
                    className="w-full text-xs px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">Initial Value</label>
                  <input
                    type="number"
                    value={krCurrent}
                    onChange={(e) => setKrCurrent(Number(e.target.value))}
                    className="w-full text-xs px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                  />
                </div>
              </div>

              <div className="flex justify-end space-x-2 pt-2 border-t border-gray-200">
                <button
                  type="button"
                  onClick={() => setShowAddKrModal(null)}
                  className="px-3 py-2 bg-gray-100 hover:bg-gray-200 text-gray-700 rounded-lg text-xs font-semibold transition cursor-pointer"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={isSubmitting}
                  className="px-4 py-2 bg-slate-900 hover:bg-slate-800 text-white rounded-lg text-xs font-semibold transition cursor-pointer disabled:opacity-50"
                >
                  {isSubmitting ? 'Adding...' : 'Attach Key Result'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
