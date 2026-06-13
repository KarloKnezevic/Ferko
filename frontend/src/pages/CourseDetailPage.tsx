import { useQuery } from '@tanstack/react-query';
import { Link, useParams } from 'react-router-dom';
import { api } from '../api/client';

export function CourseDetailPage() {
  const { id } = useParams();
  const courseId = Number(id);
  const course = useQuery({ queryKey: ['course', courseId], queryFn: () => api.course(courseId) });

  if (course.isLoading) return <p className="muted">Učitavanje…</p>;
  if (!course.data) return <p className="muted">Kolegij nije pronađen.</p>;
  const c = course.data;

  return (
    <div>
      <div className="breadcrumb">
        <Link to="/kolegiji">Kolegiji</Link> › {c.code}
      </div>
      <h1>{c.name}</h1>
      <p className="muted">
        {c.code} · {c.ects} ECTS · {c.enrolledStudents} upisanih studenata
      </p>

      <div className="card">
        <h2>O kolegiju</h2>
        <p>{c.description || 'Nema opisa.'}</p>
        {c.literature && (
          <p>
            <strong>Literatura:</strong> {c.literature}
          </p>
        )}
        <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap' }}>
          <Link className="btn" to={`/kolegiji/${c.id}/ispiti`}>
            Administracija provjera znanja
          </Link>
          <Link className="btn" to={`/kolegiji/${c.id}/bodovi`}>
            Preglednik bodova
          </Link>
        </div>
      </div>

      <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
        <table>
          <thead>
            <tr>
              <th>Nastavno osoblje</th>
              <th>Uloga</th>
            </tr>
          </thead>
          <tbody>
            {c.staff.map((s) => (
              <tr key={`${s.userId}-${s.role}`}>
                <td>{s.fullName}</td>
                <td>{s.role}</td>
              </tr>
            ))}
            {c.staff.length === 0 && (
              <tr>
                <td colSpan={2} className="muted">
                  Nema dodijeljenog osoblja.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
        <table>
          <thead>
            <tr>
              <th>Grupa</th>
              <th>Vrsta</th>
              <th>Kategorija</th>
              <th>Kapacitet</th>
            </tr>
          </thead>
          <tbody>
            {c.groups.map((g) => (
              <tr key={g.id}>
                <td>{g.groupCode}</td>
                <td>{g.type === 'LAB' ? 'Laboratorij' : 'Predavanja'}</td>
                <td>{g.category ?? '—'}</td>
                <td>{g.capacity}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
