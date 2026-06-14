import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { api } from '../api/client';
import { useAuth } from '../auth/AuthContext';

export function DashboardPage() {
  const { user, hasRole } = useAuth();
  const isStudent = hasRole('STUDENT');
  const isInvigilator = hasRole('ASISTENT', 'ASISTENT_ORGANIZATOR', 'NASTAVNIK', 'NOSITELJ');
  const semester = useQuery({ queryKey: ['active-semester'], queryFn: api.activeSemester });
  const courses = useQuery({ queryKey: ['courses'], queryFn: api.courses });
  const rooms = useQuery({ queryKey: ['rooms'], queryFn: api.rooms });
  const calendar = useQuery({ queryKey: ['calendar'], queryFn: api.calendar });
  const notices = useQuery({ queryKey: ['notices'], queryFn: () => api.notices(5) });
  const duties = useQuery({
    queryKey: ['my-duties'],
    queryFn: api.myDuties,
    enabled: isInvigilator,
  });
  const demonstratures = useQuery({
    queryKey: ['my-demonstratures'],
    queryFn: api.myDemonstratures,
    enabled: isStudent,
  });

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

      {isStudent && (
        <div className="card">
          <h2>Moj studij</h2>
          <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap' }}>
            <Link className="btn ghost" to="/moje-provjere">
              Moje provjere
            </Link>
            <Link className="btn ghost" to="/moji-bodovi">
              Moji bodovi
            </Link>
            <Link className="btn ghost" to="/profil">
              Moj profil
            </Link>
          </div>
        </div>
      )}

      {isStudent && (demonstratures.data?.length ?? 0) > 0 && (
        <div className="card">
          <h2>Moje demonstrature</h2>
          <p className="muted">Kolegiji na kojima ste demonstrator:</p>
          <ul style={{ listStyle: 'none', margin: 0, padding: 0, display: 'flex', flexWrap: 'wrap', gap: 8 }}>
            {demonstratures.data?.map((d) => (
              <li key={d.courseId} className="role-badge">
                <Link to={`/kolegiji/${d.courseId}`}>
                  {d.courseCode} {d.courseName}
                </Link>
              </li>
            ))}
          </ul>
        </div>
      )}

      {isInvigilator && (
        <div className="card">
          <h2>Moja dežurstva</h2>
          {(duties.data?.length ?? 0) === 0 ? (
            <p className="muted">Trenutno niste raspoređeni ni na jedno dežurstvo.</p>
          ) : (
            <>
              <p>
                Raspoređeni ste na <strong>{duties.data?.length}</strong> dežurstvo/a.
              </p>
              {duties.data?.slice(0, 3).map((d) => (
                <div key={`${d.examId}-${d.roomCode}`} style={{ marginBottom: 8 }}>
                  <strong>{d.examShortName}</strong> — {d.courseCode}
                  <div className="muted" style={{ fontSize: 12 }}>
                    {d.roomCode || '—'}
                    {d.startsAt ? ` · ${new Date(d.startsAt).toLocaleString('hr-HR')}` : ''}
                  </div>
                </div>
              ))}
            </>
          )}
          <Link to="/moja-dezurstva">Sva dežurstva →</Link>
        </div>
      )}

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
