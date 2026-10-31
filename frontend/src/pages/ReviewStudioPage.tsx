import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { reviewApi, reviewCycleApi, feedbackApi } from '../services/api';
import { Review, ReviewDetails, ReviewCycle, FeedbackSummary } from '../types';
import {
  Award,
  CheckCircle,
  Star,
  Target,
  Sparkles,
  FileCheck2,
  Calendar,
} from 'lucide-react';

export const ReviewStudioPage: React.FC = () => {
  const { user } = useAuth();
  const [activeCycle, setActiveCycle] = useState<ReviewCycle | null>(null);
  const [reviews, setReviews] = useState<Review[]>([]);
  const [selectedReviewId, setSelectedReviewId] = useState<string | null>(null);
  const [reviewDetails, setReviewDetails] = useState<ReviewDetails | null>(null);
  const [feedbackSummary, setFeedbackSummary] = useState<FeedbackSummary | null>(null);
  const [goalScores, setGoalScores] = useState<Record<string, number>>({});
  const [summaryText, setSummaryText] = useState('');
  const [recommendationText, setRecommendationText] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    loadCycleAndReviews();
  }, [user]);

  const loadCycleAndReviews = async () => {
    setIsLoading(true);
    try {
      const cycle = await reviewCycleApi.getActiveCycle().catch(() => null);
      setActiveCycle(cycle);

      if (cycle?.id) {
        const pageRes = await reviewApi.getReviewsInCycle(cycle.id);
        setReviews(pageRes.content);
        if (pageRes.content.length > 0) {
          handleSelectReview(pageRes.content[0].id, pageRes.content[0].employee.id, cycle.id);
        }
      }
    } catch {
      // error handled
    } finally {
      setIsLoading(false);
    }
  };

  const handleSelectReview = async (reviewId: string, employeeId: string, cycleId: string) => {
    setSelectedReviewId(reviewId);
    try {
      const details = await reviewApi.getReviewById(reviewId);
      setReviewDetails(details);
      setSummaryText(details.review.summary || '');
      setRecommendationText(details.review.recommendation || '');

      // Initialize goal scores
      const initialScores: Record<string, number> = {};
      details.reviewGoals.forEach((rg) => {
        initialScores[rg.goal.id] = Number(rg.goalScore) || 3.5;
      });
      setGoalScores(initialScores);

      // Fetch 360 feedback summary
      const fbSummary = await feedbackApi.getFeedbackSummary(employeeId, cycleId).catch(() => null);
      setFeedbackSummary(fbSummary);
    } catch {
      // error handled
    }
  };

  // Live Composite Score Math: 60% Goals + 40% Feedback
  const scoresArray = Object.values(goalScores);
  const avgGoalScore = scoresArray.length > 0
    ? scoresArray.reduce((acc, s) => acc + s, 0) / scoresArray.length
    : 3.0;

  const fbScore = Number(feedbackSummary?.averageOverallRating || avgGoalScore);
  const liveCompositeScore = ((avgGoalScore * 0.6) + (fbScore * 0.4)).toFixed(2);

  const handleScoreReview = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedReviewId) return;
    setIsSubmitting(true);
    try {
      const payloadGoalScores = Object.entries(goalScores).map(([gId, score]) => ({
        goalId: gId,
        score,
      }));

      await reviewApi.scoreReview(selectedReviewId, {
        summary: summaryText,
        recommendation: recommendationText,
        goalScores: payloadGoalScores,
      });

      alert('Performance review scored and completed successfully!');
      if (activeCycle?.id) {
        const pageRes = await reviewApi.getReviewsInCycle(activeCycle.id);
        setReviews(pageRes.content);
      }
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to complete review');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-xl font-bold text-gray-900">Performance Review Scoring Studio</h1>
        <p className="text-xs text-gray-500 mt-1">
          Formal evaluations utilizing 60% Goal Achievement + 40% 360 Feedback composite scoring engine.
        </p>
      </div>

      {isLoading ? (
        <div className="text-center py-12 text-xs text-gray-400">Loading review records...</div>
      ) : reviews.length === 0 ? (
        <div className="bg-white p-12 rounded-2xl border border-gray-200 text-center text-xs text-gray-400">
          No review records available in the active cycle. Reviews open when cycle enters UNDER_REVIEW.
        </div>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Left Col: Review Selection List */}
          <div className="space-y-3">
            <h3 className="text-xs font-bold text-gray-400 uppercase tracking-wider px-1">
              Select Review ({reviews.length})
            </h3>
            <div className="space-y-2">
              {reviews.map((r) => {
                const isSelected = selectedReviewId === r.id;
                return (
                  <button
                    key={r.id}
                    onClick={() => handleSelectReview(r.id, r.employee.id, r.reviewCycle.id)}
                    className={`w-full text-left p-3.5 rounded-xl border transition cursor-pointer ${
                      isSelected
                        ? 'bg-slate-900 text-white border-slate-900 shadow-xs'
                        : 'bg-white text-gray-800 border-gray-200 hover:bg-gray-50'
                    }`}
                  >
                    <div className="flex items-center justify-between">
                      <span className="text-xs font-bold truncate">
                        {r.employee.firstName} {r.employee.lastName}
                      </span>
                      <span
                        className={`text-[9px] font-bold px-1.5 py-0.2 rounded ${
                          r.status === 'COMPLETED'
                            ? 'bg-emerald-100 text-emerald-800'
                            : isSelected
                            ? 'bg-slate-800 text-amber-300'
                            : 'bg-amber-100 text-amber-800'
                        }`}
                      >
                        {r.status}
                      </span>
                    </div>
                    <p className={`text-[10px] truncate mt-1 ${isSelected ? 'text-slate-300' : 'text-gray-500'}`}>
                      {r.employee.designation} • {r.employee.department}
                    </p>
                    {r.overallScore && (
                      <div className="mt-2 text-xs font-bold text-emerald-400">
                        Final Score: {r.overallScore} / 5.0
                      </div>
                    )}
                  </button>
                );
              })}
            </div>
          </div>

          {/* Right 2 Cols: Evaluation Scoring Studio */}
          <div className="lg:col-span-2 space-y-6">
            {reviewDetails && (
              <form onSubmit={handleScoreReview} className="space-y-6">
                {/* Employee Header & Composite Calculation Bar */}
                <div className="bg-white p-5 rounded-2xl border border-gray-200 shadow-xs space-y-4">
                  <div className="flex items-center justify-between">
                    <div>
                      <span className="text-[10px] font-bold text-gray-400 uppercase tracking-wider">
                        {reviewDetails.review.businessId}
                      </span>
                      <h2 className="text-base font-bold text-gray-900">
                        {reviewDetails.review.employee.firstName} {reviewDetails.review.employee.lastName}
                      </h2>
                      <p className="text-xs text-gray-500">
                        {reviewDetails.review.employee.designation} • {reviewDetails.review.employee.department}
                      </p>
                    </div>

                    {/* Live Composite Score Widget */}
                    <div className="p-3 bg-indigo-50 border border-indigo-200 rounded-xl text-right">
                      <span className="text-[10px] font-bold text-indigo-700 uppercase tracking-wider block">
                        Composite Rating
                      </span>
                      <span className="text-2xl font-black text-indigo-900">
                        {reviewDetails.review.status === 'COMPLETED'
                          ? reviewDetails.review.overallScore
                          : liveCompositeScore}
                        <span className="text-xs text-indigo-500 font-bold"> / 5.0</span>
                      </span>
                    </div>
                  </div>

                  {/* Math Formula Transparency Banner */}
                  <div className="p-3 bg-gray-50 rounded-xl border border-gray-100 text-[11px] text-gray-600 flex items-center justify-between">
                    <div>
                      <span className="font-semibold text-gray-800">Formula: </span>
                      <span>(60% × Goal Score: {avgGoalScore.toFixed(2)}) + (40% × 360 Feedback: {fbScore.toFixed(2)})</span>
                    </div>
                    <span className="font-bold text-indigo-600">Calculated Live</span>
                  </div>
                </div>

                {/* Scored Goals Section */}
                <div className="bg-white p-5 rounded-2xl border border-gray-200 shadow-xs space-y-4">
                  <div className="flex items-center space-x-2">
                    <Target className="w-4 h-4 text-indigo-600" />
                    <h3 className="text-xs font-bold text-gray-900 uppercase tracking-wider">
                      Goal Achievement Scoring (Scale 1.0 to 5.0)
                    </h3>
                  </div>

                  <div className="space-y-4">
                    {reviewDetails.reviewGoals.map((rg) => {
                      const currentVal = goalScores[rg.goal.id] || 3.5;
                      return (
                        <div
                          key={rg.id}
                          className="p-4 bg-gray-50 rounded-xl border border-gray-100 space-y-2"
                        >
                          <div className="flex items-center justify-between">
                            <div>
                              <span className="text-xs font-bold text-gray-900">{rg.goal.title}</span>
                              <span className="text-[10px] text-gray-400 ml-2">
                                (Weight: {rg.goal.weight}%, Progress: {rg.goal.progress}%)
                              </span>
                            </div>
                            <span className="text-sm font-extrabold text-indigo-600">
                              {currentVal.toFixed(1)} / 5.0
                            </span>
                          </div>

                          <input
                            type="range"
                            min="1.0"
                            max="5.0"
                            step="0.1"
                            disabled={reviewDetails.review.status === 'COMPLETED'}
                            value={currentVal}
                            onChange={(e) =>
                              setGoalScores((prev) => ({
                                ...prev,
                                [rg.goal.id]: Number(e.target.value),
                              }))
                            }
                            className="w-full accent-indigo-600 cursor-pointer disabled:opacity-50"
                          />
                        </div>
                      );
                    })}
                  </div>
                </div>

                {/* Qualitative Feedback & Recommendations */}
                <div className="bg-white p-5 rounded-2xl border border-gray-200 shadow-xs space-y-4">
                  <div>
                    <label className="block text-xs font-semibold text-gray-700 mb-1">
                      Performance Summary & Key Strengths
                    </label>
                    <textarea
                      rows={3}
                      disabled={reviewDetails.review.status === 'COMPLETED'}
                      value={summaryText}
                      onChange={(e) => setSummaryText(e.target.value)}
                      placeholder="Summarize employee deliverables, strengths, and business impact..."
                      className="w-full text-xs px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none disabled:bg-gray-50"
                    />
                  </div>

                  <div>
                    <label className="block text-xs font-semibold text-gray-700 mb-1">
                      Development Recommendation & Promotion Readiness
                    </label>
                    <textarea
                      rows={2}
                      disabled={reviewDetails.review.status === 'COMPLETED'}
                      value={recommendationText}
                      onChange={(e) => setRecommendationText(e.target.value)}
                      placeholder="Recommendations for promotion, compensation adjustments, or growth areas..."
                      className="w-full text-xs px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none disabled:bg-gray-50"
                    />
                  </div>

                  {reviewDetails.review.status !== 'COMPLETED' && (
                    <div className="flex justify-end pt-2">
                      <button
                        type="submit"
                        disabled={isSubmitting}
                        className="px-5 py-2.5 bg-slate-900 hover:bg-slate-800 text-white rounded-lg text-xs font-semibold shadow-xs flex items-center space-x-1.5 transition cursor-pointer disabled:opacity-50"
                      >
                        <FileCheck2 className="w-4 h-4" />
                        <span>{isSubmitting ? 'Sealing...' : 'Complete & Finalize Review'}</span>
                      </button>
                    </div>
                  )}
                </div>
              </form>
            )}
          </div>
        </div>
      )}
    </div>
  );
};
