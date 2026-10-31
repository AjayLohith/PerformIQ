import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { developmentPlanApi } from '../services/api';
import { DevelopmentPlan, DevelopmentPlanDetails, ApprovalHistory } from '../types';
import {
  Compass,
  Plus,
  Send,
  Play,
  CheckCircle,
  Clock,
  History,
  XCircle,
  Calendar,
} from 'lucide-react';

export const DevelopmentPlansPage: React.FC = () => {
  const { user } = useAuth();
  const [plans, setPlans] = useState<DevelopmentPlan[]>([]);
  const [selectedPlanDetails, setSelectedPlanDetails] = useState<DevelopmentPlanDetails | null>(null);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  // Form states
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [objectives, setObjectives] = useState('');
  const [skills, setSkills] = useState('');
  const [actionItems, setActionItems] = useState('');
  const [targetDate, setTargetDate] = useState('2026-12-31');

  useEffect(() => {
    loadPlans();
  }, [user]);

  const loadPlans = async () => {
    if (!user?.employee?.id) return;
    setIsLoading(true);
    try {
      const list = await developmentPlanApi.getPlansForEmployee(user.employee.id);
      setPlans(list);
    } catch {
      // error handled
    } finally {
      setIsLoading(false);
    }
  };

  const handleCreatePlan = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user?.employee?.id) return;
    try {
      await developmentPlanApi.createPlan({
        employeeId: user.employee.id,
        title,
        description,
        objectives,
        skillsToImprove: skills,
        actionItems,
        targetDate,
      });
      setShowCreateModal(false);
      setTitle('');
      setDescription('');
      setObjectives('');
      setSkills('');
      setActionItems('');
      await loadPlans();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to create plan');
    }
  };

  const handleSubmitPlan = async (planId: string) => {
    try {
      await developmentPlanApi.submitPlan(planId, 'Submitted for manager coaching review');
      await loadPlans();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to submit plan');
    }
  };

  const handleStartPlan = async (planId: string) => {
    try {
      await developmentPlanApi.startPlan(planId);
      await loadPlans();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to start plan');
    }
  };

  const handleCompletePlan = async (planId: string) => {
    try {
      await developmentPlanApi.completePlan(planId, 'Objectives and action items completed');
      await loadPlans();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to complete plan');
    }
  };

  const handleViewHistory = async (planId: string) => {
    try {
      const details = await developmentPlanApi.getPlanDetails(planId);
      setSelectedPlanDetails(details);
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to load plan history');
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'APPROVED':
        return 'bg-emerald-50 text-emerald-700 border-emerald-200';
      case 'SUBMITTED':
        return 'bg-blue-50 text-blue-700 border-blue-200';
      case 'REJECTED':
        return 'bg-red-50 text-red-700 border-red-200';
      case 'IN_PROGRESS':
        return 'bg-purple-50 text-purple-700 border-purple-200';
      case 'COMPLETED':
        return 'bg-emerald-100 text-emerald-800 border-emerald-300';
      default:
        return 'bg-gray-100 text-gray-700 border-gray-200';
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-gray-900">Career Development & Growth Plans</h1>
          <p className="text-xs text-gray-500 mt-1">
            Personal coaching objectives, skills roadmap, and manager approval workflow.
          </p>
        </div>

        <button
          onClick={() => setShowCreateModal(true)}
          className="px-3.5 py-2 bg-slate-900 hover:bg-slate-800 text-white rounded-lg text-xs font-semibold shadow-xs flex items-center space-x-1.5 transition cursor-pointer"
        >
          <Plus className="w-4 h-4" />
          <span>New Development Plan</span>
        </button>
      </div>

      {/* Plans List */}
      <div className="space-y-4">
        {isLoading ? (
          <div className="text-center py-8 text-xs text-gray-400">Loading plans...</div>
        ) : plans.length === 0 ? (
          <div className="bg-white p-12 rounded-2xl border border-gray-200 text-center text-xs text-gray-400">
            No development plans created yet. Click "New Development Plan" to set your personal growth milestones.
          </div>
        ) : (
          plans.map((p) => (
            <div
              key={p.id}
              className="bg-white rounded-xl border border-gray-200 shadow-xs p-5 space-y-4"
            >
              <div className="flex items-start justify-between">
                <div>
                  <div className="flex items-center space-x-2">
                    <span className="text-[10px] font-bold text-gray-400 uppercase tracking-wider">
                      {p.businessId}
                    </span>
                    <span
                      className={`text-[10px] px-2 py-0.5 rounded font-bold border ${getStatusBadge(
                        p.status
                      )}`}
                    >
                      {p.status}
                    </span>
                    {p.targetDate && (
                      <span className="text-[10px] text-gray-400 flex items-center space-x-1">
                        <Calendar className="w-3 h-3" />
                        <span>Target: {p.targetDate}</span>
                      </span>
                    )}
                  </div>
                  <h3 className="text-base font-bold text-gray-900 mt-1">{p.title}</h3>
                  {p.description && (
                    <p className="text-xs text-gray-500 mt-0.5">{p.description}</p>
                  )}
                </div>

                {/* Actions */}
                <div className="flex items-center space-x-2">
                  <button
                    onClick={() => handleViewHistory(p.id)}
                    className="px-2.5 py-1.5 bg-gray-50 hover:bg-gray-100 border border-gray-200 rounded text-xs font-medium text-gray-700 shadow-2xs flex items-center space-x-1 cursor-pointer"
                  >
                    <History className="w-3.5 h-3.5" />
                    <span>Approval Trail</span>
                  </button>

                  {p.status === 'DRAFT' || p.status === 'REJECTED' ? (
                    <button
                      onClick={() => handleSubmitPlan(p.id)}
                      className="px-3 py-1.5 bg-indigo-600 hover:bg-indigo-700 text-white rounded text-xs font-semibold shadow-xs flex items-center space-x-1.5 transition cursor-pointer"
                    >
                      <Send className="w-3.5 h-3.5" />
                      <span>Submit Plan</span>
                    </button>
                  ) : p.status === 'APPROVED' ? (
                    <button
                      onClick={() => handleStartPlan(p.id)}
                      className="px-3 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded text-xs font-semibold shadow-xs flex items-center space-x-1.5 transition cursor-pointer"
                    >
                      <Play className="w-3.5 h-3.5" />
                      <span>Start Execution</span>
                    </button>
                  ) : p.status === 'IN_PROGRESS' ? (
                    <button
                      onClick={() => handleCompletePlan(p.id)}
                      className="px-3 py-1.5 bg-slate-900 hover:bg-slate-800 text-white rounded text-xs font-semibold shadow-xs flex items-center space-x-1.5 transition cursor-pointer"
                    >
                      <CheckCircle className="w-3.5 h-3.5" />
                      <span>Complete Plan</span>
                    </button>
                  ) : null}
                </div>
              </div>

              {/* Plan Details Blocks */}
              <div className="grid grid-cols-1 md:grid-cols-3 gap-3 pt-2 border-t border-gray-100">
                <div className="p-3 bg-gray-50 rounded-lg border border-gray-100">
                  <span className="text-[10px] font-bold text-gray-400 uppercase tracking-wider block mb-1">
                    Growth Objectives
                  </span>
                  <p className="text-xs text-gray-700 whitespace-pre-wrap">{p.objectives}</p>
                </div>

                <div className="p-3 bg-gray-50 rounded-lg border border-gray-100">
                  <span className="text-[10px] font-bold text-gray-400 uppercase tracking-wider block mb-1">
                    Skills to Improve
                  </span>
                  <p className="text-xs text-gray-700 whitespace-pre-wrap">
                    {p.skillsToImprove || 'Not specified'}
                  </p>
                </div>

                <div className="p-3 bg-gray-50 rounded-lg border border-gray-100">
                  <span className="text-[10px] font-bold text-gray-400 uppercase tracking-wider block mb-1">
                    Action Items
                  </span>
                  <p className="text-xs text-gray-700 whitespace-pre-wrap">
                    {p.actionItems || 'Not specified'}
                  </p>
                </div>
              </div>
            </div>
          ))
        )}
      </div>

      {/* Create Plan Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-xs p-4 overflow-y-auto">
          <div className="bg-white rounded-2xl max-w-lg w-full p-6 shadow-2xl border border-gray-200 space-y-4 my-8">
            <h3 className="text-sm font-bold text-gray-900">Create Career Development Plan</h3>
            <form onSubmit={handleCreatePlan} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">Plan Title</label>
                <input
                  type="text"
                  required
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  placeholder="e.g. Distributed Systems Engineering & Architecture"
                  className="w-full text-xs px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">Summary</label>
                <textarea
                  rows={2}
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  placeholder="Growth motivation and high-level milestones..."
                  className="w-full text-xs px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">
                  Core Objectives
                </label>
                <textarea
                  rows={2}
                  required
                  value={objectives}
                  onChange={(e) => setObjectives(e.target.value)}
                  placeholder="Concrete career milestones and learning outcomes..."
                  className="w-full text-xs px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">
                    Skills to Improve
                  </label>
                  <input
                    type="text"
                    value={skills}
                    onChange={(e) => setSkills(e.target.value)}
                    placeholder="e.g. Kafka, Raft consensus"
                    className="w-full text-xs px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">Target Date</label>
                  <input
                    type="date"
                    value={targetDate}
                    onChange={(e) => setTargetDate(e.target.value)}
                    className="w-full text-xs px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">Action Items</label>
                <textarea
                  rows={2}
                  value={actionItems}
                  onChange={(e) => setActionItems(e.target.value)}
                  placeholder="Specific projects, courses, books, or team mentorships..."
                  className="w-full text-xs px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                />
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
                  className="px-4 py-2 bg-slate-900 hover:bg-slate-800 text-white rounded-lg text-xs font-semibold transition cursor-pointer"
                >
                  Save Draft Plan
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Approval History Trail Modal */}
      {selectedPlanDetails && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-xs p-4">
          <div className="bg-white rounded-2xl max-w-lg w-full p-6 shadow-2xl border border-gray-200 space-y-4">
            <div className="flex items-center justify-between pb-3 border-b border-gray-200">
              <div>
                <h3 className="text-sm font-bold text-gray-900">
                  Approval Trail — {selectedPlanDetails.plan.businessId}
                </h3>
                <p className="text-xs text-gray-500">{selectedPlanDetails.plan.title}</p>
              </div>
              <button
                onClick={() => setSelectedPlanDetails(null)}
                className="text-gray-400 hover:text-gray-600 text-xs font-bold cursor-pointer"
              >
                Close
              </button>
            </div>

            <div className="space-y-3 max-h-80 overflow-y-auto">
              {selectedPlanDetails.approvalHistory.length === 0 ? (
                <div className="text-center py-6 text-xs text-gray-400">
                  No approval history recorded yet.
                </div>
              ) : (
                selectedPlanDetails.approvalHistory.map((ah) => (
                  <div
                    key={ah.id}
                    className="p-3 bg-gray-50 rounded-xl border border-gray-150 space-y-1"
                  >
                    <div className="flex items-center justify-between">
                      <div className="flex items-center space-x-2">
                        <span
                          className={`text-[9px] font-bold px-1.5 py-0.2 rounded ${
                            ah.action === 'APPROVED'
                              ? 'bg-emerald-100 text-emerald-800'
                              : ah.action === 'REJECTED'
                              ? 'bg-red-100 text-red-800'
                              : 'bg-blue-100 text-blue-800'
                          }`}
                        >
                          {ah.action}
                        </span>
                        <span className="text-xs font-semibold text-gray-800">
                          {ah.approver ? `${ah.approver.firstName} ${ah.approver.lastName}` : 'System'}
                        </span>
                      </div>
                      <span className="text-[10px] text-gray-400">{ah.actionDate}</span>
                    </div>
                    {ah.comments && (
                      <p className="text-xs text-gray-600 italic">"{ah.comments}"</p>
                    )}
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
