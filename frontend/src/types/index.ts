export type UserRole = 'ADMIN' | 'MANAGER' | 'EMPLOYEE' | 'HR_PARTNER' | 'OPERATIONS';

export interface EmployeeSummary {
  id: string;
  businessId: string;
  firstName: string;
  lastName: string;
  email: string;
  department: string;
  designation: string;
}

export interface AppUser {
  userId: string;
  username: string;
  role: UserRole;
  enabled?: boolean;
  employee?: EmployeeSummary | null;
}

export interface Employee {
  id: string;
  businessId: string;
  firstName: string;
  lastName: string;
  email: string;
  department: string;
  designation: string;
  joiningDate: string;
  status: 'ACTIVE' | 'PROBATION' | 'TERMINATED' | 'ON_LEAVE';
  currentManager?: {
    managerId: string;
    managerBusinessId: string;
    name: string;
    email: string;
    designation: string;
  } | null;
}

export type ReviewCycleStatus = 'DRAFT' | 'ACTIVE' | 'UNDER_REVIEW' | 'CLOSED';

export interface ReviewCycle {
  id: string;
  businessId: string;
  name: string;
  description?: string;
  startDate: string;
  endDate: string;
  status: ReviewCycleStatus;
  version?: number;
}

export type GoalStatus = 'DRAFT' | 'ACTIVE' | 'COMPLETED' | 'CANCELLED';

export interface Goal {
  id: string;
  businessId: string;
  title: string;
  description?: string;
  category: string;
  status: GoalStatus;
  weight: number;
  progress: number;
  createdAt?: string;
  updatedAt?: string;
}

export type MetricType = 'INCREASE' | 'DECREASE';

export interface KeyResult {
  id: string;
  businessId: string;
  title: string;
  metricType: MetricType;
  startValue: number;
  targetValue: number;
  currentValue: number;
  weight: number;
  unit: string;
  progress: number;
  createdAt?: string;
}

export interface Evidence {
  id: string;
  businessId: string;
  title: string;
  url?: string;
  description?: string;
  evidenceType: 'DOCUMENT' | 'URL' | 'CERTIFICATION' | 'CODE_PR';
  createdAt?: string;
}

export interface CheckIn {
  id: string;
  businessId: string;
  notes: string;
  progressSnapshot: number;
  checkInDate: string;
  createdAt?: string;
  employee?: EmployeeSummary;
}

export interface GoalDetails {
  goal: Goal;
  keyResults: KeyResult[];
  checkIns: CheckIn[];
}

export type FeedbackType = 'SELF' | 'MANAGER' | 'PEER' | 'UPWARD';
export type FeedbackStatus = 'DRAFT' | 'SUBMITTED';

export interface Competency {
  id: string;
  businessId: string;
  name: string;
  description: string;
  category: string;
}

export interface Feedback {
  id: string;
  businessId: string;
  employee: EmployeeSummary;
  reviewer: EmployeeSummary;
  feedbackType: FeedbackType;
  comments?: string;
  overallRating?: number;
  status: FeedbackStatus;
  submittedAt?: string;
}

export interface FeedbackSummary {
  employeeId: string;
  reviewCycleId: string;
  totalFeedbacks: number;
  averageOverallRating: number;
  ratingByFeedbackType: Record<FeedbackType, number>;
  ratingByCompetency: Record<string, number>;
}

export type ReviewStatus = 'PENDING' | 'IN_PROGRESS' | 'COMPLETED';

export interface ReviewGoal {
  id: string;
  goal: Goal;
  goalScore?: number;
  comments?: string;
}

export interface Review {
  id: string;
  businessId: string;
  employee: EmployeeSummary;
  reviewer: EmployeeSummary;
  reviewCycle: ReviewCycle;
  status: ReviewStatus;
  overallScore?: number;
  summary?: string;
  recommendation?: string;
  completedAt?: string;
}

export interface ReviewDetails {
  review: Review;
  reviewGoals: ReviewGoal[];
}

export type DevelopmentPlanStatus = 'DRAFT' | 'SUBMITTED' | 'APPROVED' | 'REJECTED' | 'IN_PROGRESS' | 'COMPLETED';
export type ApprovalAction = 'SUBMITTED' | 'APPROVED' | 'REJECTED';

export interface ApprovalHistory {
  id: string;
  approver: EmployeeSummary;
  action: ApprovalAction;
  comments?: string;
  actionDate: string;
}

export interface DevelopmentPlan {
  id: string;
  businessId: string;
  employee: EmployeeSummary;
  title: string;
  description?: string;
  objectives: string;
  skillsToImprove?: string;
  actionItems?: string;
  targetDate?: string;
  status: DevelopmentPlanStatus;
  createdAt?: string;
  updatedAt?: string;
}

export interface DevelopmentPlanDetails {
  plan: DevelopmentPlan;
  approvalHistory: ApprovalHistory[];
}

export interface DataImportRun {
  id: string;
  datasetScale: string;
  seed: number;
  generatorVersion: string;
  manifestChecksum: string;
  status: 'PENDING' | 'VALIDATING' | 'INGESTING' | 'COMPLETED' | 'FAILED';
  totalRecords: number;
  validRecords: number;
  invalidRecords: number;
  startedAt: string;
  completedAt?: string;
  errorMessage?: string;
}

export interface AnomalyCase {
  id: string;
  businessId: string;
  issueType: string;
  severity: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';
  affectedEntity: string;
  affectedBusinessId: string;
  detectionStatus: 'DETECTED' | 'ACCEPTED' | 'FIXED' | 'FALSE_POSITIVE';
  expectedOutcome: string;
  actualOutcome: string;
  createdAt?: string;
}

export interface ValidationReport {
  generatedAt: string;
  totalAnomaliesDetected: number;
  anomaliesBySeverity: Record<string, number>;
  anomaliesByIssueType: Record<string, number>;
  anomalyDetails: AnomalyCase[];
  passedAuditChecks: string[];
}

export interface AuditLog {
  id: string;
  actor?: EmployeeSummary | null;
  action: string;
  entityType: string;
  entityId: string;
  previousValue?: string;
  newValue?: string;
  reason?: string;
  createdAt: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
