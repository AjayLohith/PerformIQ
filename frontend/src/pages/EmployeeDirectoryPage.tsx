import React, { useState, useEffect } from 'react';
import { employeeApi } from '../services/api';
import { Employee } from '../types';
import {
  Contact2,
  Search,
  Filter,
  Grid,
  List,
  Mail,
  Calendar,
  Building,
  UserCheck,
  Shield,
  ChevronRight,
  RefreshCw,
  Users,
  Briefcase,
  X,
  Target,
  MessageSquare,
} from 'lucide-react';

export const EmployeeDirectoryPage: React.FC = () => {
  const [employees, setEmployees] = useState<Employee[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedDept, setSelectedDept] = useState('ALL');
  const [viewMode, setViewMode] = useState<'grid' | 'table'>('grid');
  const [inspectingEmployee, setInspectingEmployee] = useState<Employee | null>(null);

  const departments = [
    'ALL',
    'Engineering',
    'Product Management',
    'Quality Assurance',
    'Human Resources',
    'Sales',
    'Customer Success',
    'Executive Leadership',
  ];

  useEffect(() => {
    loadEmployees(currentPage, selectedDept);
  }, [currentPage, selectedDept]);

  const loadEmployees = async (page: number, dept: string) => {
    setIsLoading(true);
    try {
      const deptFilter = dept === 'ALL' ? undefined : dept;
      const res = await employeeApi.getEmployees(deptFilter, page, 24);
      setEmployees(res.content || []);
      setTotalPages(res.totalPages || 1);
      setTotalElements(res.totalElements || 0);
    } catch {
      setEmployees([]);
    } finally {
      setIsLoading(false);
    }
  };

  const filteredEmployees = employees.filter((emp) => {
    if (!searchQuery.trim()) return true;
    const q = searchQuery.toLowerCase();
    const fullName = `${emp.firstName} ${emp.lastName}`.toLowerCase();
    return (
      fullName.includes(q) ||
      emp.email.toLowerCase().includes(q) ||
      emp.businessId.toLowerCase().includes(q) ||
      emp.designation.toLowerCase().includes(q) ||
      emp.department.toLowerCase().includes(q)
    );
  });

  const getDepartmentColor = (dept: string) => {
    switch (dept) {
      case 'Engineering':
        return 'bg-blue-50 text-blue-700 border-blue-200';
      case 'Product Management':
        return 'bg-indigo-50 text-indigo-700 border-indigo-200';
      case 'Quality Assurance':
        return 'bg-amber-50 text-amber-700 border-amber-200';
      case 'Human Resources':
        return 'bg-emerald-50 text-emerald-700 border-emerald-200';
      case 'Executive Leadership':
        return 'bg-purple-50 text-purple-700 border-purple-200';
      default:
        return 'bg-gray-100 text-gray-700 border-gray-200';
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'ACTIVE':
        return 'bg-emerald-50 text-emerald-700 border-emerald-200';
      case 'PROBATION':
        return 'bg-amber-50 text-amber-700 border-amber-200';
      case 'ON_LEAVE':
        return 'bg-blue-50 text-blue-700 border-blue-200';
      default:
        return 'bg-rose-50 text-rose-700 border-rose-200';
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center space-x-2">
            <h1 className="text-xl font-bold text-gray-900 tracking-tight">Organization Directory</h1>
            <span className="px-2 py-0.5 rounded-full text-xs font-semibold bg-indigo-50 text-indigo-700 border border-indigo-200 flex items-center space-x-1">
              <Users className="w-3 h-3 mr-1" />
              Verified Hierarchy
            </span>
          </div>
          <p className="text-xs text-gray-500 mt-1">
            Search talent profiles, cross-functional organizational reporting lines, and department rosters.
          </p>
        </div>
        <div className="flex items-center space-x-3">
          {/* View Mode Toggle */}
          <div className="flex items-center bg-gray-100 p-0.5 rounded-lg border border-gray-200">
            <button
              onClick={() => setViewMode('grid')}
              className={`p-1.5 rounded-md text-xs font-medium transition cursor-pointer ${
                viewMode === 'grid'
                  ? 'bg-white text-gray-900 shadow-xs'
                  : 'text-gray-500 hover:text-gray-700'
              }`}
              title="Grid View"
            >
              <Grid className="w-3.5 h-3.5" />
            </button>
            <button
              onClick={() => setViewMode('table')}
              className={`p-1.5 rounded-md text-xs font-medium transition cursor-pointer ${
                viewMode === 'table'
                  ? 'bg-white text-gray-900 shadow-xs'
                  : 'text-gray-500 hover:text-gray-700'
              }`}
              title="Table View"
            >
              <List className="w-3.5 h-3.5" />
            </button>
          </div>

          <button
            onClick={() => loadEmployees(currentPage, selectedDept)}
            disabled={isLoading}
            className="flex items-center space-x-1.5 px-3 py-1.5 bg-white border border-gray-300 hover:bg-gray-50 rounded-lg text-xs font-semibold text-gray-700 transition shadow-xs cursor-pointer"
          >
            <RefreshCw className={`w-3.5 h-3.5 text-gray-500 ${isLoading ? 'animate-spin' : ''}`} />
            <span>Refresh</span>
          </button>
        </div>
      </div>

      {/* KPI Stats */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-xs">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-gray-500">Total Workforce</span>
            <Users className="w-4 h-4 text-indigo-600" />
          </div>
          <div className="text-2xl font-bold text-gray-900 mt-2">{totalElements}</div>
          <span className="text-[11px] text-gray-400 mt-1 block">Active headcount in DB</span>
        </div>

        <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-xs">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-gray-500">Operational Departments</span>
            <Building className="w-4 h-4 text-blue-600" />
          </div>
          <div className="text-2xl font-bold text-gray-900 mt-2">7 Units</div>
          <span className="text-[11px] text-gray-400 mt-1 block">Cross-functional coverage</span>
        </div>

        <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-xs">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-gray-500">Active Status Rate</span>
            <UserCheck className="w-4 h-4 text-emerald-600" />
          </div>
          <div className="text-2xl font-bold text-emerald-600 mt-2">98.4%</div>
          <span className="text-[11px] text-gray-400 mt-1 block">Full engagement in cycles</span>
        </div>

        <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-xs">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-gray-500">Displaying Records</span>
            <Briefcase className="w-4 h-4 text-amber-600" />
          </div>
          <div className="text-2xl font-bold text-gray-900 mt-2">
            {filteredEmployees.length} <span className="text-xs font-normal text-gray-400">people</span>
          </div>
          <span className="text-[11px] text-gray-400 mt-1 block">
            Page {currentPage + 1} of {totalPages || 1}
          </span>
        </div>
      </div>

      {/* Filter and Search Bar */}
      <div className="bg-white p-3.5 rounded-xl border border-gray-200 shadow-xs space-y-3">
        <div className="flex flex-col md:flex-row gap-3 items-center justify-between">
          <div className="relative w-full md:w-96">
            <Search className="w-4 h-4 text-gray-400 absolute left-3 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              placeholder="Search by name, email, designation, or ID..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-9 pr-3 py-1.5 text-xs bg-gray-50 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-slate-900 focus:bg-white"
            />
          </div>

          <div className="text-xs text-gray-400">
            Showing <span className="font-semibold text-gray-700">{filteredEmployees.length}</span> matching talent
          </div>
        </div>

        {/* Department Filter Chips */}
        <div className="flex items-center space-x-1.5 overflow-x-auto pb-1 scrollbar-thin">
          <span className="text-[11px] font-semibold text-gray-400 uppercase tracking-wider shrink-0 mr-1 flex items-center">
            <Filter className="w-3 h-3 mr-1" />
            Department:
          </span>
          {departments.map((dept) => (
            <button
              key={dept}
              onClick={() => {
                setSelectedDept(dept);
                setCurrentPage(0);
              }}
              className={`px-2.5 py-1 rounded-md text-[11px] font-medium transition shrink-0 cursor-pointer ${
                selectedDept === dept
                  ? 'bg-slate-900 text-white'
                  : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
              }`}
            >
              {dept}
            </button>
          ))}
        </div>
      </div>

      {/* Content: Grid or Table */}
      {isLoading ? (
        <div className="bg-white rounded-xl border border-gray-200 p-16 text-center shadow-xs">
          <RefreshCw className="w-8 h-8 text-gray-400 animate-spin mx-auto mb-3" />
          <p className="text-xs text-gray-500 font-medium">Retrieving organization directory...</p>
        </div>
      ) : filteredEmployees.length === 0 ? (
        <div className="bg-white rounded-xl border border-gray-200 p-16 text-center shadow-xs">
          <Contact2 className="w-10 h-10 text-gray-300 mx-auto mb-3" />
          <h3 className="text-sm font-semibold text-gray-800">No employees found</h3>
          <p className="text-xs text-gray-500 mt-1 max-w-sm mx-auto">
            Try adjusting your search query or department filter to see more profiles.
          </p>
        </div>
      ) : viewMode === 'grid' ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
          {filteredEmployees.map((emp) => (
            <div
              key={emp.id}
              onClick={() => setInspectingEmployee(emp)}
              className="bg-white rounded-xl border border-gray-200 p-4 shadow-xs hover:shadow-md hover:border-gray-300 transition cursor-pointer flex flex-col justify-between group"
            >
              <div>
                {/* Top card info */}
                <div className="flex items-start justify-between mb-3">
                  <div className="w-11 h-11 rounded-full bg-slate-900 text-white flex items-center justify-center font-bold text-sm tracking-wider shadow-xs group-hover:bg-indigo-600 transition">
                    {emp.firstName[0]}
                    {emp.lastName[0]}
                  </div>
                  <span
                    className={`px-2 py-0.5 rounded text-[10px] font-bold border ${getStatusBadge(
                      emp.status
                    )}`}
                  >
                    {emp.status}
                  </span>
                </div>

                <h3 className="text-sm font-bold text-gray-900 group-hover:text-indigo-600 transition truncate">
                  {emp.firstName} {emp.lastName}
                </h3>
                <p className="text-xs text-gray-500 font-medium truncate mt-0.5">{emp.designation}</p>

                <div className="mt-3 flex items-center space-x-1.5">
                  <span
                    className={`px-2 py-0.5 rounded text-[10px] font-semibold border ${getDepartmentColor(
                      emp.department
                    )}`}
                  >
                    {emp.department}
                  </span>
                  <span className="text-[10px] font-mono text-gray-400 bg-gray-50 px-1.5 py-0.5 rounded border border-gray-200">
                    {emp.businessId}
                  </span>
                </div>

                <div className="mt-4 pt-3 border-t border-gray-100 space-y-1.5 text-xs text-gray-500">
                  <div className="flex items-center space-x-2 truncate">
                    <Mail className="w-3.5 h-3.5 text-gray-400 shrink-0" />
                    <span className="truncate">{emp.email}</span>
                  </div>
                  {emp.currentManager && (
                    <div className="flex items-center space-x-2 truncate text-[11px] text-gray-600">
                      <Shield className="w-3.5 h-3.5 text-indigo-500 shrink-0" />
                      <span className="truncate">
                        Reports to: <strong className="text-gray-800">{emp.currentManager.name}</strong>
                      </span>
                    </div>
                  )}
                </div>
              </div>

              <div className="mt-4 pt-3 border-t border-gray-100 flex items-center justify-between text-[11px] text-indigo-600 font-semibold group-hover:translate-x-0.5 transition">
                <span>View Full Profile</span>
                <ChevronRight className="w-3.5 h-3.5" />
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div className="bg-white rounded-xl border border-gray-200 shadow-xs overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-gray-50/75 border-b border-gray-200 text-gray-500 uppercase tracking-wider text-[10px] font-semibold">
                <tr>
                  <th className="py-3 px-4">Employee</th>
                  <th className="py-3 px-4">ID</th>
                  <th className="py-3 px-4">Department</th>
                  <th className="py-3 px-4">Designation</th>
                  <th className="py-3 px-4">Reports To</th>
                  <th className="py-3 px-4">Joined</th>
                  <th className="py-3 px-4">Status</th>
                  <th className="py-3 px-4 text-right">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {filteredEmployees.map((emp) => (
                  <tr
                    key={emp.id}
                    onClick={() => setInspectingEmployee(emp)}
                    className="hover:bg-gray-50/60 transition cursor-pointer"
                  >
                    <td className="py-3 px-4 whitespace-nowrap">
                      <div className="flex items-center space-x-2.5">
                        <div className="w-7 h-7 rounded-full bg-slate-800 text-white flex items-center justify-center font-bold text-xs">
                          {emp.firstName[0]}
                          {emp.lastName[0]}
                        </div>
                        <div>
                          <div className="font-semibold text-gray-900">
                            {emp.firstName} {emp.lastName}
                          </div>
                          <div className="text-[11px] text-gray-400">{emp.email}</div>
                        </div>
                      </div>
                    </td>
                    <td className="py-3 px-4 font-mono text-gray-500 text-[11px]">{emp.businessId}</td>
                    <td className="py-3 px-4 whitespace-nowrap">
                      <span
                        className={`inline-block px-2 py-0.5 rounded text-[10px] font-semibold border ${getDepartmentColor(
                          emp.department
                        )}`}
                      >
                        {emp.department}
                      </span>
                    </td>
                    <td className="py-3 px-4 font-medium text-gray-700 whitespace-nowrap">
                      {emp.designation}
                    </td>
                    <td className="py-3 px-4 whitespace-nowrap text-gray-600">
                      {emp.currentManager ? (
                        <span className="font-medium text-gray-800">{emp.currentManager.name}</span>
                      ) : (
                        <span className="text-gray-400 italic">Executive Level</span>
                      )}
                    </td>
                    <td className="py-3 px-4 text-gray-500 whitespace-nowrap">
                      {emp.joiningDate ? new Date(emp.joiningDate).toLocaleDateString() : '—'}
                    </td>
                    <td className="py-3 px-4 whitespace-nowrap">
                      <span
                        className={`inline-block px-2 py-0.5 rounded text-[10px] font-bold border ${getStatusBadge(
                          emp.status
                        )}`}
                      >
                        {emp.status}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-right whitespace-nowrap">
                      <button className="text-indigo-600 hover:text-indigo-800 font-semibold text-[11px] inline-flex items-center space-x-1 cursor-pointer">
                        <span>Profile</span>
                        <ChevronRight className="w-3 h-3" />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Pagination Footer */}
      <div className="p-4 bg-white rounded-xl border border-gray-200 shadow-xs flex items-center justify-between">
        <span className="text-xs text-gray-500">
          Showing <span className="font-semibold">{filteredEmployees.length}</span> of{' '}
          <span className="font-semibold">{totalElements}</span> organization members
        </span>
        <div className="flex items-center space-x-2">
          <button
            onClick={() => setCurrentPage((p) => Math.max(0, p - 1))}
            disabled={currentPage === 0 || isLoading}
            className="px-3 py-1.5 bg-white border border-gray-200 rounded text-xs font-medium text-gray-700 disabled:opacity-50 hover:bg-gray-50 transition cursor-pointer"
          >
            Previous
          </button>
          <span className="text-xs font-semibold text-gray-700">
            Page {currentPage + 1} / {totalPages || 1}
          </span>
          <button
            onClick={() => setCurrentPage((p) => Math.min(totalPages - 1, p + 1))}
            disabled={currentPage >= totalPages - 1 || isLoading}
            className="px-3 py-1.5 bg-white border border-gray-200 rounded text-xs font-medium text-gray-700 disabled:opacity-50 hover:bg-gray-50 transition cursor-pointer"
          >
            Next
          </button>
        </div>
      </div>

      {/* Employee Quick View Drawer / Modal */}
      {inspectingEmployee && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs">
          <div className="bg-white rounded-2xl max-w-lg w-full shadow-2xl border border-gray-200 animate-in fade-in zoom-in-95 duration-150 overflow-hidden">
            {/* Header Banner */}
            <div className="bg-gradient-to-r from-slate-900 to-indigo-900 p-6 text-white relative">
              <button
                onClick={() => setInspectingEmployee(null)}
                className="absolute top-4 right-4 p-1.5 text-white/80 hover:text-white rounded-lg hover:bg-white/10 transition cursor-pointer"
              >
                <X className="w-5 h-5" />
              </button>
              <div className="flex items-center space-x-4">
                <div className="w-16 h-16 rounded-full bg-white text-slate-900 flex items-center justify-center font-bold text-xl shadow-lg">
                  {inspectingEmployee.firstName[0]}
                  {inspectingEmployee.lastName[0]}
                </div>
                <div>
                  <h3 className="text-lg font-bold">
                    {inspectingEmployee.firstName} {inspectingEmployee.lastName}
                  </h3>
                  <p className="text-indigo-200 text-xs font-medium">{inspectingEmployee.designation}</p>
                  <div className="flex items-center space-x-2 mt-2">
                    <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-white/20 text-white">
                      {inspectingEmployee.department}
                    </span>
                    <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-emerald-500/20 text-emerald-300 border border-emerald-400/30">
                      {inspectingEmployee.status}
                    </span>
                  </div>
                </div>
              </div>
            </div>

            {/* Profile Info Details */}
            <div className="p-6 space-y-4 text-xs">
              <div className="grid grid-cols-2 gap-4 pb-4 border-b border-gray-100">
                <div>
                  <span className="text-[10px] font-semibold text-gray-400 uppercase tracking-wider">
                    Employee ID
                  </span>
                  <p className="font-mono font-semibold text-gray-800 mt-0.5">
                    {inspectingEmployee.businessId}
                  </p>
                </div>
                <div>
                  <span className="text-[10px] font-semibold text-gray-400 uppercase tracking-wider">
                    Official Email
                  </span>
                  <p className="font-medium text-gray-800 mt-0.5 truncate">{inspectingEmployee.email}</p>
                </div>
                <div>
                  <span className="text-[10px] font-semibold text-gray-400 uppercase tracking-wider">
                    Joining Date
                  </span>
                  <p className="font-medium text-gray-800 mt-0.5">
                    {inspectingEmployee.joiningDate
                      ? new Date(inspectingEmployee.joiningDate).toLocaleDateString()
                      : '—'}
                  </p>
                </div>
                <div>
                  <span className="text-[10px] font-semibold text-gray-400 uppercase tracking-wider">
                    Database ID
                  </span>
                  <p className="font-mono text-gray-400 text-[10px] mt-0.5 truncate">
                    {inspectingEmployee.id}
                  </p>
                </div>
              </div>

              {/* Manager Reporting Relationship */}
              <div>
                <span className="text-[10px] font-semibold text-gray-400 uppercase tracking-wider block mb-2">
                  Manager Reporting Hierarchy
                </span>
                {inspectingEmployee.currentManager ? (
                  <div className="bg-gray-50 border border-gray-200 rounded-xl p-3 flex items-center justify-between">
                    <div className="flex items-center space-x-3">
                      <div className="w-8 h-8 rounded-full bg-slate-900 text-white flex items-center justify-center font-bold text-xs">
                        {inspectingEmployee.currentManager.name[0]}
                      </div>
                      <div>
                        <p className="font-bold text-gray-900 text-xs">
                          {inspectingEmployee.currentManager.name}
                        </p>
                        <p className="text-[10px] text-gray-500">
                          {inspectingEmployee.currentManager.designation}
                        </p>
                      </div>
                    </div>
                    <span className="font-mono text-[10px] text-gray-400 bg-white px-2 py-0.5 rounded border border-gray-200">
                      {inspectingEmployee.currentManager.managerBusinessId}
                    </span>
                  </div>
                ) : (
                  <div className="bg-gray-50 border border-gray-200 rounded-xl p-3 text-center text-gray-500 text-xs italic">
                    Executive / Department Head (No direct manager recorded)
                  </div>
                )}
              </div>

              {/* Quick Jump Action Bar */}
              <div className="pt-2 flex items-center gap-2">
                <a
                  href={`/goals`}
                  className="flex-1 flex items-center justify-center space-x-1.5 py-2 px-3 bg-gray-100 hover:bg-gray-200 text-gray-800 rounded-lg font-medium text-xs transition"
                >
                  <Target className="w-3.5 h-3.5 text-indigo-600" />
                  <span>Goals & OKRs</span>
                </a>
                <a
                  href={`/feedback`}
                  className="flex-1 flex items-center justify-center space-x-1.5 py-2 px-3 bg-gray-100 hover:bg-gray-200 text-gray-800 rounded-lg font-medium text-xs transition"
                >
                  <MessageSquare className="w-3.5 h-3.5 text-blue-600" />
                  <span>360 Feedback</span>
                </a>
              </div>
            </div>

            {/* Footer */}
            <div className="p-4 border-t border-gray-100 bg-gray-50 flex justify-end">
              <button
                onClick={() => setInspectingEmployee(null)}
                className="px-4 py-2 bg-slate-900 hover:bg-slate-800 text-white rounded-lg text-xs font-semibold transition cursor-pointer"
              >
                Close Profile
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
