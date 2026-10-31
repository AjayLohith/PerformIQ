import React, { createContext, useContext, useState, useEffect } from 'react';
import { AppUser, UserRole } from '../types';
import { authApi } from '../services/api';

export interface DemoPersona {
  key: string;
  name: string;
  role: UserRole;
  username: string;
  designation: string;
  department: string;
  avatar: string;
  description: string;
}

export const DEMO_PERSONAS: DemoPersona[] = [
  {
    key: 'admin',
    name: 'Platform Admin',
    role: 'ADMIN',
    username: 'admin',
    designation: 'Operations & HR Admin',
    department: 'People Operations',
    avatar: 'PA',
    description: 'System-wide control, dataset ingestion, anomaly audit, review cycle transitions',
  },
  {
    key: 'sarah.chen',
    name: 'Sarah Chen',
    role: 'MANAGER',
    username: 'sarah.chen',
    designation: 'VP of Engineering',
    department: 'Engineering',
    avatar: 'SC',
    description: 'Team performance scoring studio, 360 review evaluations, dev plan approvals',
  },
  {
    key: 'elena.rostova',
    name: 'Elena Rostova',
    role: 'EMPLOYEE',
    username: 'elena.rostova',
    designation: 'Staff Software Engineer',
    department: 'Engineering',
    avatar: 'ER',
    description: 'Personal OKR goals, Key Result sliders, Check-in evidence, 360 feedback hub',
  },
  {
    key: 'marcus.vance',
    name: 'Marcus Vance',
    role: 'MANAGER',
    username: 'marcus.vance',
    designation: 'Director of Engineering',
    department: 'Engineering',
    avatar: 'MV',
    description: 'Engineering leadership direct report overview and score approvals',
  },
  {
    key: 'alex.rivera',
    name: 'Alex Rivera',
    role: 'EMPLOYEE',
    username: 'alex.rivera',
    designation: 'Senior Staff Engineer',
    department: 'Engineering',
    avatar: 'AR',
    description: 'Core infrastructure initiatives, personal growth plan tracking',
  },
];

interface AuthContextType {
  user: AppUser | null;
  token: string | null;
  role: UserRole | null;
  isLoading: boolean;
  login: (username: string, password?: string) => Promise<void>;
  switchPersona: (personaKey: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<AppUser | null>(null);
  const [token, setToken] = useState<string | null>(localStorage.getItem('performiq_token'));
  const [isLoading, setIsLoading] = useState<boolean>(true);

  useEffect(() => {
    const initializeAuth = async () => {
      const storedToken = localStorage.getItem('performiq_token');
      if (storedToken) {
        try {
          const profile = await authApi.getMe();
          setUser(profile);
          setToken(storedToken);
        } catch {
          localStorage.removeItem('performiq_token');
          setUser(null);
          setToken(null);
        }
      }
      setIsLoading(false);
    };

    initializeAuth();
  }, []);

  const login = async (username: string, password = 'password123') => {
    setIsLoading(true);
    try {
      const res = await authApi.login(username, password);
      localStorage.setItem('performiq_token', res.token);
      setToken(res.token);
      const profile = await authApi.getMe();
      setUser(profile);
    } finally {
      setIsLoading(false);
    }
  };

  const switchPersona = async (personaKey: string) => {
    const persona = DEMO_PERSONAS.find((p) => p.key === personaKey);
    if (persona) {
      await login(persona.username, 'password123');
    }
  };

  const logout = () => {
    localStorage.removeItem('performiq_token');
    setUser(null);
    setToken(null);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        role: user?.role || null,
        isLoading,
        login,
        switchPersona,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
