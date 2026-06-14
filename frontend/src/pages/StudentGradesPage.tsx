import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { api } from '../api/client';

export function StudentGradesPage() {
  const grades = useQuery({ queryKey: ['my-grades'], queryFn: api.myGrades });

  if (grades.isLoading) return <p className="muted">Učitavanje…</p>;
  const data = grades.data ?? [];

  return (
    <div>
      <h1>Moji bodovi</h1>
      <p className="muted">Bodovi i ocjene po kolegijima koje pohađate.</p>

      {data.length === 0 ? (
        <div className="card">
          <p className="muted">Trenutno nema bodova na vašim kolegijima.</p>
        </div>
      ) : (
        data.map((course) => (
          <div className="card" key={course.courseId}>
            <h2>
              <Link to={`/kolegiji/${course.courseId}`}>{course.courseCode}</Link> — {course.courseName}
            </h2>
            {course.components.length === 0 ? (
              <p className="muted">Bodovne komponente još nisu definirane.</p>
            ) : (
              <table>
                <thead>
                  <tr>
                    <th>Komponenta</th>
                    <th>Bodovi</th>
                    <th>Maksimum</th>
                  </tr>
                </thead>
                <tbody>
                  {course.components.map((c) => (
                    <tr key={c.shortName}>
                      <td>
                        <strong>{c.shortName}</strong> — {c.name}
                      </td>
                      <td>{c.points}</td>
                      <td>{c.maxPoints}</td>
                    </tr>
                  ))}
                  <tr>
                    <td>
                      <strong>Ukupno</strong>
                    </td>
                    <td>
                      <strong>{course.totalPoints}</strong>
                    </td>
                    <td>{course.maxPoints}</td>
                  </tr>
                </tbody>
              </table>
            )}
            <p style={{ marginTop: 12 }}>
              Konačna ocjena:{' '}
              {course.finalGrade > 0 ? (
                <span className="pill ok">{course.finalGrade}</span>
              ) : (
                <span className="muted">još nije zaključena</span>
              )}
            </p>
          </div>
        ))
      )}
    </div>
  );
}
