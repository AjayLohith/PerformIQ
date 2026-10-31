import React, { useState } from 'react';
import { useAuth, DEMO_PERSONAS } from '../context/AuthContext';
import { Sparkles, Shield, UserCheck, ArrowRight, Lock, User } from 'lucide-react';

export const LoginPage: React.FC = () => {
  const { login, isLoading } = useAuth();
  const [username, setUsername] = useState('admin');
  const [password, setPassword] = useState('password123');
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    try {
      await login(username, password);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Invalid username or password');
    }
  };

  const handleQuickLogin = async (demoUsername: string) => {
    setError(null);
    setUsername(demoUsername);
    setPassword('password123');
    try {
      await login(demoUsername, 'password123');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Quick login failed');
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md">
        <div className="flex justify-center">
          <div className="w-12 h-12 rounded-xl bg-slate-900 flex items-center justify-center text-white font-black text-xl shadow-md">
            PQ
          </div>
        </div>
        <h2 className="mt-4 text-center text-2xl font-bold tracking-tight text-gray-900">
          Perform<span className="text-indigo-600">IQ</span>
        </h2>
        <p className="mt-1 text-center text-xs text-gray-500">
          Enterprise Employee Performance & Growth Management System
        </p>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-xl">
        <div className="bg-white py-8 px-6 shadow-sm rounded-2xl border border-gray-200 sm:px-10">
          {/* Quick Demo Persona Switcher Banner */}
          <div className="mb-6 p-4 bg-indigo-50/70 border border-indigo-100 rounded-xl">
            <div className="flex items-center space-x-2 mb-2.5">
              <Sparkles className="w-4 h-4 text-indigo-600" />
              <span className="text-xs font-bold text-indigo-900 uppercase tracking-wider">
                Quick Demo Personas & Role Switcher
              </span>
            </div>
            <p className="text-xs text-indigo-700/90 mb-3">
              Select any profile below to sign in with pre-configured role permissions:
            </p>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
              {DEMO_PERSONAS.map((p) => (
                <button
                  key={p.key}
                  type="button"
                  onClick={() => handleQuickLogin(p.username)}
                  className="p-2.5 bg-white hover:bg-indigo-600 hover:text-white rounded-lg border border-indigo-200 text-left transition flex items-center space-x-2.5 group cursor-pointer shadow-2xs"
                >
                  <div className="w-7 h-7 rounded-full bg-slate-900 text-white group-hover:bg-white group-hover:text-indigo-600 flex items-center justify-center font-bold text-xs shrink-0">
                    {p.avatar}
                  </div>
                  <div className="min-w-0 flex-1">
                    <div className="flex items-center justify-between">
                      <p className="text-xs font-semibold truncate">{p.name}</p>
                      <span className="text-[9px] font-bold px-1.5 py-0.2 rounded bg-indigo-100 text-indigo-800 group-hover:bg-indigo-700 group-hover:text-white border border-indigo-200">
                        {p.role}
                      </span>
                    </div>
                    <p className="text-[10px] text-gray-500 group-hover:text-indigo-100 truncate">
                      {p.designation}
                    </p>
                  </div>
                </button>
              ))}
            </div>
          </div>

          <div className="relative mb-6">
            <div className="absolute inset-0 flex items-center">
              <div className="w-full border-t border-gray-200" />
            </div>
            <div className="relative flex justify-center text-xs">
              <span className="bg-white px-3 text-gray-400 font-medium">Or enter credentials</span>
            </div>
          </div>

          {error && (
            <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-xs text-red-700">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-xs font-medium text-gray-700 mb-1">Username</label>
              <div className="relative">
                <User className="w-4 h-4 text-gray-400 absolute left-3 top-2.5" />
                <input
                  type="text"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  required
                  className="w-full pl-9 pr-3 py-2 text-xs border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                  placeholder="e.g. admin or sarah.chen"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-medium text-gray-700 mb-1">Password</label>
              <div className="relative">
                <Lock className="w-4 h-4 text-gray-400 absolute left-3 top-2.5" />
                <input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                  className="w-full pl-9 pr-3 py-2 text-xs border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                  placeholder="••••••••"
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={isLoading}
              className="w-full py-2.5 px-4 bg-slate-900 hover:bg-slate-800 text-white rounded-lg text-xs font-semibold shadow-xs flex items-center justify-center space-x-1.5 transition cursor-pointer disabled:opacity-50"
            >
              <span>{isLoading ? 'Authenticating...' : 'Sign In to PerformIQ'}</span>
              <ArrowRight className="w-4 h-4" />
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};
