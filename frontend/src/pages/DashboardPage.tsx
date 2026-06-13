import { useQuery } from '@tanstack/react-query';
import { api } from '../api/client';
import { useAuth } from '../auth/AuthContext';

export function DashboardPage() {
  const { user } = useAuth();
  const semester = useQuery({ queryKey: ['active-semester'], queryFn: api.activeSemester });
  const courses = useQuery({ queryKey: ['courses'], queryFn: api.courses });
  const rooms = useQuery({ queryKey: ['rooms'], queryFn: api.rooms });

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
          <div className="value">{rooms.data?.length ?? '—'}</div>
          <div className="label">Prostorije</div>
        </div>
        <div className="stat">
          <div className="value">
            {courses.data?.reduce((sum, c) => sum + c.enrolledStudents, 0) ?? '—'}
          </div>
          <div className="label">Ukupno upisa</div>
        </div>
        <div className="stat">
          <div className="value">{semester.data?.code ?? '—'}</div>
          <div className="label">Oznaka semestra</div>
        </div>
      </div>
    </div>
  );
}
