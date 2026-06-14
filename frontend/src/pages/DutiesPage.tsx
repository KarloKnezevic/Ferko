import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { api } from '../api/client';

export function DutiesPage() {
  const duties = useQuery({ queryKey: ['my-duties'], queryFn: api.myDuties });

  if (duties.isLoading) return <p className="muted">Učitavanje…</p>;
  const data = duties.data ?? [];

  return (
    <div>
      <h1>Moja dežurstva</h1>
      <p className="muted">Provjere znanja na kojima ste raspoređeni kao dežurni.</p>

      {data.length === 0 ? (
        <div className="card">
          <p className="muted">Trenutno niste raspoređeni ni na jedno dežurstvo.</p>
        </div>
      ) : (
        <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
          <table>
            <thead>
              <tr>
                <th>Provjera</th>
                <th>Kolegij</th>
                <th>Termin</th>
                <th>Dvorana</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {data.map((d) => (
                <tr key={`${d.examId}-${d.roomCode}`}>
                  <td>
                    <strong>{d.examShortName}</strong> — {d.examTitle}
                  </td>
                  <td>
                    <Link to={`/kolegiji/${d.courseId}`}>{d.courseCode}</Link>
                  </td>
                  <td>{d.startsAt ? new Date(d.startsAt).toLocaleString('hr-HR') : '—'}</td>
                  <td>{d.roomCode || '—'}</td>
                  <td>
                    {d.published ? (
                      <span className="pill ok">Objavljeno</span>
                    ) : (
                      <span className="pill warn">U pripremi</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
