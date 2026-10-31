import React, { useState, useEffect } from 'react';
import { useAuth, DEMO_PERSONAS } from '../../context/AuthContext';
import { reviewCycleApi } from '../../services/api';
import { ReviewCycle } from '../../types';
import { Users, LogOut, ChevronDown, Activity, Sparkles } from 'lucide-react';

export const Navbar: React.FC = () => {
  const { user, role, switchPersona, logout } = useAuth();
  const [activeCycle, setActiveCycle] = useState<ReviewCycle | null>(null);
  const [showPersonaMenu, setShowPersonaMenu] = useState(false);

  useEffect(() => {
    const fetchActiveCycle = async () => {
      try {
        const cycle = await reviewCycleApi.getActiveCycle();
        setActiveCycle(cycle);
      } catch {
        // No active cycle or error
      }
    };
    fetchActiveCycle();
  }, []);

  const getRoleBadgeColor = (r?: string) => {
    switch (r) {
      case 'ADMIN':
        return 'bg-purple-100 text-purple-800 border-purple-200';
      case 'MANAGER':
        return 'bg-blue-100 text-blue-800 border-blue-200';
      default:
        return 'bg-emerald-100 text-emerald-800 border-emerald-200';
    }
  };

  return (
    <header className="h-16 bg-white border-b border-gray-200 px-6 flex items-center justify-between sticky top-0 z-30 shadow-xs">
      {/* Left: Active Cycle Banner */}
      <div className="flex items-center space-x-3">
        {activeCycle ? (
          <div className="flex items-center space-x-2 px-3 py-1 bg-gray-50 border border-gray-200 rounded-full text-xs font-medium text-gray-700">
            <span className="relative flex h-2 w-2">
              <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
              <span className="relative inline-flex rounded-full h-2 w-2 bg-emerald-500"></span>
            </span>
            <span className="font-semibold text-gray-900">{activeCycle.name}</span>
            <span className="text-gray-400">|</span>
            <span className="px-1.5 py-0.5 rounded text-[10px] bg-emerald-50 text-emerald-700 font-bold border border-emerald-200">
              {activeCycle.status}
            </span>
          </div>
        ) : (
          <div className="flex items-center space-x-2 px-3 py-1 bg-amber-50 border border-amber-200 rounded-full text-xs text-amber-800">
            <Activity className="w-3.5 h-3.5" />
            <span>Review Cycle Inactive</span>
          </div>
        )}
      </div>

      {/* Right: Persona Switcher & User Profile */}
      <div className="flex items-center space-x-4">
        {/* Quick Persona Switcher Dropdown */}
        <div className="relative">
          <button
            onClick={() => setShowPersonaMenu(!showPersonaMenu)}
            className="flex items-center space-x-2 px-3 py-1.5 bg-gray-100 hover:bg-gray-200 text-gray-800 rounded-lg text-xs font-medium transition cursor-pointer border border-gray-200"
            title="Switch Demo Persona"
          >
            <Sparkles className="w-3.5 h-3.5 text-indigo-600" />
            <span>Switch Persona</span>
            <ChevronDown className="w-3.5 h-3.5 text-gray-500" />
          </button>

          {showPersonaMenu && (
            <div className="absolute right-0 mt-2 w-72 bg-white rounded-xl shadow-xl border border-gray-200 py-2 z-50">
              <div className="px-3 py-1.5 border-b border-gray-100">
                <p className="text-[11px] font-semibold text-gray-400 uppercase tracking-wider">
                  Select Demo Persona
                </p>
              </div>
              <div className="py-1">
                {DEMO_PERSONAS.map((p) => (
                  <button
                    key={p.key}
                    onClick={() => {
                      switchPersona(p.key);
                      setShowPersonaMenu(false);
                    }}
                    className={`w-full text-left px-3 py-2 hover:bg-gray-50 flex items-start space-x-2.5 transition ${
                      user?.username === p.username ? 'bg-indigo-50/70 border-l-3 border-indigo-600' : ''
                    }`}
                  >
                    <div className="w-7 h-7 rounded-full bg-gray-800 text-white flex items-center justify-center font-bold text-xs shrink-0 mt-0.5">
                      {p.avatar}
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center justify-between">
                        <span className="text-xs font-semibold text-gray-900 truncate">{p.name}</span>
                        <span
                          className={`text-[9px] px-1.5 py-0.2 rounded font-bold border ${getRoleBadgeColor(
                            p.role
                          )}`}
                        >
                          {p.role}
                        </span>
                      </div>
                      <p className="text-[10px] text-gray-500 truncate">{p.designation}</p>
                    </div>
                  </button>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* Current User Card */}
        <div className="flex items-center space-x-2.5 pl-3 border-l border-gray-200">
          <div className="w-8 h-8 rounded-full bg-slate-900 text-white flex items-center justify-center font-bold text-xs shadow-xs">
            {user?.employee
              ? `${user.employee.firstName[0]}${user.employee.lastName[0]}`
              : user?.username?.substring(0, 2).toUpperCase() || 'U'}
          </div>
          <div className="hidden sm:block text-left">
            <div className="flex items-center space-x-1.5">
              <span className="text-xs font-semibold text-gray-900 leading-tight">
                {user?.employee
                  ? `${user.employee.firstName} ${user.employee.lastName}`
                  : user?.username || 'User'}
              </span>
              <span
                className={`text-[9px] font-bold px-1.5 py-0.2 rounded border ${getRoleBadgeColor(
                  role || undefined
                )}`}
              >
                {role}
              </span>
            </div>
            <p className="text-[10px] text-gray-500 leading-tight truncate max-w-[160px]">
              {user?.employee?.designation || user?.employee?.department || 'System User'}
            </p>
          </div>
        </div>

        {/* Logout Button */}
        <button
          onClick={logout}
          className="p-1.5 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition cursor-pointer"
          title="Sign Out"
        >
          <LogOut className="w-4 h-4" />
        </button>
      </div>
    </header>
  );
};
