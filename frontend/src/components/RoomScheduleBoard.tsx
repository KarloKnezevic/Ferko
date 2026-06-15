import { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api } from '../api/client';
import type { BoardSession } from '../api/types';

const DAYS = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY'];
const DAY_HR: Record<string, string> = {
  MONDAY: 'Pon',
  TUESDAY: 'Uto',
  WEDNESDAY: 'Sri',
  THURSDAY: 'Čet',
  FRIDAY: 'Pet',
};
const HOURS = Array.from({ length: 12 }, (_, i) => 8 + i); // 08:00 .. 19:00 start cells

function hourOf(hhmm: string): number {
  return parseInt(hhmm.slice(0, 2), 10);
}

/**
 * Drag-and-drop weekly board for one room: drag a session card onto a free cell to move it there
 * (same room, new weekday/time; duration preserved). Cells the room already occupies are shaded and
 * are not drop targets, so the empty cells visibly are the room's free slots.
 */
export function RoomScheduleBoard() {
  const queryClient = useQueryClient();
  const [roomId, setRoomId] = useState<number | undefined>(undefined);
  const [dragging, setDragging] = useState<BoardSession | null>(null);

  const board = useQuery({
    queryKey: ['resolution-board', roomId ?? null],
    queryFn: () => api.resolutionBoard(roomId),
  });

  const move = useMutation({
    mutationFn: (m: { slotId: number; dayOfWeek: string; startsAt: string; roomId: number | null }) =>
      api.resolveMove(m),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['resolution-board'] });
      queryClient.invalidateQueries({ queryKey: ['timetable-resolution'] });
      queryClient.invalidateQueries({ queryKey: ['timetable'] });
    },
  });

  const data = board.data;
  const selectedRoom = data?.rooms.find((r) => r.id === data.selectedRoomId);

  // Map "DAY-hour" -> session starting there, and the set of hours each session occupies.
  const { startAt, occupied } = useMemo(() => {
    const startAt = new Map<string, BoardSession>();
    const occupied = new Set<string>();
    for (const s of data?.sessions ?? []) {
      startAt.set(`${s.dayOfWeek}-${hourOf(s.startsAt)}`, s);
      for (let h = hourOf(s.startsAt); h < hourOf(s.endsAt); h++) {
        occupied.add(`${s.dayOfWeek}-${h}`);
      }
    }
    return { startAt, occupied };
  }, [data?.sessions]);

  const drop = (day: string, hour: number) => {
    if (!dragging || data?.selectedRoomId == null) return;
    if (occupied.has(`${day}-${hour}`)) return; // not a free cell
    move.mutate({
      slotId: dragging.slotId,
      dayOfWeek: day,
      startsAt: `${String(hour).padStart(2, '0')}:00`,
      roomId: data.selectedRoomId,
    });
    setDragging(null);
  };

  return (
    <div>
      <div style={{ display: 'flex', gap: 10, alignItems: 'center', marginBottom: 10, flexWrap: 'wrap' }}>
        <label htmlFor="board-room">Dvorana</label>
        <select
          id="board-room"
          value={data?.selectedRoomId ?? ''}
          onChange={(e) => setRoomId(Number(e.target.value))}
        >
          {data?.rooms.map((r) => (
            <option key={r.id} value={r.id}>
              {r.code} (kapacitet {r.capacity})
            </option>
          ))}
        </select>
        <span className="muted">Povucite termin na slobodnu (svijetlu) ćeliju da ga premjestite.</span>
        {move.isPending && <span className="muted">Premještam…</span>}
        {move.isError && <span className="banner err">{(move.error as Error).message}</span>}
      </div>

      {board.isLoading && <p className="muted">Učitavanje…</p>}

      {data && (
        <div style={{ overflowX: 'auto' }}>
          <table className="board-table">
            <thead>
              <tr>
                <th style={{ width: 56 }}>Sat</th>
                {DAYS.map((d) => (
                  <th key={d}>{DAY_HR[d]}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {HOURS.map((hour) => (
                <tr key={hour}>
                  <td className="muted" style={{ fontVariantNumeric: 'tabular-nums' }}>
                    {String(hour).padStart(2, '0')}:00
                  </td>
                  {DAYS.map((day) => {
                    const key = `${day}-${hour}`;
                    const session = startAt.get(key);
                    const isOccupied = occupied.has(key);
                    const isDropTarget = dragging != null && !isOccupied;
                    return (
                      <td
                        key={day}
                        className="board-cell"
                        style={{
                          background: isOccupied
                            ? 'rgba(0,56,107,0.06)'
                            : isDropTarget
                              ? 'rgba(10,138,138,0.18)'
                              : undefined,
                          outline: isDropTarget ? '1px dashed var(--fer-blue)' : undefined,
                        }}
                        onDragOver={(e) => {
                          if (isDropTarget) e.preventDefault();
                        }}
                        onDrop={() => drop(day, hour)}
                      >
                        {session && (
                          <div
                            draggable
                            onDragStart={() => setDragging(session)}
                            onDragEnd={() => setDragging(null)}
                            title={`${session.label} · ${session.startsAt}–${session.endsAt} · ${session.enrolled} upisanih`}
                            className="board-card"
                            style={{
                              background: session.overCapacity ? 'rgba(184,0,0,0.14)' : 'var(--surface)',
                              border: `1px solid ${session.overCapacity ? '#b80000' : 'var(--border)'}`,
                            }}
                          >
                            <strong>{session.label}</strong>
                            <div className="muted" style={{ fontSize: 11 }}>
                              {session.startsAt}–{session.endsAt}
                              {session.overCapacity && ' · ⚠ kapacitet'}
                            </div>
                          </div>
                        )}
                      </td>
                    );
                  })}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      {selectedRoom && data?.sessions.length === 0 && (
        <p className="muted">Dvorana {selectedRoom.code} nema termina.</p>
      )}
    </div>
  );
}
