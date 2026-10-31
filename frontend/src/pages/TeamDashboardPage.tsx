import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { employeeApi } from '../services/api';
import { Employee } from '../types';
import { Users, Mail, ArrowUpRight, Award, Shield } from 'lucide-react';

interface TeamProps {
  onNavigate: (path: string) => void;
}

export const TeamDashboardPage: React.FC<TeamProps> = ({ onNavigate }) => {
  const { user } = useAuth();
  const [reports, setReports] = useState<Employee[]>([]);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    loadReports();
  }, [user]);

  const loadReports = async () => {
    if (!user?.employee?.id) return;
    setIsLoading(true);
    try {
      const list = await employeeApi.getDirectReports(user.employee.id);
      setReports(list);
    } catch {
      setReports([]);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-gray-900">Engineering Team Overview</h1>
          <p className="text-xs text-gray-500 mt-1">
            Direct reports hierarchy, active review cycle status, and coaching pathways.
          </p>
        </div>

        <div className="flex items-center space-x-2">
          <button
            onClick={() => onNavigate('/reviews')}
            className="px-3.5 py-2 bg-slate-900 hover:bg-slate-800 text-white rounded-lg text-xs font-semibold shadow-xs flex items-center space-x-1.5 transition cursor-pointer"
          >
            <Award className="w-4 h-4" />
            <span>Open Review Studio</span>
          </button>
        </div>
      </div>

      {/* Reports Grid */}
      {isLoading ? (
        <div className="text-center py-12 text-xs text-gray-400">Loading team hierarchy...</div>
      ) : reports.length === 0 ? (
        <div className="bg-white p-12 rounded-2xl border border-gray-200 text-center text-xs text-gray-400">
          No direct reports assigned to this manager account in the active reporting history.
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {reports.map((emp) => (
            <div
              key={emp.id}
              className="bg-white p-5 rounded-2xl border border-gray-200 shadow-xs space-y-4 hover:shadow-md transition"
            >
              <div className="flex items-start justify-between">
                <div className="flex items-center space-x-3">
                  <div className="w-10 h-10 rounded-full bg-slate-900 text-white flex items-center justify-center font-bold text-sm shadow-xs">
                    {emp.firstName[0]}{emp.lastName[0]}
                  </div>
                  <div>
                    <h3 className="text-sm font-bold text-gray-900">
                      {emp.firstName} {emp.lastName}
                    </h3>
                    <p className="text-xs text-gray-500">{emp.designation}</p>
                  </div>
                </div>

                <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-emerald-50 text-emerald-700 border border-emerald-200">
                  {emp.status}
                </span>
              </div>

              <div className="space-y-1 text-xs text-gray-600 pt-3 border-t border-gray-100">
                <div className="flex items-center justify-between text-[11px]">
                  <span className="text-gray-400">Employee ID:</span>
                  <span className="font-semibold text-gray-800">{emp.businessId}</span>
                </div>
                <div className="flex items-center justify-between text-[11px]">
                  <span className="text-gray-400">Department:</span>
                  <span className="font-semibold text-gray-800">{emp.department}</span>
                </div>
                <div className="flex items-center justify-between text-[11px]">
                  <span className="text-gray-400">Joined:</span>
                  <span className="font-semibold text-gray-800">{emp.joiningDate}</span>
                </div>
              </div>

              <div className="pt-2 flex items-center space-x-2">
                <button
                  onClick={() => onNavigate('/reviews')}
                  className="flex-1 py-1.5 px-3 bg-gray-50 hover:bg-gray-100 border border-gray-200 rounded-lg text-xs font-semibold text-gray-800 flex items-center justify-center space-x-1 cursor-pointer transition"
                >
                  <Award className="w-3.5 h-3.5 text-indigo-600" />
                  <span>Score Review</span>
                </button>
                <button
                  onClick={() => onNavigate('/plan-approvals')}
                  className="py-1.5 px-3 bg-gray-50 hover:bg-gray-100 border border-gray-200 rounded-lg text-xs font-semibold text-gray-800 flex items-center justify-center space-x-1 cursor-pointer transition"
                >
                  <ArrowUpRight className="w-3.5 h-3.5" />
                  <span>Plans</span>
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
