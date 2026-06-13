import { Navigate, Route, Routes } from 'react-router-dom';
import type { ReactNode } from 'react';
import { useAuth } from './auth/AuthContext';
import { Layout } from './components/Layout';
import { LoginPage } from './pages/LoginPage';
import { DashboardPage } from './pages/DashboardPage';
import { CoursesPage } from './pages/CoursesPage';
import { CourseDetailPage } from './pages/CourseDetailPage';
import { ExamSchedulingPage } from './pages/ExamSchedulingPage';
import { RoomsPage } from './pages/RoomsPage';
import { StudentsPage } from './pages/StudentsPage';

function RequireAuth({ children }: { children: ReactNode }) {
  const { user, loading } = useAuth();
  if (loading) return <div className="page-loading">Učitavanje…</div>;
  if (!user) return <Navigate to="/login" replace />;
  return <>{children}</>;
}

export function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route
        element={
          <RequireAuth>
            <Layout />
          </RequireAuth>
        }
      >
        <Route path="/" element={<DashboardPage />} />
        <Route path="/kolegiji" element={<CoursesPage />} />
        <Route path="/kolegiji/:id" element={<CourseDetailPage />} />
        <Route path="/kolegiji/:id/ispiti" element={<ExamSchedulingPage />} />
        <Route path="/prostorije" element={<RoomsPage />} />
        <Route path="/studenti" element={<StudentsPage />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  );
}
