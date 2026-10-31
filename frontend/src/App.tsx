import React from 'react';
import { BrowserRouter, Routes, Route, Navigate, useNavigate, useLocation } from 'react-router';
import { AuthProvider, useAuth } from './context/AuthContext';
import { Navbar } from './components/layout/Navbar';
import { Sidebar } from './components/layout/Sidebar';
import { LoginPage } from './pages/LoginPage';
import { DashboardPage } from './pages/DashboardPage';
import { GoalsPage } from './pages/GoalsPage';
import { CheckInsPage } from './pages/CheckInsPage';
import { FeedbackPage } from './pages/FeedbackPage';
import { DevelopmentPlansPage } from './pages/DevelopmentPlansPage';
import { TeamDashboardPage } from './pages/TeamDashboardPage';
import { ReviewStudioPage } from './pages/ReviewStudioPage';
import { PlanApprovalsPage } from './pages/PlanApprovalsPage';
import { AdminDataStudioPage } from './pages/AdminDataStudioPage';
import { ReviewCycleManagerPage } from './pages/ReviewCycleManagerPage';
import { AuditLogsPage } from './pages/AuditLogsPage';
import { EmployeeDirectoryPage } from './pages/EmployeeDirectoryPage';
import { RefreshCw } from 'lucide-react';
import './index.css';

const AppLayout: React.FC = () => {
  const { user, token, isLoading } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  if (isLoading) {
    return (
      <div className="min-h-screen bg-slate-900 flex flex-col items-center justify-center text-white">
        <div className="w-12 h-12 rounded-xl bg-indigo-600 flex items-center justify-center font-black text-xl mb-4 shadow-lg animate-pulse">
          PQ
        </div>
        <div className="flex items-center space-x-2 text-sm text-gray-300">
          <RefreshCw className="w-4 h-4 animate-spin text-indigo-400" />
          <span>Initializing PerformIQ Enterprise...</span>
        </div>
      </div>
    );
  }

  if (!token || !user) {
    return <LoginPage />;
  }

  const handleNavigate = (path: string) => {
    navigate(path);
  };

  return (
    <div className="flex h-screen bg-slate-50/50 overflow-hidden font-sans text-gray-900 antialiased">
      {/* Fixed Left Sidebar */}
      <Sidebar currentPath={location.pathname} onNavigate={handleNavigate} />

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
        {/* Top Navbar */}
        <Navbar />

        {/* Page Content */}
        <main className="flex-1 overflow-y-auto p-6 scrollbar-thin">
          <Routes>
            <Route path="/" element={<DashboardPage onNavigate={handleNavigate} />} />
            <Route path="/dashboard" element={<DashboardPage onNavigate={handleNavigate} />} />
            <Route path="/goals" element={<GoalsPage />} />
            <Route path="/check-ins" element={<CheckInsPage />} />
            <Route path="/feedback" element={<FeedbackPage />} />
            <Route path="/development-plans" element={<DevelopmentPlansPage />} />
            <Route path="/team" element={<TeamDashboardPage onNavigate={handleNavigate} />} />
            <Route path="/reviews" element={<ReviewStudioPage />} />
            <Route path="/plan-approvals" element={<PlanApprovalsPage />} />
            <Route path="/admin/data-import" element={<AdminDataStudioPage />} />
            <Route path="/admin/review-cycles" element={<ReviewCycleManagerPage />} />
            <Route path="/admin/audit-logs" element={<AuditLogsPage />} />
            <Route path="/directory" element={<EmployeeDirectoryPage />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </main>
      </div>
    </div>
  );
};

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <AppLayout />
      </BrowserRouter>
    </AuthProvider>
  );
}
