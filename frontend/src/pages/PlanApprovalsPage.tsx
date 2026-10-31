import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { developmentPlanApi, employeeApi } from '../services/api';
import { DevelopmentPlan, Employee } from '../types';
import {
  CheckCircle2,
  XCircle,
  Clock,
  Check,
  X,
  MessageSquare,
  AlertCircle,
  Calendar,
} from 'lucide-react';

export const PlanApprovalsPage: React.FC = () => {
  const { user } = useAuth();
  const [teamPlans, setTeamPlans] = useState<DevelopmentPlan[]>([]);
  const [rejectingPlanId, setRejectingPlanId] = useState<string | null>(null);
  const [rejectionComments, setRejectionComments] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    loadPendingPlans();
  }, [user]);

  const loadPendingPlans = async () => {
    if (!user?.employee?.id) return;
    setIsLoading(true);
    try {
      const reports = await employeeApi.getDirectReports(user.employee.id);
      const allPlans: DevelopmentPlan[] = [];
      for (const report of reports) {
        const plans = await developmentPlanApi.getPlansForEmployee(report.id);
        allPlans.push(...plans);
      }
      setTeamPlans(allPlans);
    } catch {
      setTeamPlans([]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleApprove = async (planId: string) => {
    try {
      await developmentPlanApi.approvePlan(planId, 'Approved by engineering manager');
      await loadPendingPlans();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to approve plan');
    }
  };

  const handleReject = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!rejectingPlanId || !rejectionComments.trim()) {
      alert('A rejection reason / coaching comment is mandatory');
      return;
    }
    try {
      await developmentPlanApi.rejectPlan(rejectingPlanId, rejectionComments);
      setRejectingPlanId(null);
      setRejectionComments('');
      await loadPendingPlans();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to reject plan');
    }
  };

  const pendingPlans = teamPlans.filter((p) => p.status === 'SUBMITTED');
  const otherPlans = teamPlans.filter((p) => p.status !== 'SUBMITTED');

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-xl font-bold text-gray-900">Team Development Plan Approvals</h1>
        <p className="text-xs text-gray-500 mt-1">
          Review career growth roadmaps submitted by direct reports. Approving unlocks plan execution.
        </p>
      </div>

      {/* Pending Approvals Section */}
      <div className="space-y-4">
        <div className="flex items-center space-x-2">
          <Clock className="w-4 h-4 text-amber-600" />
          <h2 className="text-xs font-bold text-gray-900 uppercase tracking-wider">
            Pending Manager Action ({pendingPlans.length})
          </h2>
        </div>

        {isLoading ? (
          <div className="text-center py-8 text-xs text-gray-400">Loading pending approvals...</div>
        ) : pendingPlans.length === 0 ? (
          <div className="bg-white p-8 rounded-2xl border border-gray-200 text-center text-xs text-gray-400">
            Inbox clear! No development plans currently awaiting manager review.
          </div>
        ) : (
          <div className="space-y-4">
            {pendingPlans.map((plan) => (
              <div
                key={plan.id}
                className="bg-white rounded-2xl border border-amber-200 shadow-xs p-5 space-y-4"
              >
                <div className="flex items-start justify-between">
                  <div className="flex items-center space-x-3">
                    <div className="w-9 h-9 rounded-full bg-slate-900 text-white flex items-center justify-center font-bold text-xs">
                      {plan.employee.firstName[0]}{plan.employee.lastName[0]}
                    </div>
                    <div>
                      <h3 className="text-sm font-bold text-gray-900">
                        {plan.employee.firstName} {plan.employee.lastName}
                      </h3>
                      <p className="text-xs text-gray-500">{plan.employee.designation}</p>
                    </div>
                  </div>

                  <div className="flex items-center space-x-2">
                    <button
                      onClick={() => handleApprove(plan.id)}
                      className="px-3.5 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg text-xs font-semibold shadow-xs flex items-center space-x-1 cursor-pointer transition"
                    >
                      <Check className="w-4 h-4" />
                      <span>Approve</span>
                    </button>
                    <button
                      onClick={() => setRejectingPlanId(plan.id)}
                      className="px-3.5 py-1.5 bg-red-50 hover:bg-red-100 text-red-700 border border-red-200 rounded-lg text-xs font-semibold flex items-center space-x-1 cursor-pointer transition"
                    >
                      <X className="w-4 h-4" />
                      <span>Reject</span>
                    </button>
                  </div>
                </div>

                {/* Plan Content */}
                <div className="bg-gray-50 p-4 rounded-xl border border-gray-150 space-y-2">
                  <h4 className="text-xs font-bold text-gray-900">{plan.title}</h4>
                  <p className="text-xs text-gray-600 whitespace-pre-wrap">{plan.objectives}</p>
                  <div className="flex items-center space-x-4 pt-2 text-[11px] text-gray-500 border-t border-gray-200">
                    <span>Target Date: {plan.targetDate || 'Flexible'}</span>
                    <span>Skills: {plan.skillsToImprove || 'N/A'}</span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Previously Processed Plans */}
      {otherPlans.length > 0 && (
        <div className="space-y-4 pt-4 border-t border-gray-200">
          <h2 className="text-xs font-bold text-gray-500 uppercase tracking-wider">
            Previously Processed Plans ({otherPlans.length})
          </h2>
          <div className="space-y-2">
            {otherPlans.map((plan) => (
              <div
                key={plan.id}
                className="bg-white p-3.5 rounded-xl border border-gray-200 flex items-center justify-between text-xs"
              >
                <div>
                  <span className="font-bold text-gray-900">{plan.employee.firstName} {plan.employee.lastName}</span>
                  <span className="text-gray-400 mx-2">—</span>
                  <span className="text-gray-700">{plan.title}</span>
                </div>
                <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-gray-100 text-gray-700">
                  {plan.status}
                </span>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Reject Reason Modal */}
      {rejectingPlanId && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-xs p-4">
          <div className="bg-white rounded-2xl max-w-lg w-full p-6 shadow-2xl border border-gray-200 space-y-4">
            <div className="flex items-center space-x-2 text-red-600">
              <AlertCircle className="w-5 h-5" />
              <h3 className="text-sm font-bold text-gray-900">Reject Development Plan</h3>
            </div>
            <p className="text-xs text-gray-600">
              A rejection reason is mandatory to guide the engineer on revisions before resubmission.
            </p>

            <form onSubmit={handleReject} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">
                  Coaching Comments & Revision Request
                </label>
                <textarea
                  rows={3}
                  required
                  value={rejectionComments}
                  onChange={(e) => setRejectionComments(e.target.value)}
                  placeholder="Explain why the plan was rejected and what concrete milestones should be adjusted..."
                  className="w-full text-xs px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:outline-none"
                />
              </div>

              <div className="flex justify-end space-x-2 pt-2 border-t border-gray-200">
                <button
                  type="button"
                  onClick={() => setRejectingPlanId(null)}
                  className="px-3 py-2 bg-gray-100 hover:bg-gray-200 text-gray-700 rounded-lg text-xs font-semibold transition cursor-pointer"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded-lg text-xs font-semibold transition cursor-pointer"
                >
                  Confirm Rejection
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
