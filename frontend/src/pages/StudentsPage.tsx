import { useQuery } from '@tanstack/react-query';
import { api } from '../api/client';

export function StudentsPage() {
  const students = useQuery({ queryKey: ['students'], queryFn: api.students });

  return (
    <div>
      <div className="breadcrumb">Početna › Studenti</div>
      <h1>Studenti</h1>
      <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
        <table>
          <thead>
            <tr>
              <th>JMBAG</th>
              <th>Ime i prezime</th>
              <th>Studij</th>
              <th>Godina</th>
            </tr>
          </thead>
          <tbody>
            {students.data?.map((s) => (
              <tr key={s.id}>
                <td>{s.jmbag}</td>
                <td>{s.fullName}</td>
                <td>{s.studyProgram ?? '—'}</td>
                <td>{s.yearOfStudy}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {students.data?.length === 0 && <p className="muted">Nema studenata.</p>}
    </div>
  );
}
