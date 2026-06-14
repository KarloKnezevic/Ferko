import { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, useParams } from 'react-router-dom';
import { api } from '../api/client';
import { useAuth } from '../auth/AuthContext';
import { useI18n } from '../i18n';
import type { AutoGradeResult } from '../api/types';

function gradeCellClass(value: number, max: number): string {
  if (max <= 0) return '';
  const pct = value / max;
  if (pct >= 0.875) return 'g5';
  if (pct >= 0.75) return 'g4';
  if (pct >= 0.625) return 'g3';
  if (pct >= 0.5) return 'g2';
  return 'g1';
}

export function GradingPage() {
  const { id } = useParams();
  const courseId = Number(id);
  const queryClient = useQueryClient();
  const { t } = useI18n();
  const { hasRole } = useAuth();
  const canManage = hasRole('ADMIN', 'NOSITELJ', 'NASTAVNIK', 'ASISTENT_ORGANIZATOR', 'ASISTENT');

  const components = useQuery({
    queryKey: ['grade-components', courseId],
    queryFn: () => api.gradeComponents(courseId),
  });
  const overview = useQuery({
    queryKey: ['points-overview', courseId],
    queryFn: () => api.pointsOverview(courseId),
    enabled: canManage,
  });

  const invalidate = () => {
    queryClient.invalidateQueries({ queryKey: ['points-overview', courseId] });
    queryClient.invalidateQueries({ queryKey: ['grade-components', courseId] });
  };

  // Add component
  const [cName, setCName] = useState('');
  const [cShort, setCShort] = useState('');
  const [cMax, setCMax] = useState(20);
  const addComponent = useMutation({
    mutationFn: () => api.addGradeComponent(courseId, { name: cName, shortName: cShort, maxPoints: cMax }),
    onSuccess: () => {
      setCName('');
      setCShort('');
      invalidate();
    },
  });

  // Enter points
  const [pStudent, setPStudent] = useState<number | ''>('');
  const [pComponent, setPComponent] = useState<number | ''>('');
  const [pPoints, setPPoints] = useState(0);
  const enterPoints = useMutation({
    mutationFn: () =>
      api.enterPoints(courseId, {
        studentId: Number(pStudent),
        componentId: Number(pComponent),
        points: pPoints,
      }),
    onSuccess: invalidate,
  });

  // Assign grade
  const [gStudent, setGStudent] = useState<number | ''>('');
  const [gGrade, setGGrade] = useState(2);
  const assignGrade = useMutation({
    mutationFn: () =>
      api.assignGrade(courseId, { studentId: Number(gStudent), finalGrade: gGrade }),
    onSuccess: invalidate,
  });

  // Auto-grade
  const [examId, setExamId] = useState(1);
  const [keyText, setKeyText] = useState('A; B; A,C; A+B');
  const [subsText, setSubsText] = useState('0036500001 = A;B;C;AB\n0036500002 = A;D;A;A');
  const [autoResult, setAutoResult] = useState<AutoGradeResult[] | null>(null);
  const autoGrade = useMutation({
    mutationFn: () => {
      const correctAnswers = keyText.split(';').map((s) => s.trim()).filter(Boolean);
      const submissions = subsText
        .split('\n')
        .map((line) => line.trim())
        .filter(Boolean)
        .map((line) => {
          const [jmbag, ans = ''] = line.split('=');
          return {
            jmbag: jmbag.trim(),
            answers: ans.split(';').map((a) => a.trim()),
          };
        });
      return api.autoGrade(examId, { correctAnswers, submissions });
    },
    onSuccess: (data) => setAutoResult(data),
  });

  const students = useMemo(
    () => (overview.data ?? []).map((r) => ({ id: r.studentId, label: `${r.fullName} (${r.jmbag})` })),
    [overview.data],
  );
  const comps = components.data ?? [];

  if (!canManage) {
    return (
      <div>
        <div className="breadcrumb">
          <Link to="/kolegiji">{t('nav.courses')}</Link> ›{' '}
          <Link to={`/kolegiji/${courseId}`}>Kolegij</Link> › {t('grading.title')}
        </div>
        <h1>{t('grading.title')}</h1>
        <div className="banner err">{t('common.noAccess')}</div>
      </div>
    );
  }

  return (
    <div>
      <div className="breadcrumb">
        <Link to="/kolegiji">{t('nav.courses')}</Link> ›{' '}
        <Link to={`/kolegiji/${courseId}`}>Kolegij</Link> › {t('grading.title')}
      </div>
      <h1>{t('grading.title')}</h1>
      <p className="muted">{t('grading.subtitle')}</p>

      {canManage && (
        <div className="card">
          <h2>{t('grading.addComponent')}</h2>
          <div className="form-row">
            <div>
              <label>{t('grading.name')}</label>
              <input value={cName} onChange={(e) => setCName(e.target.value)} placeholder="Prvi međuispit" />
            </div>
            <div>
              <label>{t('grading.short')}</label>
              <input value={cShort} onChange={(e) => setCShort(e.target.value)} placeholder="MI1" />
            </div>
            <div>
              <label>{t('grading.maxPoints')}</label>
              <input type="number" value={cMax} onChange={(e) => setCMax(Number(e.target.value))} />
            </div>
            <div style={{ display: 'flex', alignItems: 'flex-end' }}>
              <button disabled={!cName || !cShort || addComponent.isPending} onClick={() => addComponent.mutate()}>
                {t('common.add')}
              </button>
            </div>
          </div>
        </div>
      )}

      <div className="card" style={{ padding: 0, overflow: 'auto' }}>
        <table className="grade-table">
          <thead>
            <tr>
              <th>{t('common.student')}</th>
              <th>JMBAG</th>
              {comps.map((c) => (
                <th key={c.id}>
                  {c.shortName}
                  <span className="th-sub"> / {c.maxPoints}</span>
                </th>
              ))}
              <th>{t('common.total')}</th>
              <th>{t('common.grade')}</th>
            </tr>
          </thead>
          <tbody>
            {overview.data?.map((row) => (
              <tr key={row.studentId}>
                <td>{row.fullName}</td>
                <td>{row.jmbag}</td>
                {comps.map((c) => {
                  const v = row.pointsByComponent[c.shortName] ?? 0;
                  return (
                    <td key={c.id} className={`grade-cell ${gradeCellClass(v, c.maxPoints)}`}>
                      {v}
                    </td>
                  );
                })}
                <td>
                  <strong>{row.total}</strong>
                </td>
                <td>{row.finalGrade > 0 ? row.finalGrade : '—'}</td>
              </tr>
            ))}
            {overview.data?.length === 0 && (
              <tr>
                <td colSpan={comps.length + 4} className="muted">
                  {t('grading.noComponents')}
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {canManage && (
        <div className="form-row">
          <div className="card" style={{ flex: 1 }}>
            <h2>{t('grading.enterPoints')}</h2>
            <label>{t('common.student')}</label>
            <select value={pStudent} onChange={(e) => setPStudent(Number(e.target.value))}>
              <option value="">—</option>
              {students.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.label}
                </option>
              ))}
            </select>
            <label>{t('grading.components')}</label>
            <select value={pComponent} onChange={(e) => setPComponent(Number(e.target.value))}>
              <option value="">—</option>
              {comps.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.shortName} — {c.name}
                </option>
              ))}
            </select>
            <label>{t('grading.points')}</label>
            <input type="number" value={pPoints} onChange={(e) => setPPoints(Number(e.target.value))} />
            <button
              style={{ marginTop: 12 }}
              disabled={pStudent === '' || pComponent === '' || enterPoints.isPending}
              onClick={() => enterPoints.mutate()}
            >
              {t('common.save')}
            </button>
          </div>

          <div className="card" style={{ flex: 1 }}>
            <h2>{t('grading.assignGrade')}</h2>
            <label>{t('common.student')}</label>
            <select value={gStudent} onChange={(e) => setGStudent(Number(e.target.value))}>
              <option value="">—</option>
              {students.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.label}
                </option>
              ))}
            </select>
            <label>{t('common.grade')}</label>
            <select value={gGrade} onChange={(e) => setGGrade(Number(e.target.value))}>
              {[1, 2, 3, 4, 5].map((n) => (
                <option key={n} value={n}>
                  {n}
                </option>
              ))}
            </select>
            <button
              style={{ marginTop: 12 }}
              disabled={gStudent === '' || assignGrade.isPending}
              onClick={() => assignGrade.mutate()}
            >
              {t('common.save')}
            </button>
          </div>
        </div>
      )}

      {canManage && (
        <div className="card">
          <h2>{t('grading.autograde')}</h2>
          <div className="form-row">
            <div style={{ maxWidth: 140 }}>
              <label>ID provjere</label>
              <input type="number" value={examId} onChange={(e) => setExamId(Number(e.target.value))} />
            </div>
            <div>
              <label>{t('grading.correctKey')}</label>
              <input value={keyText} onChange={(e) => setKeyText(e.target.value)} />
            </div>
          </div>
          <label>{t('grading.submissions')}</label>
          <textarea
            rows={4}
            value={subsText}
            onChange={(e) => setSubsText(e.target.value)}
            style={{ width: '100%', fontFamily: 'monospace', padding: 10, borderRadius: 6, border: '1px solid var(--border)' }}
          />
          <button style={{ marginTop: 12 }} disabled={autoGrade.isPending} onClick={() => autoGrade.mutate()}>
            {t('grading.run')}
          </button>
          {autoGrade.isError && (
            <div className="banner err" style={{ marginTop: 12 }}>
              {(autoGrade.error as Error).message}
            </div>
          )}
          {autoResult && (
            <table style={{ marginTop: 14 }}>
              <thead>
                <tr>
                  <th>JMBAG</th>
                  <th>{t('grading.correct')}</th>
                  <th>{t('common.total')}</th>
                </tr>
              </thead>
              <tbody>
                {autoResult.map((r) => (
                  <tr key={r.jmbag}>
                    <td>{r.jmbag}</td>
                    <td>
                      {r.correct} / {r.questions}
                    </td>
                    <td>
                      <strong>{r.total}</strong>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}
    </div>
  );
}
