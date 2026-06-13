import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { api } from '../api/client';

export function CoursesPage() {
  const courses = useQuery({ queryKey: ['courses'], queryFn: api.courses });

  return (
    <div>
      <div className="breadcrumb">Početna › Kolegiji</div>
      <h1>Kolegiji</h1>
      <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
        <table>
          <thead>
            <tr>
              <th>Šifra</th>
              <th>Naziv</th>
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
    </div>
  );
}
