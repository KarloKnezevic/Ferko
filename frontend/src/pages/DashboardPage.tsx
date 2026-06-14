import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { api } from '../api/client';
import { useAuth } from '../auth/AuthContext';

export function DashboardPage() {
  const { user, hasRole } = useAuth();
  const isStudent = hasRole('STUDENT');
  const semester = useQuery({ queryKey: ['active-semester'], queryFn: api.activeSemester });
  const courses = useQuery({ queryKey: ['courses'], queryFn: api.courses });
  const rooms = useQuery({ queryKey: ['rooms'], queryFn: api.rooms });
  const calendar = useQuery({ queryKey: ['calendar'], queryFn: api.calendar });
  const notices = useQuery({ queryKey: ['notices'], queryFn: () => api.notices(5) });

  const upcomingExams = (calendar.data?.exams ?? []).slice(0, 4);
  const weekly = calendar.data?.weekly ?? [];
  const topCourses = (courses.data ?? []).slice(0, 6);

  return (
    <div>
      <h1>Dobrodošli, {user?.fullName}</h1>
      <p className="muted">
        Aktivni semestar:{' '}
        {semester.data ? `${semester.data.academicYear} — ${semester.data.term}` : '—'}
      </p>

      <div className="card-grid" style={{ marginTop: 16 }}>
        <div className="stat">
          <div className="value">{courses.data?.length ?? '—'}</div>
          <div className="label">Kolegiji u semestru</div>
        </div>
        <div className="stat">
          <div className="value">{weekly.length || '—'}</div>
          <div className="label">{isStudent ? 'Termini nastave (tjedno)' : 'Moji termini (tjedno)'}</div>
        </div>
        <div className="stat">
          <div className="value">{upcomingExams.length || '—'}</div>
          <div className="label">Nadolazeće provjere</div>
        </div>
        <div className="stat">
          <div className="value">{rooms.data?.length ?? '—'}</div>
          <div className="label">Prostorije</div>
        </div>
      </div>

      <div className="form-row" style={{ marginTop: 4 }}>
        <div className="card" style={{ flex: 1 }}>
          <h2>Nadolazeće provjere</h2>
          {upcomingExams.length === 0 && <p className="muted">Nema datiranih provjera.</p>}
          {upcomingExams.map((e, i) => (
            <div key={i} style={{ marginBottom: 8 }}>
              <strong>{e.shortName}</strong> — {e.title}
              <div className="muted" style={{ fontSize: 12 }}>
                {e.courseCode} · {new Date(e.startsAt).toLocaleString('hr-HR')}
              </div>
            </div>
          ))}
          <Link to="/kalendar">Otvori kalendar →</Link>
        </div>

        <div className="card" style={{ flex: 1 }}>
          <h2>Najnovije obavijesti</h2>
          {notices.data?.length === 0 && <p className="muted">Nema obavijesti.</p>}
          {notices.data?.slice(0, 4).map((n) => (
            <div key={n.id} style={{ marginBottom: 8 }}>
              {n.pinned && (
                <span className="pill warn" style={{ marginRight: 6 }}>
                  📌
                </span>
              )}
              <strong>{n.title}</strong>
              <div className="muted" style={{ fontSize: 12 }}>
                {new Date(n.createdAt).toLocaleDateString('hr-HR')}
              </div>
            </div>
          ))}
          <Link to="/obavijesti">Sve obavijesti →</Link>
        </div>
      </div>

      <div className="card">
        <h2>Brzi pristup kolegijima</h2>
        {topCourses.length === 0 && <p className="muted">Nema kolegija.</p>}
        <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap' }}>
          {topCourses.map((c) => (
            <Link key={c.id} className="btn ghost" to={`/kolegiji/${c.id}`}>
              {c.code} — {c.name}
            </Link>
          ))}
        </div>
      </div>
    </div>
  );
}
