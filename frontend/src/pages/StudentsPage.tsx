import { useQuery } from '@tanstack/react-query';
import { api } from '../api/client';
import { useAuth } from '../auth/AuthContext';
import { DataTable, type Column } from '../components/DataTable';
import type { Student } from '../api/types';

const COLUMNS: Column<Student>[] = [
  { key: 'jmbag', header: 'JMBAG', value: (s) => s.jmbag },
  { key: 'fullName', header: 'Ime i prezime', value: (s) => s.fullName },
  { key: 'studyProgram', header: 'Studij', value: (s) => s.studyProgram ?? '—' },
  { key: 'yearOfStudy', header: 'Godina', value: (s) => s.yearOfStudy },
];

export function StudentsPage() {
  const { hasRole } = useAuth();
  const canView = hasRole(
    'ADMIN',
    'STUSLU',
    'NOSITELJ',
    'NASTAVNIK',
    'ASISTENT_ORGANIZATOR',
    'ASISTENT',
  );
  const students = useQuery({
    queryKey: ['students'],
    queryFn: api.students,
    enabled: canView,
  });

  if (!canView) {
    return (
      <div>
        <div className="breadcrumb">Početna › Studenti</div>
        <h1>Studenti</h1>
        <div className="banner err">Nemate ovlasti za pristup ovom sadržaju.</div>
      </div>
    );
  }

  return (
    <div>
      <div className="breadcrumb">Početna › Studenti</div>
      <h1>Studenti</h1>
      <DataTable
        rows={students.data ?? []}
        columns={COLUMNS}
        rowKey={(s) => s.id}
        searchPlaceholder="Pretraži po JMBAG-u, imenu, studiju…"
        emptyText="Nema studenata."
        loading={students.isLoading}
      />
    </div>
  );
}
