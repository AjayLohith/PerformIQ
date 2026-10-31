import axios from 'axios';
import {
  AppUser,
  Employee,
  ReviewCycle,
  ReviewCycleStatus,
  Goal,
  GoalStatus,
  GoalDetails,
  KeyResult,
  CheckIn,
  Feedback,
  FeedbackSummary,
  Competency,
  Review,
  ReviewDetails,
  DevelopmentPlan,
  DevelopmentPlanDetails,
  DataImportRun,
  ValidationReport,
  AnomalyCase,
  AuditLog,
  PageResponse,
  MetricType,
  FeedbackType,
} from '../types';

const api = axios.create({
  baseURL: '/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor: inject JWT token
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('performiq_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Response interceptor: handle 401
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('performiq_token');
      localStorage.removeItem('performiq_user');
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export const authApi = {
  login: async (username: string, password: string) => {
    const res = await api.post('/auth/login', { username, password });
    return res.data;
  },
  getMe: async (): Promise<AppUser> => {
    const res = await api.get('/auth/me');
    return res.data;
  },
};

export const employeeApi = {
  getEmployees: async (department?: string, page = 0, size = 20): Promise<PageResponse<Employee>> => {
    const params = new URLSearchParams({ page: page.toString(), size: size.toString() });
    if (department) params.append('department', department);
    const res = await api.get(`/employees?${params.toString()}`);
    return res.data;
  },
  getEmployeeById: async (id: string): Promise<Employee> => {
    const res = await api.get(`/employees/${id}`);
    return res.data;
  },
  getDirectReports: async (managerEmployeeId: string): Promise<Employee[]> => {
    const res = await api.get(`/employees/${managerEmployeeId}/direct-reports`);
    return res.data;
  },
};

export const reviewCycleApi = {
  getReviewCycles: async (): Promise<PageResponse<ReviewCycle>> => {
    const res = await api.get('/review-cycles?page=0&size=50');
    return res.data;
  },
  getActiveCycle: async (): Promise<ReviewCycle> => {
    const res = await api.get('/review-cycles/active');
    return res.data;
  },
  createCycle: async (data: { businessId?: string; name: string; description?: string; startDate: string; endDate: string }): Promise<ReviewCycle> => {
    const res = await api.post('/review-cycles', data);
    return res.data;
  },
  transitionStatus: async (id: string, targetStatus: ReviewCycleStatus, reason?: string): Promise<ReviewCycle> => {
    const res = await api.put(`/review-cycles/${id}/transition`, { targetStatus, reason });
    return res.data;
  },
};

export const goalApi = {
  getGoals: async (employeeId: string, reviewCycleId?: string): Promise<Goal[]> => {
    const params = new URLSearchParams({ employeeId });
    if (reviewCycleId) params.append('reviewCycleId', reviewCycleId);
    const res = await api.get(`/goals?${params.toString()}`);
    return res.data;
  },
  getGoalDetails: async (id: string): Promise<GoalDetails> => {
    const res = await api.get(`/goals/${id}`);
    return res.data;
  },
  createGoal: async (data: { employeeId: string; reviewCycleId: string; title: string; description?: string; category?: string; weight: number }): Promise<Goal> => {
    const res = await api.post('/goals', data);
    return res.data;
  },
  updateGoalStatus: async (id: string, status: GoalStatus, reason?: string): Promise<Goal> => {
    const res = await api.patch(`/goals/${id}/status`, { status, reason });
    return res.data;
  },
  updateGoalProgress: async (id: string, progress: number, notes?: string): Promise<Goal> => {
    const res = await api.patch(`/goals/${id}/progress`, { progress, notes });
    return res.data;
  },
};

export const keyResultApi = {
  getKeyResults: async (goalId: string): Promise<KeyResult[]> => {
    const res = await api.get(`/key-results?goalId=${goalId}`);
    return res.data;
  },
  createKeyResult: async (data: {
    goalId: string;
    title: string;
    metricType: MetricType;
    startValue: number;
    targetValue: number;
    currentValue?: number;
    weight?: number;
    unit?: string;
  }): Promise<KeyResult> => {
    const res = await api.post('/key-results', data);
    return res.data;
  },
  updateCurrentValue: async (id: string, currentValue: number, note?: string): Promise<KeyResult> => {
    const res = await api.patch(`/key-results/${id}/value`, { currentValue, note });
    return res.data;
  },
};

export const checkInApi = {
  getCheckInsForGoal: async (goalId: string): Promise<CheckIn[]> => {
    const res = await api.get(`/check-ins?goalId=${goalId}`);
    return res.data;
  },
  recordCheckIn: async (data: {
    goalId: string;
    notes: string;
    progressSnapshot: number;
    checkInDate?: string;
    evidenceList?: Array<{ title: string; url?: string; description?: string; evidenceType?: string }>;
  }): Promise<CheckIn> => {
    const res = await api.post('/check-ins', data);
    return res.data;
  },
};

export const feedbackApi = {
  getFeedbacksForEmployee: async (employeeId: string, cycleId: string): Promise<Feedback[]> => {
    const res = await api.get(`/feedbacks/employee/${employeeId}/cycle/${cycleId}`);
    return res.data;
  },
  getFeedbackSummary: async (employeeId: string, reviewCycleId: string): Promise<FeedbackSummary> => {
    const res = await api.get(`/feedbacks/summary?employeeId=${employeeId}&reviewCycleId=${reviewCycleId}`);
    return res.data;
  },
  getCompetencies: async (): Promise<Competency[]> => {
    const res = await api.get('/feedbacks/competencies');
    return res.data;
  },
  requestFeedback: async (data: { employeeId: string; reviewerId: string; reviewCycleId: string; feedbackType: FeedbackType }): Promise<Feedback> => {
    const res = await api.post('/feedbacks/request', data);
    return res.data;
  },
  submitFeedback: async (
    id: string,
    data: { overallRating: number; comments?: string; competencyRatings?: Array<{ competencyId: string; rating: number; comments?: string }> }
  ): Promise<Feedback> => {
    const res = await api.post(`/feedbacks/${id}/submit`, data);
    return res.data;
  },
};

export const reviewApi = {
  getReviewsInCycle: async (cycleId: string): Promise<PageResponse<Review>> => {
    const res = await api.get(`/reviews/cycle/${cycleId}`);
    return res.data;
  },
  getReviewById: async (id: string): Promise<ReviewDetails> => {
    const res = await api.get(`/reviews/${id}`);
    return res.data;
  },
  getReviewForEmployee: async (employeeId: string, cycleId: string): Promise<ReviewDetails> => {
    const res = await api.get(`/reviews/employee/${employeeId}/cycle/${cycleId}`);
    return res.data;
  },
  scoreReview: async (
    id: string,
    data: { summary?: string; recommendation?: string; goalScores?: Array<{ goalId: string; score: number; comments?: string }> }
  ): Promise<Review> => {
    const res = await api.post(`/reviews/${id}/score`, data);
    return res.data;
  },
};

export const developmentPlanApi = {
  getPlansForEmployee: async (employeeId: string): Promise<DevelopmentPlan[]> => {
    const res = await api.get(`/development-plans/employee/${employeeId}`);
    return res.data;
  },
  getPlanDetails: async (id: string): Promise<DevelopmentPlanDetails> => {
    const res = await api.get(`/development-plans/${id}`);
    return res.data;
  },
  createPlan: async (data: {
    employeeId: string;
    reviewId?: string;
    title: string;
    description?: string;
    objectives: string;
    skillsToImprove?: string;
    actionItems?: string;
    targetDate?: string;
  }): Promise<DevelopmentPlan> => {
    const res = await api.post('/development-plans', data);
    return res.data;
  },
  submitPlan: async (id: string, comments?: string): Promise<DevelopmentPlan> => {
    const res = await api.post(`/development-plans/${id}/submit`, { comments });
    return res.data;
  },
  approvePlan: async (id: string, comments?: string): Promise<DevelopmentPlan> => {
    const res = await api.post(`/development-plans/${id}/approve`, { comments });
    return res.data;
  },
  rejectPlan: async (id: string, comments: string): Promise<DevelopmentPlan> => {
    const res = await api.post(`/development-plans/${id}/reject`, { comments });
    return res.data;
  },
  startPlan: async (id: string): Promise<DevelopmentPlan> => {
    const res = await api.post(`/development-plans/${id}/start`);
    return res.data;
  },
  completePlan: async (id: string, comments?: string): Promise<DevelopmentPlan> => {
    const res = await api.post(`/development-plans/${id}/complete`, { comments });
    return res.data;
  },
};

export const adminApi = {
  triggerImport: async (runValidation = true): Promise<any> => {
    const res = await api.post('/admin/import', { runValidation });
    return res.data;
  },
  getImportStatus: async (): Promise<DataImportRun> => {
    const res = await api.get('/admin/import/status');
    return res.data;
  },
  getImportRuns: async (): Promise<DataImportRun[]> => {
    const res = await api.get('/admin/import/runs');
    return res.data;
  },
  getValidationReport: async (): Promise<ValidationReport> => {
    const res = await api.get('/admin/validation/report');
    return res.data;
  },
  getAnomalies: async (): Promise<AnomalyCase[]> => {
    const res = await api.get('/admin/validation/anomalies');
    return res.data;
  },
  getAuditLogs: async (page = 0, size = 20): Promise<PageResponse<AuditLog>> => {
    const res = await api.get(`/admin/audit-logs?page=${page}&size=${size}`);
    return res.data;
  },
};

export default api;
