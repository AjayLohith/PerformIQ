import React from 'react';
import { useAuth } from '../../context/AuthContext';
import {
  LayoutDashboard,
  Target,
  Clock,
  MessageSquare,
  Compass,
  Users,
  Award,
  CheckCircle2,
  Database,
  CalendarCheck2,
  ShieldAlert,
  Contact2,
} from 'lucide-react';

interface SidebarProps {
  currentPath: string;
  onNavigate: (path: string) => void;
}

export const Sidebar: React.FC<SidebarProps> = ({ currentPath, onNavigate }) => {
  const { role } = useAuth();

  const isManager = role === 'MANAGER' || role === 'ADMIN';
  const isAdmin = role === 'ADMIN';

  interface NavItem {
    label: string;
    path: string;
    icon: React.ReactNode;
  }

  const employeeItems: NavItem[] = [
    { label: 'Goals & OKRs', path: '/goals', icon: <Target className="w-4 h-4" /> },
    { label: 'Check-in Timeline', path: '/check-ins', icon: <Clock className="w-4 h-4" /> },
    { label: '360° Feedback Hub', path: '/feedback', icon: <MessageSquare className="w-4 h-4" /> },
    { label: 'Development Plans', path: '/development-plans', icon: <Compass className="w-4 h-4" /> },
  ];

  const managerItems: NavItem[] = [
    { label: 'Team Overview', path: '/team', icon: <Users className="w-4 h-4" /> },
    { label: 'Review Scoring Studio', path: '/reviews', icon: <Award className="w-4 h-4" /> },
    { label: 'Plan Approvals', path: '/plan-approvals', icon: <CheckCircle2 className="w-4 h-4" /> },
  ];

  const adminItems: NavItem[] = [
    { label: 'Data Ingestion & Quality', path: '/admin/data-import', icon: <Database className="w-4 h-4" /> },
    { label: 'Review Cycle Control', path: '/admin/review-cycles', icon: <CalendarCheck2 className="w-4 h-4" /> },
    { label: 'Security & Audit Logs', path: '/admin/audit-logs', icon: <ShieldAlert className="w-4 h-4" /> },
  ];

  const renderNavGroup = (title: string, items: NavItem[]) => (
    <div className="mb-5">
      <h3 className="px-3 text-[10px] font-bold text-gray-400 uppercase tracking-wider mb-1.5">
        {title}
      </h3>
      <div className="space-y-0.5">
        {items.map((item) => {
          const isActive = currentPath === item.path;
          return (
            <button
              key={item.path}
              onClick={() => onNavigate(item.path)}
              className={`w-full flex items-center space-x-2.5 px-3 py-2 rounded-lg text-xs font-medium transition cursor-pointer text-left ${
                isActive
                  ? 'bg-slate-900 text-white shadow-xs'
                  : 'text-gray-600 hover:text-gray-900 hover:bg-gray-100'
              }`}
            >
              <span className={isActive ? 'text-white' : 'text-gray-500'}>{item.icon}</span>
              <span>{item.label}</span>
            </button>
          );
        })}
      </div>
    </div>
  );

  return (
    <aside className="w-60 bg-white border-r border-gray-200 flex flex-col shrink-0 h-screen sticky top-0">
      {/* Brand Header */}
      <div className="h-16 flex items-center px-5 border-b border-gray-200">
        <div className="flex items-center space-x-2.5">
          <div className="w-8 h-8 rounded-lg bg-slate-900 flex items-center justify-center text-white font-black text-sm tracking-wider shadow-sm">
            PQ
          </div>
          <div>
            <h1 className="text-sm font-bold text-gray-900 tracking-tight leading-none">
              Perform<span className="text-indigo-600">IQ</span>
            </h1>
            <p className="text-[10px] text-gray-400 font-medium">Enterprise Growth OS</p>
          </div>
        </div>
      </div>

      {/* Nav Content */}
      <div className="flex-1 overflow-y-auto px-3 py-4 scrollbar-thin">
        {/* Core Dashboard */}
        <div className="mb-4">
          <button
            onClick={() => onNavigate('/dashboard')}
            className={`w-full flex items-center space-x-2.5 px-3 py-2 rounded-lg text-xs font-medium transition cursor-pointer text-left ${
              currentPath === '/dashboard' || currentPath === '/'
                ? 'bg-slate-900 text-white shadow-xs'
                : 'text-gray-600 hover:text-gray-900 hover:bg-gray-100'
            }`}
          >
            <LayoutDashboard className="w-4 h-4" />
            <span>Dashboard Overview</span>
          </button>
        </div>

        {/* My Performance */}
        {renderNavGroup('My Performance', employeeItems)}

        {/* Manager Studio */}
        {isManager && renderNavGroup('Manager Studio', managerItems)}

        {/* Administration */}
        {isAdmin && renderNavGroup('System Administration', adminItems)}

        {/* Directory */}
        {renderNavGroup('Organization', [
          { label: 'Employee Directory', path: '/directory', icon: <Contact2 className="w-4 h-4" /> },
        ])}
      </div>

      {/* Footer Info */}
      <div className="p-3 border-t border-gray-200 text-center text-[10px] text-gray-400">
        PerformIQ Enterprise v1.0.0
      </div>
    </aside>
  );
};
