import { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api } from '../api/client';
import type { ResolutionCollision } from '../api/types';

const DAYS = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY'];
const DAY_HR: Record<string, string> = {
  MONDAY: 'Pon',
  TUESDAY: 'Uto',
  WEDNESDAY: 'Sri',
  THURSDAY: 'Čet',
  FRIDAY: 'Pet',
};
const KIND_HR: Record<string, string> = {
  ROOM: 'Dvorana',
  INSTRUCTOR: 'Nastavnik',
  GROUP: 'Grupa',
  CAPACITY: 'Kapacitet',
};

function cellColor(value: number, max: number): string {
  if (value <= 0) return 'transparent';
  const intensity = max <= 0 ? 0 : value / max;
  return `rgba(184, 0, 0, ${(0.18 + 0.6 * intensity).toFixed(3)})`;
}

/** Admin workbench: faculty-wide hard-constraint collisions, a clickable heatmap and one-click fixes. */
export function CollisionWorkbench() {
  const queryClient = useQueryClient();
  const report = useQuery({ queryKey: ['timetable-resolution'], queryFn: api.timetableResolution });
  const [selected, setSelected] = useState<{ room: string; day: string } | null>(null);

  const invalidate = () => {
    queryClient.invalidateQueries({ queryKey: ['timetable-resolution'] });
    queryClient.invalidateQueries({ queryKey: ['timetable-collisions'] });
    queryClient.invalidateQueries({ queryKey: ['timetable'] });
  };

  const resolveOne = useMutation({
    mutationFn: (c: ResolutionCollision) =>
      api.resolveMove({
        slotId: c.slotId,
        dayOfWeek: c.suggestion.dayOfWeek!,
        startsAt: c.suggestion.startsAt!,
        roomId: c.suggestion.roomId,
      }),
    onSuccess: invalidate,
  });
  const resolveAll = useMutation({ mutationFn: api.resolveAuto, onSuccess: invalidate });
  const generateAll = useMutation({
    mutationFn: api.generateFacultyTimetable,
    onSuccess: () => {
      setSelected(null);
      invalidate();
    },
  });
  const busy = resolveAll.isPending || generateAll.isPending;

  const collisions = report.data?.collisions ?? [];

  // Room × day collision grid (rooms with at least one collision).
  const grid = useMemo(() => {
    const counts = new Map<string, Map<string, number>>();
    const rooms = new Set<string>();
    for (const c of collisions) {
      const room = c.room || '—';
      rooms.add(room);
      const row = counts.get(room) ?? new Map<string, number>();
      row.set(c.dayOfWeek, (row.get(c.dayOfWeek) ?? 0) + 1);
      counts.set(room, row);
    }
    const rows = [...rooms]
      .map((room) => ({
        room,
        perDay: DAYS.map((d) => counts.get(room)?.get(d) ?? 0),
        total: DAYS.reduce((sum, d) => sum + (counts.get(room)?.get(d) ?? 0), 0),
      }))
      .sort((a, b) => b.total - a.total);
    const max = Math.max(1, ...rows.flatMap((r) => r.perDay));
    return { rows, max };
  }, [collisions]);

  const detail = selected
    ? collisions.filter((c) => (c.room || '—') === selected.room && c.dayOfWeek === selected.day)
    : [];

  if (report.isLoading) return <div className="card"><p className="muted">Učitavanje…</p></div>;
  const data = report.data;
  if (!data) return null;

  return (
    <div className="card">
      <div className="cal-head">
        <h2 style={{ margin: 0 }}>Razrješavanje kolizija</h2>
        <div style={{ display: 'flex', gap: 10, alignItems: 'center' }}>
          {data.conflictFree ? (
            <span className="pill ok">Nema kolizija ✓</span>
          ) : (
            <span className="pill warn">
              {data.roomCollisions + data.instructorCollisions + data.groupCollisions + data.capacityViolations}{' '}
              kolizija
            </span>
          )}
          <button
            disabled={busy}
            title="Generira raspored za sve studente, sve godine i razrješava kolizije"
            onClick={() => {
              if (window.confirm('Generirati cijeli raspored fakulteta? Postojeći raspored bit će preraspoređen.'))
                generateAll.mutate();
            }}
          >
            {generateAll.isPending ? 'Generiram…' : 'Generiraj cijeli raspored'}
          </button>
          <button
            className="ghost"
            disabled={busy || data.conflictFree}
            onClick={() => resolveAll.mutate()}
          >
            {resolveAll.isPending ? 'Rješavam…' : 'Riješi sve'}
          </button>
        </div>
      </div>
      {generateAll.isError && (
        <div className="banner err" style={{ marginTop: 10 }}>
          {(generateAll.error as Error).message}
        </div>
      )}

      <div className="card-grid" style={{ marginTop: 12 }}>
        <div className="stat">
          <div className="value">{data.roomCollisions}</div>
          <div className="label">Dvorana 1×</div>
        </div>
        <div className="stat">
          <div className="value">{data.instructorCollisions}</div>
          <div className="label">Nastavnik 1×</div>
        </div>
        <div className="stat">
          <div className="value">{data.groupCollisions}</div>
          <div className="label">Grupa 1×</div>
        </div>
        <div className="stat">
          <div className="value">{data.capacityViolations}</div>
          <div className="label">Kapacitet</div>
        </div>
      </div>

      {data.conflictFree && (
        <p className="muted" style={{ marginTop: 12 }}>
          Raspored je bez kolizija. 🎉
        </p>
      )}

      {!data.conflictFree && (
        <>
          <h3 style={{ marginTop: 16 }}>Toplinska karta kolizija (kliknite ćeliju za detalje)</h3>
          <div style={{ overflowX: 'auto' }}>
            <table className="heat-table">
              <thead>
                <tr>
                  <th>Dvorana</th>
                  {DAYS.map((d) => (
                    <th key={d}>{DAY_HR[d]}</th>
                  ))}
                  <th>Σ</th>
                </tr>
              </thead>
              <tbody>
                {grid.rows.map((r) => (
                  <tr key={r.room}>
                    <td>
                      <strong>{r.room}</strong>
                    </td>
                    {r.perDay.map((v, i) => {
                      const day = DAYS[i];
                      const active = selected?.room === r.room && selected?.day === day;
                      return (
                        <td
                          key={i}
                          className="heat-cell"
                          style={{
                            background: cellColor(v, grid.max),
                            cursor: v > 0 ? 'pointer' : 'default',
                            outline: active ? '2px solid var(--fer-blue)' : undefined,
                          }}
                          onClick={() => v > 0 && setSelected({ room: r.room, day })}
                        >
                          {v || ''}
                        </td>
                      );
                    })}
                    <td>{r.total}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {selected && (
            <div style={{ marginTop: 16 }}>
              <h3 style={{ marginBottom: 8 }}>
                {selected.room} · {DAY_HR[selected.day] ?? selected.day} — {detail.length} kolizija
              </h3>
              {resolveOne.isError && (
                <div className="banner err">{(resolveOne.error as Error).message}</div>
              )}
              <ul className="conflict-list">
                {detail.map((c, i) => (
                  <li key={i} style={{ display: 'flex', justifyContent: 'space-between', gap: 12 }}>
                    <span>
                      <span className={`pill ${c.kind === 'ROOM' ? 'warn' : ''}`}>
                        {KIND_HR[c.kind] ?? c.kind}
                      </span>{' '}
                      {c.startsAt}–{c.endsAt}: <strong>{c.slotLabel}</strong>
                      {c.otherLabel && <> ↔ {c.otherLabel}</>}
                    </span>
                    {c.suggestion.feasible ? (
                      <button
                        className="ghost"
                        style={{ whiteSpace: 'nowrap' }}
                        disabled={resolveOne.isPending}
                        title={`Premjesti u ${c.suggestion.roomCode}, ${DAY_HR[c.suggestion.dayOfWeek ?? ''] ?? c.suggestion.dayOfWeek} ${c.suggestion.startsAt}`}
                        onClick={() => resolveOne.mutate(c)}
                      >
                        Riješi → {c.suggestion.roomCode} {DAY_HR[c.suggestion.dayOfWeek ?? '']}{' '}
                        {c.suggestion.startsAt}
                      </button>
                    ) : (
                      <span className="muted" style={{ whiteSpace: 'nowrap' }}>
                        nema prijedloga
                      </span>
                    )}
                  </li>
                ))}
              </ul>
            </div>
          )}
        </>
      )}
    </div>
  );
}
