import { Navigate, Route, Routes } from 'react-router-dom';
import type { ReactNode } from 'react';
import { useAuth } from './auth/AuthContext';
import { Layout } from './components/Layout';
import { LoginPage } from './pages/LoginPage';
import { DashboardPage } from './pages/DashboardPage';
import { CoursesPage } from './pages/CoursesPage';
import { CourseDetailPage } from './pages/CourseDetailPage';
import { ExamSchedulingPage } from './pages/ExamSchedulingPage';
import { GradingPage } from './pages/GradingPage';
import { SurveysPage } from './pages/SurveysPage';
import { ForumPage } from './pages/ForumPage';
import { RepositoryPage } from './pages/RepositoryPage';
import { GroupExchangePage } from './pages/GroupExchangePage';
import { RoomsPage } from './pages/RoomsPage';
import { StudentsPage } from './pages/StudentsPage';
import { NoticesPage } from './pages/NoticesPage';
import { CalendarPage } from './pages/CalendarPage';
import { StudentExamsPage } from './pages/StudentExamsPage';
import { AdminPage } from './pages/AdminPage';

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
        <Route path="/kolegiji/:id/bodovi" element={<GradingPage />} />
        <Route path="/kolegiji/:id/ankete" element={<SurveysPage />} />
        <Route path="/kolegiji/:id/forum" element={<ForumPage />} />
        <Route path="/kolegiji/:id/repozitorij" element={<RepositoryPage />} />
        <Route path="/kolegiji/:id/burza" element={<GroupExchangePage />} />
        <Route path="/moje-provjere" element={<StudentExamsPage />} />
        <Route path="/kalendar" element={<CalendarPage />} />
        <Route path="/obavijesti" element={<NoticesPage />} />
        <Route path="/prostorije" element={<RoomsPage />} />
        <Route path="/studenti" element={<StudentsPage />} />
        <Route path="/admin" element={<AdminPage />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  );
}
