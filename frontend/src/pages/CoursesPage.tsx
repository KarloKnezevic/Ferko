import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { api } from '../api/client';

const ALL = '__all__';

export function CoursesPage() {
  const semesters = useQuery({ queryKey: ['semesters'], queryFn: api.semesters });
  const active = useQuery({ queryKey: ['active-semester'], queryFn: api.activeSemester });

  // Default to the active semester once it has loaded; '' means "not yet chosen".
  const [selected, setSelected] = useState<string>('');
  const semesterCode = selected || active.data?.code || '';
  const filter = semesterCode === ALL ? undefined : semesterCode || undefined;

  const courses = useQuery({
    queryKey: ['courses', filter ?? ALL],
    queryFn: () => api.courses(filter),
    enabled: !!active.data || selected === ALL,
  });

  return (
    <div>
      <div className="breadcrumb">Početna › Kolegiji</div>
      <h1>Kolegiji</h1>
      <p className="muted">Odaberite semestar za pregled kolegija iz prošlih razdoblja.</p>

      <div className="card" style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
        <label htmlFor="semester-select" style={{ margin: 0 }}>
          Semestar:
        </label>
        <select
          id="semester-select"
          value={semesterCode}
          onChange={(e) => setSelected(e.target.value)}
          style={{ width: 'auto', minWidth: 220 }}
        >
          {semesters.data?.map((s) => (
            <option key={s.code} value={s.code}>
              {s.academicYear} — {s.term}
              {s.active ? ' (tekući)' : ''}
            </option>
          ))}
          <option value={ALL}>Svi semestri</option>
        </select>
      </div>

      <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
        <table>
          <thead>
            <tr>
              <th>Šifra</th>
              <th>Naziv</th>
              <th>Semestar</th>
              <th>ECTS</th>
              <th>Upisani</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {courses.data?.map((course) => (
              <tr key={course.id}>
                <td>{course.code}</td>
                <td>{course.name}</td>
                <td>{course.semesterCode}</td>
                <td>{course.ects}</td>
                <td>{course.enrolledStudents}</td>
                <td className="row-actions">
                  <Link to={`/kolegiji/${course.id}`}>Detalji</Link>
                  <Link to={`/kolegiji/${course.id}/ispiti`}>Provjere znanja</Link>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {courses.isLoading && <p className="muted">Učitavanje…</p>}
      {courses.data?.length === 0 && (
        <p className="muted">Nema kolegija za odabrani semestar.</p>
      )}
    </div>
  );
}
