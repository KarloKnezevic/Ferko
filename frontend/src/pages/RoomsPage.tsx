import { useQuery } from '@tanstack/react-query';
import { api } from '../api/client';

export function RoomsPage() {
  const rooms = useQuery({ queryKey: ['rooms'], queryFn: api.rooms });

  return (
    <div>
      <div className="breadcrumb">Početna › Prostorije</div>
      <h1>Prostorije</h1>
      <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
        <table>
          <thead>
            <tr>
              <th>Oznaka</th>
              <th>Zgrada</th>
              <th>Kapacitet</th>
              <th>Potrebno asistenata</th>
              <th>Računala</th>
            </tr>
          </thead>
          <tbody>
            {rooms.data?.map((room) => (
              <tr key={room.id}>
                <td>{room.code}</td>
                <td>{room.building ?? '—'}</td>
                <td>{room.capacity}</td>
                <td>{room.requiredAssistants}</td>
                <td>{room.hasComputers ? 'Da' : 'Ne'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
