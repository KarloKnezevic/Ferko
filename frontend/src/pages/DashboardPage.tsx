import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { api } from '../api/client';
import { useAuth } from '../auth/AuthContext';
import type { WeeklySlot } from '../api/types';

const DAY_LABELS: Record<string, string> = {
  MONDAY: 'Ponedjeljak',
  TUESDAY: 'Utorak',
  WEDNESDAY: 'Srijeda',
  THURSDAY: 'Četvrtak',
  FRIDAY: 'Petak',
  SATURDAY: 'Subota',
  SUNDAY: 'Nedjelja',
};
const DAY_ORDER = Object.keys(DAY_LABELS);

function groupByDay(weekly: WeeklySlot[]): [string, WeeklySlot[]][] {
  const byDay = new Map<string, WeeklySlot[]>();
  for (const slot of weekly) {
    const list = byDay.get(slot.dayOfWeek) ?? [];
    list.push(slot);
    byDay.set(slot.dayOfWeek, list);
  }
  return DAY_ORDER.filter((d) => byDay.has(d)).map((d) => [
    d,
    byDay.get(d)!.slice().sort((a, b) => a.startsAt.localeCompare(b.startsAt)),
  ]);
}

export function DashboardPage() {
  const { user, hasRole } = useAuth();
  const isStudent = hasRole('STUDENT');
  const isStaff = hasRole('ADMIN', 'STUSLU', 'NOSITELJ', 'NASTAVNIK', 'ASISTENT_ORGANIZATOR', 'ASISTENT');
  const isInvigilator = hasRole('ASISTENT', 'ASISTENT_ORGANIZATOR', 'NASTAVNIK', 'NOSITELJ');

  const semester = useQuery({ queryKey: ['active-semester'], queryFn: api.activeSemester });
  const semesterCode = semester.data?.code;
  const courses = useQuery({
    queryKey: ['courses', semesterCode],
    queryFn: () => api.courses(semesterCode),
    enabled: !!semesterCode,
  });
  const rooms = useQuery({ queryKey: ['rooms'], queryFn: api.rooms, enabled: isStaff });
  const calendar = useQuery({ queryKey: ['calendar'], queryFn: api.calendar });
  const notices = useQuery({
    queryKey: ['notices'],
    queryFn: () => api.notices(5),
    enabled: isStaff,
  });
  const duties = useQuery({ queryKey: ['my-duties'], queryFn: api.myDuties, enabled: isInvigilator });
  const demonstratures = useQuery({
    queryKey: ['my-demonstratures'],
    queryFn: api.myDemonstratures,
    enabled: isStudent,
  });

  const upcomingExams = (calendar.data?.exams ?? []).slice(0, 4);
  const weekly = calendar.data?.weekly ?? [];
  const myCourses = courses.data ?? [];
  const quickCourses = myCourses.slice(0, 6);

  const header = (
    <>
      <h1>Dobrodošli, {user?.fullName}</h1>
      <p className="muted">
        Aktivni semestar:{' '}
        {semester.data ? `${semester.data.academicYear} — ${semester.data.term}` : '—'}
      </p>
    </>
  );

  const weekCard = (
    <div className="card">
      <h2>Ovaj tjedan</h2>
      {weekly.length === 0 && <p className="muted">Nema termina nastave u tjednu.</p>}
      {groupByDay(weekly).map(([day, slots]) => (
        <div key={day} style={{ marginBottom: 12 }}>
          <div style={{ fontWeight: 600, marginBottom: 4 }}>{DAY_LABELS[day] ?? day}</div>
          {slots.map((s, i) => (
            <div
              key={i}
              style={{ display: 'flex', gap: 10, alignItems: 'baseline', padding: '2px 0' }}
            >
              <span className="muted" style={{ minWidth: 92, fontVariantNumeric: 'tabular-nums' }}>
                {s.startsAt}–{s.endsAt}
              </span>
              <span>
                <strong>{s.courseCode}</strong> {s.courseName}
              </span>
              {s.room && <span className="pill">{s.room}</span>}
            </div>
          ))}
        </div>
      ))}
      <Link to="/kalendar">Otvori kalendar →</Link>
    </div>
  );

  const examsCard = (
    <div className="card" style={{ flex: 1 }}>
      <h2>Nadolazeće provjere</h2>
      {upcomingExams.length === 0 && <p className="muted">Nema datiranih provjera.</p>}
      {upcomingExams.map((e, i) => (
        <div key={i} style={{ marginBottom: 8 }}>
          <strong>{e.shortName}</strong> — {e.courseName}
          <div className="muted" style={{ fontSize: 12 }}>
            {e.courseCode} · {e.startsAt ? new Date(e.startsAt).toLocaleString('hr-HR') : '—'}
          </div>
        </div>
      ))}
      <Link to="/kalendar">Otvori kalendar →</Link>
    </div>
  );

  const quickAccessCard = (
    <div className="card">
      <h2>{isStudent ? 'Kolegiji koje slušam' : 'Kolegiji koje predajem'}</h2>
      {quickCourses.length === 0 && <p className="muted">Nema kolegija u tekućem semestru.</p>}
      <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap' }}>
        {quickCourses.map((c) => (
          <Link key={c.id} className="btn ghost" to={`/kolegiji/${c.id}`}>
            {c.code} — {c.name}
          </Link>
        ))}
      </div>
    </div>
  );

  const demonstraturesCard = isStudent && (demonstratures.data?.length ?? 0) > 0 && (
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
  );

  if (isStudent) {
    return (
      <div>
        {header}
        <div className="card-grid" style={{ marginTop: 16 }}>
          <div className="stat">
            <div className="value">{courses.isLoading ? '—' : myCourses.length}</div>
            <div className="label">Kolegiji u tekućem semestru</div>
          </div>
          <div className="stat">
            <div className="value">{weekly.length || '—'}</div>
            <div className="label">Termini nastave (tjedno)</div>
          </div>
          <div className="stat">
            <div className="value">{upcomingExams.length || '—'}</div>
            <div className="label">Nadolazeće provjere</div>
          </div>
        </div>

        <div className="form-row" style={{ marginTop: 4 }}>
          <div style={{ flex: 2 }}>{weekCard}</div>
          <div style={{ flex: 1 }}>{examsCard}</div>
        </div>

        {quickAccessCard}
        {demonstraturesCard}

        <div className="card">
          <h2>Moj studij</h2>
          <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap' }}>
            <Link className="btn ghost" to="/kolegiji">
              Moji kolegiji
            </Link>
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
      </div>
    );
  }

  return (
    <div>
      {header}

      <div className="card-grid" style={{ marginTop: 16 }}>
        <div className="stat">
          <div className="value">{courses.data?.length ?? '—'}</div>
          <div className="label">Kolegiji u semestru</div>
        </div>
        <div className="stat">
          <div className="value">{weekly.length || '—'}</div>
          <div className="label">Moji termini (tjedno)</div>
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
        {examsCard}
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

      {quickAccessCard}
    </div>
  );
}
