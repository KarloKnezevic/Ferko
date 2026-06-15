import { useEffect, useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api } from '../api/client';
import { useAuth } from '../auth/AuthContext';
import { useI18n } from '../i18n';
import type {
  GeneratedExamTimetable,
  GeneratedTimetable,
  TimetableComparison,
  TimetableSlot,
} from '../api/types';

const ALGORITHMS = [
  'GENETIC',
  'DIFFERENTIAL_EVOLUTION',
  'MAX_MIN_ANT_SYSTEM',
  'PARTICLE_SWARM',
  'IMMUNE_ALGORITHM',
  'CLONALG',
];

function Sparkline({ values }: { values: number[] }) {
  if (values.length < 2) return null;
  const max = Math.max(...values);
  const min = Math.min(...values);
  const span = max - min || 1;
  const points = values
    .map((v, i) => {
      const x = (i / (values.length - 1)) * 100;
      const y = 24 - ((v - min) / span) * 24;
      return `${x.toFixed(1)},${y.toFixed(1)}`;
    })
    .join(' ');
  return (
    <svg viewBox="0 0 100 24" width="220" height="48" preserveAspectRatio="none">
      <polyline points={points} fill="none" stroke="var(--fer-blue, #0a3d62)" strokeWidth="1.5" />
    </svg>
  );
}

const DAY_ORDER = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];
const DAY_HR: Record<string, string> = {
  MONDAY: 'Ponedjeljak',
  TUESDAY: 'Utorak',
  WEDNESDAY: 'Srijeda',
  THURSDAY: 'Četvrtak',
  FRIDAY: 'Petak',
  SATURDAY: 'Subota',
  SUNDAY: 'Nedjelja',
};

export function TimetablePage() {
  const { t } = useI18n();
  const { hasRole } = useAuth();
  const queryClient = useQueryClient();
  const isAdmin = hasRole('ADMIN');
  const [filter, setFilter] = useState('');
  const [studyYear, setStudyYear] = useState(1);
  const [periods, setPeriods] = useState(15);
  const [algorithm, setAlgorithm] = useState('GENETIC');
  const [generated, setGenerated] = useState<GeneratedTimetable | null>(null);

  const generate = useMutation({
    mutationFn: () => api.generateTimetable({ studyYear, periods, algorithm }),
    onSuccess: (data) => setGenerated(data),
  });
  const apply = useMutation({
    mutationFn: () => api.applyTimetable({ studyYear, periods, algorithm }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['timetable'] });
      queryClient.invalidateQueries({ queryKey: ['timetable-collisions'] });
    },
  });
  // Clear the previewed result when the scope changes, so Apply never persists a stale assignment.
  useEffect(() => {
    setGenerated(null);
    apply.reset();
  }, [studyYear, periods, algorithm]);
  const [examSlots, setExamSlots] = useState(10);
  const [examGenerated, setExamGenerated] = useState<GeneratedExamTimetable | null>(null);
  const generateExam = useMutation({
    mutationFn: () =>
      api.generateExamTimetable({ studyYear, slots: examSlots, algorithm, referenceTerm: 'ZI' }),
    onSuccess: (data) => setExamGenerated(data),
  });
  const [comparison, setComparison] = useState<TimetableComparison | null>(null);
  const compare = useMutation({
    mutationFn: () => api.compareTimetable({ studyYear, periods }),
    onSuccess: (data) => setComparison(data),
  });

  const timetable = useQuery({ queryKey: ['timetable'], queryFn: api.timetable });
  const collisions = useQuery({
    queryKey: ['timetable-collisions'],
    queryFn: api.timetableCollisions,
    enabled: isAdmin,
  });

  const byDay = useMemo(() => {
    const needle = filter.trim().toLowerCase();
    const slots = (timetable.data ?? []).filter(
      (s) =>
        !needle ||
        s.courseName.toLowerCase().includes(needle) ||
        s.courseCode.toLowerCase().includes(needle) ||
        s.room.toLowerCase().includes(needle),
    );
    const grouped: Record<string, TimetableSlot[]> = {};
    for (const slot of slots) {
      (grouped[slot.dayOfWeek] ??= []).push(slot);
    }
    for (const day of Object.keys(grouped)) {
      grouped[day].sort((a, b) => a.startsAt.localeCompare(b.startsAt));
    }
    return grouped;
  }, [timetable.data, filter]);

  return (
    <div>
      <h1>{t('timetable.title')}</h1>
      <p className="muted">{t('timetable.subtitle')}</p>

      {isAdmin && collisions.data && (
        <div className="card">
          <h2>{t('timetable.quality')}</h2>
          <div className="card-grid">
            <div className="stat">
              <div className="value">{collisions.data.totalSlots}</div>
              <div className="label">{t('timetable.slots')}</div>
            </div>
            <div className="stat">
              <div className="value">{collisions.data.roomConflicts}</div>
              <div className="label">{t('timetable.roomConflicts')}</div>
            </div>
            <div className="stat">
              <div className="value">{collisions.data.instructorConflicts}</div>
              <div className="label">{t('timetable.instructorConflicts')}</div>
            </div>
          </div>
          {collisions.data.conflicts.length > 0 && (
            <ul className="conflict-list">
              {collisions.data.conflicts.slice(0, 25).map((c, i) => (
                <li key={i}>
                  <span className={`pill ${c.kind === 'ROOM' ? 'warn' : ''}`}>{c.kind}</span>{' '}
                  <strong>{c.resource}</strong> · {DAY_HR[c.dayOfWeek] ?? c.dayOfWeek}{' '}
                  {c.startsAt}–{c.endsAt}: {c.courseA} ↔ {c.courseB}
                </li>
              ))}
            </ul>
          )}
          {(collisions.data.roomUtilization?.length ?? 0) > 0 && (
            <>
              <h3 style={{ marginTop: 16 }}>{t('timetable.roomUsage')}</h3>
              <ul className="conflict-list">
                {collisions.data.roomUtilization.slice(0, 10).map((r) => (
                  <li key={r.room}>
                    <strong>{r.room}</strong>: {r.slots} {t('timetable.slots').toLowerCase()}
                  </li>
                ))}
              </ul>
            </>
          )}
        </div>
      )}

      {isAdmin && (
        <div className="card">
          <h2>{t('timetable.generate')}</h2>
          <p className="muted">{t('timetable.generateNote')}</p>
          <div className="form-row">
            <div>
              <label>{t('timetable.studyYear')}</label>
              <select value={studyYear} onChange={(e) => setStudyYear(Number(e.target.value))}>
                {[1, 2, 3, 4, 5].map((y) => (
                  <option key={y} value={y}>
                    {y}.
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label>{t('timetable.periods')}</label>
              <input
                type="number"
                min={1}
                value={periods}
                onChange={(e) => setPeriods(Number(e.target.value))}
              />
            </div>
            <div>
              <label>{t('timetable.algorithm')}</label>
              <select value={algorithm} onChange={(e) => setAlgorithm(e.target.value)}>
                {ALGORITHMS.map((a) => (
                  <option key={a} value={a}>
                    {a}
                  </option>
                ))}
              </select>
            </div>
          </div>
          <button
            style={{ marginTop: 12 }}
            disabled={generate.isPending}
            onClick={() => generate.mutate()}
          >
            {generate.isPending ? t('timetable.generating') : t('timetable.runGenerate')}
          </button>
          {generated && (
            <div style={{ marginTop: 16 }}>
              <div className="card-grid">
                <div className="stat">
                  <div className="value">{generated.courses}</div>
                  <div className="label">{t('timetable.course')}</div>
                </div>
                <div className="stat">
                  <div className="value">
                    {generated.baselineConflicts} → {generated.resultConflicts}
                  </div>
                  <div className="label">{t('timetable.conflicts')}</div>
                </div>
                <div className="stat">
                  <div className="value">{generated.iterations}</div>
                  <div className="label">{t('timetable.iterations')}</div>
                </div>
                <div className="stat">
                  <div className="value">
                    {generated.feasible ? (
                      <span className="pill ok">{t('timetable.feasible')}</span>
                    ) : (
                      <span className="pill warn">{t('timetable.infeasible')}</span>
                    )}
                  </div>
                  <div className="label">{generated.algorithm}</div>
                </div>
              </div>
              <Sparkline values={generated.convergence} />
              <div style={{ marginTop: 8 }}>
                <button
                  className="ghost"
                  disabled={apply.isPending}
                  onClick={() => {
                    if (window.confirm(t('timetable.applyConfirm'))) apply.mutate();
                  }}
                >
                  {apply.isPending ? t('timetable.applying') : t('timetable.apply')}
                </button>
                {apply.data && (
                  <span className="pill ok" style={{ marginLeft: 8 }}>
                    {t('timetable.applied')}: {apply.data.slotsWritten}
                  </span>
                )}
                {apply.isError && (
                  <span className="pill warn" style={{ marginLeft: 8 }}>
                    {(apply.error as Error).message}
                  </span>
                )}
              </div>
              <table style={{ marginTop: 12 }}>
                <thead>
                  <tr>
                    <th>{t('timetable.course')}</th>
                    <th>{t('timetable.time')}</th>
                  </tr>
                </thead>
                <tbody>
                  {generated.assignments.slice(0, 50).map((a) => (
                    <tr key={a.courseId}>
                      <td>
                        <strong>{a.courseCode}</strong> {a.courseName}
                      </td>
                      <td>
                        {DAY_HR[a.dayOfWeek] ?? a.dayOfWeek} {a.startsAt}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
          {generate.isError && (
            <div className="banner err" style={{ marginTop: 12 }}>
              {(generate.error as Error).message}
            </div>
          )}
        </div>
      )}

      {isAdmin && (
        <div className="card">
          <h2>{t('examtt.generate')}</h2>
          <p className="muted">{t('examtt.generateNote')}</p>
          <div className="form-row">
            <div>
              <label>{t('timetable.studyYear')}</label>
              <select value={studyYear} onChange={(e) => setStudyYear(Number(e.target.value))}>
                {[1, 2, 3, 4, 5].map((y) => (
                  <option key={y} value={y}>
                    {y}.
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label>{t('examtt.slots')}</label>
              <input
                type="number"
                min={1}
                value={examSlots}
                onChange={(e) => setExamSlots(Number(e.target.value))}
              />
            </div>
            <div>
              <label>{t('timetable.algorithm')}</label>
              <select value={algorithm} onChange={(e) => setAlgorithm(e.target.value)}>
                {ALGORITHMS.map((a) => (
                  <option key={a} value={a}>
                    {a}
                  </option>
                ))}
              </select>
            </div>
          </div>
          <button
            style={{ marginTop: 12 }}
            disabled={generateExam.isPending}
            onClick={() => generateExam.mutate()}
          >
            {generateExam.isPending ? t('timetable.generating') : t('timetable.runGenerate')}
          </button>
          {examGenerated && (
            <div style={{ marginTop: 16 }}>
              <div className="card-grid">
                <div className="stat">
                  <div className="value">{examGenerated.exams}</div>
                  <div className="label">{t('examtt.exams')}</div>
                </div>
                <div className="stat">
                  <div className="value">
                    {examGenerated.baselineConflicts} → {examGenerated.resultConflicts}
                  </div>
                  <div className="label">{t('timetable.conflicts')}</div>
                </div>
                <div className="stat">
                  <div className="value">
                    {examGenerated.legacyConflicts < 0 ? '—' : examGenerated.legacyConflicts}
                  </div>
                  <div className="label">{t('examtt.legacy')}</div>
                </div>
                <div className="stat">
                  <div className="value">
                    {examGenerated.feasible ? (
                      <span className="pill ok">{t('timetable.feasible')}</span>
                    ) : (
                      <span className="pill warn">{t('timetable.infeasible')}</span>
                    )}
                  </div>
                  <div className="label">{examGenerated.algorithm}</div>
                </div>
              </div>
              <Sparkline values={examGenerated.convergence} />
              <table style={{ marginTop: 12 }}>
                <thead>
                  <tr>
                    <th>{t('timetable.course')}</th>
                    <th>{t('examtt.examDate')}</th>
                  </tr>
                </thead>
                <tbody>
                  {examGenerated.assignments.slice(0, 50).map((a) => (
                    <tr key={a.courseId}>
                      <td>
                        <strong>{a.courseCode}</strong> {a.courseName}
                      </td>
                      <td>{a.date}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
          {generateExam.isError && (
            <div className="banner err" style={{ marginTop: 12 }}>
              {(generateExam.error as Error).message}
            </div>
          )}
        </div>
      )}

      {isAdmin && (
        <div className="card">
          <h2>{t('timetable.compare')}</h2>
          <p className="muted">{t('timetable.compareNote')}</p>
          <button disabled={compare.isPending} onClick={() => compare.mutate()}>
            {compare.isPending ? t('timetable.generating') : t('timetable.runCompare')}
          </button>
          {comparison && (
            <table style={{ marginTop: 12 }}>
              <thead>
                <tr>
                  <th>{t('timetable.algorithm')}</th>
                  <th>{t('timetable.conflictsCol')}</th>
                  <th>{t('timetable.iterations')}</th>
                  <th>ms</th>
                  <th>{t('timetable.convergence')}</th>
                </tr>
              </thead>
              <tbody>
                {comparison.runs.map((r) => (
                  <tr key={r.algorithm}>
                    <td>
                      <strong>{r.algorithm}</strong>
                    </td>
                    <td>
                      {r.conflicts}
                      {r.feasible && <span className="pill ok" style={{ marginLeft: 6 }}>0</span>}
                    </td>
                    <td>{r.iterations}</td>
                    <td className="muted">{r.durationMillis}</td>
                    <td>
                      <Sparkline values={r.convergence} />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
          {compare.isError && (
            <div className="banner err" style={{ marginTop: 12 }}>
              {(compare.error as Error).message}
            </div>
          )}
        </div>
      )}

      <div className="card">
        <input
          placeholder={t('timetable.filter')}
          value={filter}
          onChange={(e) => setFilter(e.target.value)}
          style={{ maxWidth: 360 }}
        />
      </div>

      {DAY_ORDER.filter((day) => (byDay[day]?.length ?? 0) > 0).map((day) => (
        <div key={day} className="card" style={{ padding: 0, overflow: 'hidden' }}>
          <h2 style={{ padding: '14px 16px 0' }}>{DAY_HR[day] ?? day}</h2>
          <table>
            <thead>
              <tr>
                <th>{t('timetable.time')}</th>
                <th>{t('timetable.course')}</th>
                <th>{t('timetable.room')}</th>
                <th>{t('timetable.instructor')}</th>
              </tr>
            </thead>
            <tbody>
              {byDay[day].map((s, i) => (
                <tr key={i}>
                  <td>
                    {s.startsAt}–{s.endsAt}
                  </td>
                  <td>
                    <strong>{s.courseCode}</strong> {s.courseName}
                  </td>
                  <td>{s.room || '—'}</td>
                  <td className="muted">{s.instructor || '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ))}

      {(timetable.data?.length ?? 0) === 0 && <p className="muted">{t('timetable.empty')}</p>}
    </div>
  );
}
