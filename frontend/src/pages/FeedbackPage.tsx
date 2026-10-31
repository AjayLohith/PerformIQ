import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { feedbackApi, reviewCycleApi, employeeApi } from '../services/api';
import {
  Feedback,
  FeedbackSummary,
  Competency,
  ReviewCycle,
  Employee,
  FeedbackType,
} from '../types';
import {
  MessageSquare,
  Award,
  Star,
  Plus,
  Send,
  Users,
  TrendingUp,
  Sliders,
  CheckCircle2,
} from 'lucide-react';

export const FeedbackPage: React.FC = () => {
  const { user } = useAuth();
  const [feedbacks, setFeedbacks] = useState<Feedback[]>([]);
  const [summary, setSummary] = useState<FeedbackSummary | null>(null);
  const [competencies, setCompetencies] = useState<Competency[]>([]);
  const [employees, setEmployees] = useState<Employee[]>([]);
  const [activeCycle, setActiveCycle] = useState<ReviewCycle | null>(null);
  const [activeTab, setActiveTab] = useState<'matrix' | 'received'>('matrix');

  // Modals
  const [showRequestModal, setShowRequestModal] = useState(false);
  const [showSubmitModal, setShowSubmitModal] = useState<Feedback | null>(null);

  // Request form
  const [reqReviewerId, setReqReviewerId] = useState('');
  const [reqType, setReqType] = useState<FeedbackType>('PEER');

  // Submit form
  const [submitOverallRating, setSubmitOverallRating] = useState(5);
  const [submitComments, setSubmitComments] = useState('');
  const [competencyRatings, setCompetencyRatings] = useState<Record<string, number>>({});

  useEffect(() => {
    loadFeedbackData();
  }, [user]);

  const loadFeedbackData = async () => {
    if (!user?.employee?.id) return;
    try {
      const cycle = await reviewCycleApi.getActiveCycle().catch(() => null);
      setActiveCycle(cycle);

      const comps = await feedbackApi.getCompetencies();
      setCompetencies(comps);

      // Initialize default competency ratings to 4
      const defaultRatings: Record<string, number> = {};
      comps.forEach((c) => (defaultRatings[c.id] = 4));
      setCompetencyRatings(defaultRatings);

      if (cycle?.id) {
        const list = await feedbackApi.getFeedbacksForEmployee(user.employee.id, cycle.id);
        setFeedbacks(list);

        const sum = await feedbackApi.getFeedbackSummary(user.employee.id, cycle.id);
        setSummary(sum);
      }

      const emps = await employeeApi.getEmployees(undefined, 0, 50);
      setEmployees(emps.content.filter((e) => e.id !== user.employee?.id));
      if (emps.content.length > 0) {
        setReqReviewerId(emps.content[0].id);
      }
    } catch {
      // error handled
    }
  };

  const handleRequestFeedback = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user?.employee?.id || !activeCycle?.id || !reqReviewerId) return;
    try {
      await feedbackApi.requestFeedback({
        employeeId: user.employee.id,
        reviewerId: reqReviewerId,
        reviewCycleId: activeCycle.id,
        feedbackType: reqType,
      });
      setShowRequestModal(false);
      await loadFeedbackData();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to request feedback');
    }
  };

  const handleSubmitFeedback = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!showSubmitModal) return;
    try {
      const ratingsArray = Object.entries(competencyRatings).map(([compKey, val]) => ({
        competencyId: compKey,
        rating: val,
      }));

      await feedbackApi.submitFeedback(showSubmitModal.id, {
        overallRating: submitOverallRating,
        comments: submitComments,
        competencyRatings: ratingsArray,
      });

      setShowSubmitModal(null);
      setSubmitComments('');
      await loadFeedbackData();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to submit feedback');
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-gray-900">360° Feedback & Competency Hub</h1>
          <p className="text-xs text-gray-500 mt-1">
            Multi-directional peer, manager, and upward feedback scored against enterprise rubric competencies.
          </p>
        </div>

        <button
          onClick={() => setShowRequestModal(true)}
          className="px-3.5 py-2 bg-slate-900 hover:bg-slate-800 text-white rounded-lg text-xs font-semibold shadow-xs flex items-center space-x-1.5 transition cursor-pointer"
        >
          <Plus className="w-4 h-4" />
          <span>Request 360 Feedback</span>
        </button>
      </div>

      {/* Tabs */}
      <div className="flex border-b border-gray-200 space-x-6 text-xs font-bold">
        <button
          onClick={() => setActiveTab('matrix')}
          className={`pb-2.5 transition cursor-pointer flex items-center space-x-1.5 ${
            activeTab === 'matrix'
              ? 'border-b-2 border-indigo-600 text-indigo-600'
              : 'text-gray-500 hover:text-gray-800'
          }`}
        >
          <Award className="w-4 h-4" />
          <span>360° Evaluation Matrix</span>
        </button>
        <button
          onClick={() => setActiveTab('received')}
          className={`pb-2.5 transition cursor-pointer flex items-center space-x-1.5 ${
            activeTab === 'received'
              ? 'border-b-2 border-indigo-600 text-indigo-600'
              : 'text-gray-500 hover:text-gray-800'
          }`}
        >
          <MessageSquare className="w-4 h-4" />
          <span>Feedback Items ({feedbacks.length})</span>
        </button>
      </div>

      {/* Tab 1: Matrix & Multi-Dimensional Summary */}
      {activeTab === 'matrix' && (
        <div className="space-y-6">
          {/* Top Metric Cards */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div className="bg-white p-5 rounded-xl border border-gray-200 shadow-xs">
              <span className="text-xs font-medium text-gray-500">Overall 360 Score</span>
              <div className="mt-2 flex items-baseline space-x-2">
                <span className="text-3xl font-black text-indigo-600">
                  {summary?.averageOverallRating ? `${summary.averageOverallRating}` : 'N/A'}
                </span>
                <span className="text-xs text-gray-400 font-bold">/ 5.0</span>
              </div>
              <div className="flex items-center space-x-1 mt-2 text-amber-500">
                {[1, 2, 3, 4, 5].map((star) => (
                  <Star
                    key={star}
                    className={`w-3.5 h-3.5 ${
                      summary && star <= Math.round(Number(summary.averageOverallRating))
                        ? 'fill-amber-400 text-amber-400'
                        : 'text-gray-200'
                    }`}
                  />
                ))}
              </div>
            </div>

            <div className="bg-white p-5 rounded-xl border border-gray-200 shadow-xs sm:col-span-2">
              <span className="text-xs font-medium text-gray-500 mb-2 block">
                Multi-Directional Breakdown
              </span>
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-2">
                {(['SELF', 'MANAGER', 'PEER', 'UPWARD'] as FeedbackType[]).map((type) => {
                  const val = summary?.ratingByFeedbackType?.[type] || 0;
                  return (
                    <div key={type} className="p-2.5 bg-gray-50 rounded-lg border border-gray-100">
                      <span className="text-[10px] font-bold text-gray-400 uppercase tracking-wider block">
                        {type}
                      </span>
                      <span className="text-base font-extrabold text-gray-900 mt-1 block">
                        {val > 0 ? `${val} / 5` : '—'}
                      </span>
                    </div>
                  );
                })}
              </div>
            </div>
          </div>

          {/* Competency Ratings Matrix */}
          <div className="bg-white p-6 rounded-2xl border border-gray-200 shadow-xs">
            <h3 className="text-sm font-bold text-gray-900 mb-4">
              Competency Rubric Scores (Cross-Evaluator Average)
            </h3>
            {competencies.length === 0 ? (
              <div className="text-center py-6 text-xs text-gray-400">Loading competencies...</div>
            ) : (
              <div className="space-y-4">
                {competencies.map((comp) => {
                  const score = summary?.ratingByCompetency?.[comp.name] || 0;
                  const pct = (score / 5) * 100;
                  return (
                    <div key={comp.id} className="p-3.5 bg-gray-50 rounded-xl border border-gray-100">
                      <div className="flex items-center justify-between mb-1.5">
                        <div>
                          <span className="text-xs font-bold text-gray-900">{comp.name}</span>
                          <span className="text-[10px] text-gray-400 ml-2">({comp.category})</span>
                          <p className="text-[10px] text-gray-500 mt-0.5">{comp.description}</p>
                        </div>
                        <div className="text-right">
                          <span className="text-sm font-extrabold text-indigo-600">
                            {score > 0 ? `${score} / 5.0` : 'No ratings'}
                          </span>
                        </div>
                      </div>
                      <div className="w-full bg-gray-200 rounded-full h-1.5 mt-2">
                        <div
                          className="bg-indigo-600 h-1.5 rounded-full transition-all duration-500"
                          style={{ width: `${pct}%` }}
                        />
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        </div>
      )}

      {/* Tab 2: Received & Pending Feedback Items */}
      {activeTab === 'received' && (
        <div className="space-y-4">
          {feedbacks.length === 0 ? (
            <div className="bg-white p-8 rounded-2xl border border-gray-200 text-center text-xs text-gray-400">
              No feedback items found for this cycle. Use "Request 360 Feedback" above to request input.
            </div>
          ) : (
            feedbacks.map((fb) => (
              <div
                key={fb.id}
                className="bg-white p-5 rounded-xl border border-gray-200 shadow-xs space-y-3"
              >
                <div className="flex items-start justify-between">
                  <div className="flex items-center space-x-3">
                    <div className="w-8 h-8 rounded-full bg-slate-800 text-white flex items-center justify-center font-bold text-xs">
                      {fb.reviewer?.firstName?.[0] || 'R'}
                      {fb.reviewer?.lastName?.[0] || 'V'}
                    </div>
                    <div>
                      <div className="flex items-center space-x-2">
                        <span className="text-xs font-bold text-gray-900">
                          {fb.reviewer?.firstName} {fb.reviewer?.lastName}
                        </span>
                        <span className="text-[9px] font-bold px-1.5 py-0.2 rounded bg-indigo-50 text-indigo-700 border border-indigo-200">
                          {fb.feedbackType}
                        </span>
                        <span
                          className={`text-[9px] font-bold px-1.5 py-0.2 rounded ${
                            fb.status === 'SUBMITTED'
                              ? 'bg-emerald-50 text-emerald-700 border border-emerald-200'
                              : 'bg-amber-50 text-amber-700 border border-amber-200'
                          }`}
                        >
                          {fb.status}
                        </span>
                      </div>
                      <p className="text-[10px] text-gray-500">{fb.reviewer?.designation}</p>
                    </div>
                  </div>

                  {fb.overallRating && (
                    <div className="flex items-center space-x-1 px-2.5 py-1 bg-amber-50 border border-amber-200 rounded-lg text-xs font-bold text-amber-800">
                      <Star className="w-3.5 h-3.5 fill-amber-400 text-amber-400" />
                      <span>{fb.overallRating} / 5</span>
                    </div>
                  )}
                </div>

                {fb.comments && (
                  <p className="text-xs text-gray-700 bg-gray-50 p-3 rounded-lg border border-gray-100 whitespace-pre-wrap">
                    "{fb.comments}"
                  </p>
                )}

                {fb.status === 'DRAFT' && fb.reviewer?.id === user?.employee?.id && (
                  <button
                    onClick={() => setShowSubmitModal(fb)}
                    className="px-3 py-1.5 bg-slate-900 hover:bg-slate-800 text-white rounded-lg text-xs font-semibold shadow-xs flex items-center space-x-1.5 transition cursor-pointer"
                  >
                    <Send className="w-3.5 h-3.5" />
                    <span>Submit Evaluation</span>
                  </button>
                )}
              </div>
            ))
          )}
        </div>
      )}

      {/* Request Feedback Modal */}
      {showRequestModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-xs p-4">
          <div className="bg-white rounded-2xl max-w-lg w-full p-6 shadow-2xl border border-gray-200 space-y-4">
            <h3 className="text-sm font-bold text-gray-900">Request 360 Feedback</h3>
            <form onSubmit={handleRequestFeedback} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">Feedback Type</label>
                <select
                  value={reqType}
                  onChange={(e) => setReqType(e.target.value as FeedbackType)}
                  className="w-full text-xs px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                >
                  <option value="PEER">PEER (Peer Review)</option>
                  <option value="MANAGER">MANAGER (Manager Evaluation)</option>
                  <option value="UPWARD">UPWARD (Upward Feedback for Manager)</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">Select Reviewer</label>
                <select
                  value={reqReviewerId}
                  onChange={(e) => setReqReviewerId(e.target.value)}
                  className="w-full text-xs px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                >
                  {employees.map((emp) => (
                    <option key={emp.id} value={emp.id}>
                      {emp.firstName} {emp.lastName} — {emp.designation} ({emp.department})
                    </option>
                  ))}
                </select>
              </div>

              <div className="flex justify-end space-x-2 pt-2 border-t border-gray-200">
                <button
                  type="button"
                  onClick={() => setShowRequestModal(false)}
                  className="px-3 py-2 bg-gray-100 hover:bg-gray-200 text-gray-700 rounded-lg text-xs font-semibold transition cursor-pointer"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-slate-900 hover:bg-slate-800 text-white rounded-lg text-xs font-semibold transition cursor-pointer"
                >
                  Send Request
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Submit Evaluation Modal */}
      {showSubmitModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-xs p-4 overflow-y-auto">
          <div className="bg-white rounded-2xl max-w-xl w-full p-6 shadow-2xl border border-gray-200 space-y-4 my-8">
            <h3 className="text-sm font-bold text-gray-900">
              Submit Evaluation for {showSubmitModal.employee.firstName} {showSubmitModal.employee.lastName}
            </h3>

            <form onSubmit={handleSubmitFeedback} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">
                  Overall Rating ({submitOverallRating} / 5)
                </label>
                <div className="flex items-center space-x-2">
                  {[1, 2, 3, 4, 5].map((star) => (
                    <button
                      key={star}
                      type="button"
                      onClick={() => setSubmitOverallRating(star)}
                      className="p-1 cursor-pointer"
                    >
                      <Star
                        className={`w-6 h-6 ${
                          star <= submitOverallRating
                            ? 'fill-amber-400 text-amber-400'
                            : 'text-gray-300'
                        }`}
                      />
                    </button>
                  ))}
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">
                  Qualitative Comments & Coaching
                </label>
                <textarea
                  rows={3}
                  required
                  value={submitComments}
                  onChange={(e) => setSubmitComments(e.target.value)}
                  placeholder="Provide constructive feedback, strengths, and areas for improvement..."
                  className="w-full text-xs px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                />
              </div>

              {/* Rubric Competency Sliders */}
              <div className="space-y-3 pt-2 border-t border-gray-200">
                <span className="text-xs font-bold text-gray-900 uppercase tracking-wider block">
                  Competency Rubric Ratings
                </span>
                {competencies.map((c) => (
                  <div key={c.id} className="p-3 bg-gray-50 rounded-xl border border-gray-100">
                    <div className="flex justify-between items-center text-xs mb-1">
                      <span className="font-semibold text-gray-800">{c.name}</span>
                      <span className="font-bold text-indigo-600">
                        {competencyRatings[c.id] || 4} / 5
                      </span>
                    </div>
                    <input
                      type="range"
                      min="1"
                      max="5"
                      step="1"
                      value={competencyRatings[c.id] || 4}
                      onChange={(e) =>
                        setCompetencyRatings((prev) => ({
                          ...prev,
                          [c.id]: Number(e.target.value),
                        }))
                      }
                      className="w-full accent-indigo-600 cursor-pointer"
                    />
                  </div>
                ))}
              </div>

              <div className="flex justify-end space-x-2 pt-2 border-t border-gray-200">
                <button
                  type="button"
                  onClick={() => setShowSubmitModal(null)}
                  className="px-3 py-2 bg-gray-100 hover:bg-gray-200 text-gray-700 rounded-lg text-xs font-semibold transition cursor-pointer"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-slate-900 hover:bg-slate-800 text-white rounded-lg text-xs font-semibold transition cursor-pointer"
                >
                  Submit 360 Review
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
