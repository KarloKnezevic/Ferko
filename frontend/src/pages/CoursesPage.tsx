import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { api } from '../api/client';
import { DataTable, type Column } from '../components/DataTable';
import type { CourseSummary } from '../api/types';

const ALL = '__all__';

const COLUMNS: Column<CourseSummary>[] = [
  { key: 'code', header: 'Šifra', value: (c) => c.code },
  { key: 'name', header: 'Naziv', value: (c) => c.name },
  { key: 'semesterCode', header: 'Semestar', value: (c) => c.semesterCode },
  { key: 'ects', header: 'ECTS', value: (c) => c.ects },
  { key: 'enrolledStudents', header: 'Upisani', value: (c) => c.enrolledStudents },
  {
    key: 'actions',
    header: '',
    sortable: false,
    value: () => '',
    className: 'row-actions',
    render: (c) => (
      <>
        <Link to={`/kolegiji/${c.id}`}>Detalji</Link>
        <Link to={`/kolegiji/${c.id}/ispiti`}>Provjere znanja</Link>
      </>
    ),
  },
];

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

      <DataTable
        rows={courses.data ?? []}
        columns={COLUMNS}
        rowKey={(c) => c.id}
        searchPlaceholder="Pretraži po šifri ili nazivu…"
        emptyText="Nema kolegija za odabrani semestar."
        loading={courses.isLoading}
      />
    </div>
  );
}
