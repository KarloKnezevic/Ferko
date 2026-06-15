import { useEffect, useMemo, useRef, useState } from 'react';
import type { CourseConflictMatrix as Matrix } from '../api/types';

/** Heat colour stops (cream → amber → orange → deep red): hotter = more shared students. */
const STOPS: [number, [number, number, number]][] = [
  [0.0, [255, 245, 205]],
  [0.4, [254, 196, 79]],
  [0.7, [240, 120, 30]],
  [1.0, [178, 24, 24]],
];

function heat(t: number): string {
  const clamped = Math.max(0, Math.min(1, t));
  for (let i = 1; i < STOPS.length; i++) {
    if (clamped <= STOPS[i][0]) {
      const [t0, c0] = STOPS[i - 1];
      const [t1, c1] = STOPS[i];
      const f = (clamped - t0) / (t1 - t0 || 1);
      const r = Math.round(c0[0] + f * (c1[0] - c0[0]));
      const g = Math.round(c0[1] + f * (c1[1] - c0[1]));
      const b = Math.round(c0[2] + f * (c1[2] - c0[2]));
      return `rgb(${r}, ${g}, ${b})`;
    }
  }
  return 'rgb(178, 24, 24)';
}

interface Hover {
  i: number;
  j: number;
  shared: number;
  x: number;
  y: number;
}

/**
 * Course × course overlap heatmap (per M. Čupić's exam-timetabling model): each cell is the number
 * of students shared by two courses — redder = more shared, white = none. Zoomable; hover a cell to
 * see which two courses overlap and by how much. This is the conflict density a scheduler works on.
 */
export function CourseConflictMatrix({ matrix }: { matrix: Matrix }) {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const [cell, setCell] = useState(7);
  const [hover, setHover] = useState<Hover | null>(null);
  const n = matrix.axis.length;

  // Dense symmetric lookup of shared counts, keyed i*n+j (both triangles).
  const lookup = useMemo(() => {
    const m = new Map<number, number>();
    for (const c of matrix.cells) {
      m.set(c.i * n + c.j, c.shared);
      m.set(c.j * n + c.i, c.shared);
    }
    return m;
  }, [matrix.cells, n]);

  const max = Math.max(1, matrix.maxShared);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const size = n * cell;
    canvas.width = size;
    canvas.height = size;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;
    ctx.clearRect(0, 0, size, size);
    // White background = no shared students anywhere by default.
    ctx.fillStyle = '#ffffff';
    ctx.fillRect(0, 0, size, size);
    for (let i = 0; i < n; i++) {
      for (let j = 0; j < n; j++) {
        if (i === j) {
          ctx.fillStyle = '#e6ebf1'; // identity line (course vs itself)
          ctx.fillRect(j * cell, i * cell, cell, cell);
          continue;
        }
        const shared = lookup.get(i * n + j) ?? 0;
        if (shared <= 0) continue;
        ctx.fillStyle = heat(shared / max);
        ctx.fillRect(j * cell, i * cell, cell, cell);
      }
    }
  }, [n, cell, lookup, max]);

  const onMove = (e: React.MouseEvent<HTMLCanvasElement>) => {
    const rect = e.currentTarget.getBoundingClientRect();
    const j = Math.floor((e.clientX - rect.left) / cell);
    const i = Math.floor((e.clientY - rect.top) / cell);
    if (i < 0 || j < 0 || i >= n || j >= n || i === j) {
      setHover(null);
      return;
    }
    const shared = lookup.get(i * n + j) ?? 0;
    setHover({ i, j, shared, x: e.clientX, y: e.clientY });
  };

  if (n === 0) {
    return <p className="muted">Nema kolegija u odabranom semestru.</p>;
  }

  return (
    <div>
      <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 10, flexWrap: 'wrap' }}>
        <span className="muted">
          {n} kolegija · najviše dijeljenih studenata u paru: <strong>{matrix.maxShared}</strong>
        </span>
        <span style={{ flex: 1 }} />
        <button className="ghost" type="button" onClick={() => setCell((c) => Math.max(3, c - 2))}>
          −
        </button>
        <span className="muted" style={{ minWidth: 64, textAlign: 'center' }}>
          Zoom {cell}px
        </span>
        <button className="ghost" type="button" onClick={() => setCell((c) => Math.min(28, c + 2))}>
          +
        </button>
      </div>

      {/* Legend */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 10 }}>
        <span className="muted" style={{ fontSize: 12 }}>0 (bijelo)</span>
        <span
          style={{
            display: 'inline-block',
            width: 180,
            height: 12,
            borderRadius: 3,
            border: '1px solid var(--border)',
            background: `linear-gradient(90deg, ${heat(0.001)}, ${heat(0.4)}, ${heat(0.7)}, ${heat(1)})`,
          }}
        />
        <span className="muted" style={{ fontSize: 12 }}>{matrix.maxShared} dijeljenih</span>
      </div>

      <div style={{ maxHeight: '64vh', overflow: 'auto', border: '1px solid var(--border)', borderRadius: 6 }}>
        <canvas
          ref={canvasRef}
          style={{ display: 'block', imageRendering: 'pixelated', cursor: 'crosshair' }}
          onMouseMove={onMove}
          onMouseLeave={() => setHover(null)}
        />
      </div>

      {hover && (
        <div
          style={{
            position: 'fixed',
            left: Math.min(hover.x + 14, window.innerWidth - 280),
            top: hover.y + 14,
            zIndex: 1200,
            background: 'var(--fer-blue, #00386b)',
            color: '#fff',
            padding: '8px 10px',
            borderRadius: 6,
            fontSize: 13,
            pointerEvents: 'none',
            maxWidth: 260,
            boxShadow: '0 6px 18px rgba(0,0,0,0.3)',
          }}
        >
          <div>
            <strong>{matrix.axis[hover.i].code}</strong> ↔ <strong>{matrix.axis[hover.j].code}</strong>
          </div>
          <div style={{ opacity: 0.85, fontSize: 12, margin: '2px 0' }}>
            {matrix.axis[hover.i].name} · {matrix.axis[hover.j].name}
          </div>
          <div>
            Dijeljenih studenata: <strong>{hover.shared}</strong>
          </div>
        </div>
      )}
    </div>
  );
}
