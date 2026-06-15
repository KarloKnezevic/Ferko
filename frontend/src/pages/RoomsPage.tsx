import { useQuery } from '@tanstack/react-query';
import { api } from '../api/client';
import { DataTable, type Column } from '../components/DataTable';
import type { Room } from '../api/types';

const COLUMNS: Column<Room>[] = [
  { key: 'code', header: 'Oznaka', value: (r) => r.code },
  { key: 'building', header: 'Zgrada', value: (r) => r.building ?? '—' },
  { key: 'capacity', header: 'Kapacitet', value: (r) => r.capacity },
  { key: 'requiredAssistants', header: 'Potrebno asistenata', value: (r) => r.requiredAssistants },
  {
    key: 'hasComputers',
    header: 'Računala',
    value: (r) => (r.hasComputers ? 'Da' : 'Ne'),
  },
];

export function RoomsPage() {
  const rooms = useQuery({ queryKey: ['rooms'], queryFn: api.rooms });

  return (
    <div>
      <div className="breadcrumb">Početna › Prostorije</div>
      <h1>Prostorije</h1>
      <DataTable
        rows={rooms.data ?? []}
        columns={COLUMNS}
        rowKey={(r) => r.id}
        searchPlaceholder="Pretraži po oznaci ili zgradi…"
        emptyText="Nema prostorija."
        loading={rooms.isLoading}
      />
    </div>
  );
}
