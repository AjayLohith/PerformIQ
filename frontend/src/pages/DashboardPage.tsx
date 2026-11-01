import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import {
  reviewCycleApi,
  goalApi,
  employeeApi,
  adminApi,
  feedbackApi,
  developmentPlanApi,
} from '../services/api';
import {
  ReviewCycle,
  Goal,
  Employee,
  DataImportRun,
  FeedbackSummary,
  DevelopmentPlan,
} from '../types';
import {
  Target,
  Users,
  Award,
  AlertTriangle,
  ArrowUpRight,
  TrendingUp,
  Clock,
  Compass,
  CheckCircle2,
  Database,
  CalendarCheck2,
} from 'lucide-react';

interface DashboardProps {
  onNavigate: (path: string) => void;
}

export const DashboardPage: React.FC<DashboardProps> = ({ onNavigate }) => {
  const { user, role } = useAuth();
  const [activeCycle, setActiveCycle] = useState<ReviewCycle | null>(null);
  const [goals, setGoals] = useState<Goal[]>([]);
  const [directReports, setDirectReports] = useState<Employee[]>([]);
  const [importStatus, setImportStatus] = useState<DataImportRun | null>(null);
  const [feedbackSummary, setFeedbackSummary] = useState<FeedbackSummary | null>(null);
  const [devPlans, setDevPlans] = useState<DevelopmentPlan[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const loadDashboardData = async () => {
      setIsLoading(true);
      try {
        const cycle = await reviewCycleApi.getActiveCycle().catch(() => null);
        setActiveCycle(cycle);

        if (user?.employee?.id) {
          // Employee data
          const myGoals = await goalApi.getGoals(user.employee.id, cycle?.id).catch(() => []);
          setGoals(myGoals);

          const plans = await developmentPlanApi.getPlansForEmployee(user.employee.id).catch(() => []);
          setDevPlans(plans);

          if (cycle?.id) {
            const fbSummary = await feedbackApi.getFeedbackSummary(user.employee.id, cycle.id).catch(() => null);
            setFeedbackSummary(fbSummary);
          }

          // Manager data
          if (role === 'MANAGER' || role === 'ADMIN') {
            const reports = await employeeApi.getDirectReports(user.employee.id).catch(() => []);
            setDirectReports(reports);
          }
        }

        // Admin data
        if (role === 'ADMIN') {
          const run = await adminApi.getImportStatus().catch(() => null);
          setImportStatus(run);
        }
      } finally {
        setIsLoading(false);
      }
    };

    loadDashboardData();
  }, [user, role]);

  const avgProgress = goals.length > 0
    ? (goals.reduce((acc, g) => acc + Number(g.progress || 0), 0) / goals.length).toFixed(1)
    : '0.0';

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-xs text-gray-500 font-medium">Loading executive dashboard...</div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Welcome Banner */}
      <div className="bg-white p-6 rounded-2xl border border-gray-200 shadow-xs flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <div className="flex items-center space-x-2">
            <h1 className="text-xl font-bold text-gray-900">
              Welcome back, {user?.employee?.firstName || user?.username}!
            </h1>
            <span className="text-xs px-2 py-0.5 rounded-full font-bold bg-indigo-50 text-indigo-700 border border-indigo-200">
              {role}
            </span>
          </div>
          <p className="text-xs text-gray-500 mt-1">
            {user?.employee?.designation} • {user?.employee?.department} • Cycle:{' '}
            <span className="font-semibold text-gray-800">{activeCycle?.name || 'No Active Cycle'}</span>
          </p>
        </div>

        <div className="flex items-center space-x-2">
          {role === 'ADMIN' ? (
            <button
              onClick={() => onNavigate('/admin/data-import')}
              className="px-3 py-2 bg-slate-900 hover:bg-slate-800 text-white rounded-lg text-xs font-semibold shadow-xs flex items-center space-x-1.5 transition cursor-pointer"
            >
              <Database className="w-3.5 h-3.5" />
              <span>Data Ingestion Studio</span>
            </button>
          ) : role === 'MANAGER' ? (
            <button
              onClick={() => onNavigate('/reviews')}
              className="px-3 py-2 bg-slate-900 hover:bg-slate-800 text-white rounded-lg text-xs font-semibold shadow-xs flex items-center space-x-1.5 transition cursor-pointer"
            >
              <Award className="w-3.5 h-3.5" />
              <span>Review Scoring Studio</span>
            </button>
          ) : (
            <button
              onClick={() => onNavigate('/goals')}
              className="px-3 py-2 bg-slate-900 hover:bg-slate-800 text-white rounded-lg text-xs font-semibold shadow-xs flex items-center space-x-1.5 transition cursor-pointer"
            >
              <Target className="w-3.5 h-3.5" />
              <span>Update OKR Goals</span>
            </button>
          )}
        </div>
      </div>

      {/* KPI Cards Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* KPI 1 */}
        <div className="bg-white p-5 rounded-xl border border-gray-200 shadow-xs">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-gray-500">
              {role === 'ADMIN' ? 'Review Cycle Status' : 'My Active Goals'}
            </span>
            <div className="p-2 bg-indigo-50 text-indigo-600 rounded-lg">
              {role === 'ADMIN' ? <CalendarCheck2 className="w-4 h-4" /> : <Target className="w-4 h-4" />}
            </div>
          </div>
          <div className="mt-3 flex items-baseline space-x-2">
            <span className="text-2xl font-bold text-gray-900">
              {role === 'ADMIN' ? activeCycle?.status || 'N/A' : goals.length}
            </span>
            {role !== 'ADMIN' && (
              <span className="text-xs text-gray-400">goals committed</span>
            )}
          </div>
          <div className="mt-2 text-[11px] text-gray-400">
            {role === 'ADMIN' ? activeCycle?.name : `${goals.filter((g) => g.status === 'COMPLETED').length} goals completed`}
          </div>
        </div>

        {/* KPI 2 */}
        <div className="bg-white p-5 rounded-xl border border-gray-200 shadow-xs">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-gray-500">
              {role === 'ADMIN' ? 'Ingestion State' : 'Average OKR Progress'}
            </span>
            <div className="p-2 bg-emerald-50 text-emerald-600 rounded-lg">
              <TrendingUp className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3 flex items-baseline space-x-2">
            <span className="text-2xl font-bold text-gray-900">
              {role === 'ADMIN' ? importStatus?.status || 'READY' : `${avgProgress}%`}
            </span>
          </div>
          <div className="mt-2">
            <div className="w-full bg-gray-100 rounded-full h-1.5 overflow-hidden">
              <div
                className="bg-emerald-500 h-1.5 rounded-full transition-all duration-500"
                style={{ width: `${Math.min(100, Math.max(0, Number(avgProgress)))}%` }}
              />
            </div>
          </div>
        </div>

        {/* KPI 3 */}
        <div className="bg-white p-5 rounded-xl border border-gray-200 shadow-xs">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-gray-500">
              {role === 'MANAGER' ? 'Direct Reports' : '360° Feedback Rating'}
            </span>
            <div className="p-2 bg-blue-50 text-blue-600 rounded-lg">
              {role === 'MANAGER' ? <Users className="w-4 h-4" /> : <Award className="w-4 h-4" />}
            </div>
          </div>
          <div className="mt-3 flex items-baseline space-x-2">
            <span className="text-2xl font-bold text-gray-900">
              {role === 'MANAGER'
                ? directReports.length
                : feedbackSummary?.averageOverallRating
                ? `${feedbackSummary.averageOverallRating} / 5.0`
                : 'Pending'}
            </span>
          </div>
          <div className="mt-2 text-[11px] text-gray-400">
            {role === 'MANAGER'
              ? 'Managed engineers'
              : `${feedbackSummary?.totalFeedbacks || 0} reviews received`}
          </div>
        </div>

        {/* KPI 4 */}
        <div className="bg-white p-5 rounded-xl border border-gray-200 shadow-xs">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-gray-500">Growth & Dev Plans</span>
            <div className="p-2 bg-amber-50 text-amber-600 rounded-lg">
              <Compass className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3 flex items-baseline space-x-2">
            <span className="text-2xl font-bold text-gray-900">
              {devPlans.length > 0 ? devPlans[0].status : 'No Plan'}
            </span>
          </div>
          <div className="mt-2 text-[11px] text-gray-400">
            {devPlans.length > 0 ? devPlans[0].title : 'Create new development plan'}
          </div>
        </div>
      </div>

      {/* Main Section */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left 2 Cols: Active Goals or Team Reports */}
        <div className="lg:col-span-2 space-y-4">
          <div className="bg-white p-5 rounded-xl border border-gray-200 shadow-xs">
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center space-x-2">
                <Target className="w-4 h-4 text-gray-500" />
                <h2 className="text-sm font-bold text-gray-900">
                  {role === 'MANAGER' ? 'Direct Reports & Team Progress' : 'My Active OKR Goals'}
                </h2>
              </div>
              <button
                onClick={() => onNavigate(role === 'MANAGER' ? '/team' : '/goals')}
                className="text-xs font-semibold text-indigo-600 hover:text-indigo-800 flex items-center space-x-1 cursor-pointer"
              >
                <span>View all</span>
                <ArrowUpRight className="w-3.5 h-3.5" />
              </button>
            </div>

            {role === 'MANAGER' && directReports.length > 0 ? (
              <div className="divide-y divide-gray-100">
                {directReports.map((emp) => (
                  <div key={emp.id} className="py-3 flex items-center justify-between">
                    <div className="flex items-center space-x-3">
                      <div className="w-8 h-8 rounded-full bg-slate-800 text-white flex items-center justify-center font-bold text-xs">
                        {emp.firstName[0]}{emp.lastName[0]}
                      </div>
                      <div>
                        <p className="text-xs font-semibold text-gray-900">{emp.firstName} {emp.lastName}</p>
                        <p className="text-[10px] text-gray-500">{emp.designation}</p>
                      </div>
                    </div>
                    <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-gray-100 text-gray-700">
                      {emp.status}
                    </span>
                  </div>
                ))}
              </div>
            ) : goals.length > 0 ? (
              <div className="space-y-3">
                {goals.slice(0, 4).map((g) => (
                  <div key={g.id} className="p-3 bg-gray-50 rounded-lg border border-gray-100">
                    <div className="flex items-center justify-between mb-1.5">
                      <span className="text-xs font-semibold text-gray-900">{g.title}</span>
                      <span className="text-xs font-bold text-gray-900">{g.progress}%</span>
                    </div>
                    <div className="w-full bg-gray-200 rounded-full h-1.5">
                      <div
                        className="bg-indigo-600 h-1.5 rounded-full"
                        style={{ width: `${g.progress}%` }}
                      />
                    </div>
                    <div className="flex items-center justify-between mt-2 text-[10px] text-gray-400">
                      <span>Category: {g.category}</span>
                      <span>Weight: {g.weight}%</span>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="text-center py-8 text-xs text-gray-400">
                No active goals found for this cycle. Click below to add goals.
              </div>
            )}
          </div>
        </div>

        {/* Right Col: Quick Actions & Cycle Context */}
        <div className="space-y-4">
          <div className="bg-white p-5 rounded-xl border border-gray-200 shadow-xs">
            <h3 className="text-xs font-bold text-gray-900 uppercase tracking-wider mb-3">
              Quick Shortcuts
            </h3>
            <div className="space-y-2">
              <button
                onClick={() => onNavigate('/goals')}
                className="w-full text-left p-2.5 rounded-lg border border-gray-100 hover:bg-gray-50 flex items-center justify-between text-xs font-medium text-gray-700 transition cursor-pointer"
              >
                <div className="flex items-center space-x-2">
                  <Target className="w-4 h-4 text-indigo-600" />
                  <span>Update Goal Progress</span>
                </div>
                <ArrowUpRight className="w-3.5 h-3.5 text-gray-400" />
              </button>

              <button
                onClick={() => onNavigate('/check-ins')}
                className="w-full text-left p-2.5 rounded-lg border border-gray-100 hover:bg-gray-50 flex items-center justify-between text-xs font-medium text-gray-700 transition cursor-pointer"
              >
                <div className="flex items-center space-x-2">
                  <Clock className="w-4 h-4 text-emerald-600" />
                  <span>Log Goal Check-in</span>
                </div>
                <ArrowUpRight className="w-3.5 h-3.5 text-gray-400" />
              </button>

              <button
                onClick={() => onNavigate('/feedback')}
                className="w-full text-left p-2.5 rounded-lg border border-gray-100 hover:bg-gray-50 flex items-center justify-between text-xs font-medium text-gray-700 transition cursor-pointer"
              >
                <div className="flex items-center space-x-2">
                  <Award className="w-4 h-4 text-blue-600" />
                  <span>360° Feedback Matrix</span>
                </div>
                <ArrowUpRight className="w-3.5 h-3.5 text-gray-400" />
              </button>

              <button
                onClick={() => onNavigate('/development-plans')}
                className="w-full text-left p-2.5 rounded-lg border border-gray-100 hover:bg-gray-50 flex items-center justify-between text-xs font-medium text-gray-700 transition cursor-pointer"
              >
                <div className="flex items-center space-x-2">
                  <Compass className="w-4 h-4 text-amber-600" />
                  <span>Career Growth Plan</span>
                </div>
                <ArrowUpRight className="w-3.5 h-3.5 text-gray-400" />
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
