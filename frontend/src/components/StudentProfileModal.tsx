import { useState } from 'react';
import { useMutation, useQuery } from '@tanstack/react-query';
import { api } from '../api/client';
import type { PasswordResetResult, WeeklySlot } from '../api/types';
import { Modal } from './Modal';

const DAY_LABELS: Record<string, string> = {
  MONDAY: 'Ponedjeljak',
  TUESDAY: 'Utorak',
  WEDNESDAY: 'Srijeda',
  THURSDAY: 'Četvrtak',
  FRIDAY: 'Petak',
  SATURDAY: 'Subota',
  SUNDAY: 'Nedjelja',
};
const DAY_ORDER = Object.keys(DAY_LABELS);

function byDay(weekly: WeeklySlot[]): [string, WeeklySlot[]][] {
  const map = new Map<string, WeeklySlot[]>();
  for (const slot of weekly) {
    const list = map.get(slot.dayOfWeek) ?? [];
    list.push(slot);
    map.set(slot.dayOfWeek, list);
  }
  return DAY_ORDER.filter((d) => map.has(d)).map((d) => [
    d,
    map.get(d)!.slice().sort((a, b) => a.startsAt.localeCompare(b.startsAt)),
  ]);
}

/** Admin-only modal showing a user's profile, enrolment, grades and weekly schedule, plus a
 * one-click password reset that reveals the new temporary password once. */
export function StudentProfileModal({ userId, onClose }: { userId: number; onClose: () => void }) {
  const profile = useQuery({
    queryKey: ['admin-user-profile', userId],
    queryFn: () => api.adminUserProfile(userId),
  });
  const [reset, setReset] = useState<PasswordResetResult | null>(null);
  const resetPassword = useMutation({
    mutationFn: () => api.adminResetPassword(userId),
    onSuccess: setReset,
  });

  const p = profile.data;
  const title = p ? p.fullName : 'Profil korisnika';

  return (
    <Modal open onClose={onClose} title={title} wide>
      {profile.isLoading && <p className="muted">Učitavanje…</p>}
      {profile.isError && <div className="banner err">{(profile.error as Error).message}</div>}
      {p && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 18 }}>
          {/* Identity + password reset */}
          <div className="form-row" style={{ alignItems: 'flex-start' }}>
            <div style={{ flex: 2 }}>
              <table>
                <tbody>
                  <tr>
                    <th style={{ width: 160 }}>Korisničko ime</th>
                    <td>{p.username}</td>
                  </tr>
                  <tr>
                    <th>E-mail</th>
                    <td>{p.email || '—'}</td>
                  </tr>
                  {p.student && (
                    <>
                      <tr>
                        <th>JMBAG</th>
                        <td>{p.jmbag || '—'}</td>
                      </tr>
                      <tr>
                        <th>Studij</th>
                        <td>
                          {p.studyProgram || '—'}
                          {p.yearOfStudy ? `, ${p.yearOfStudy}. godina` : ''}
                        </td>
                      </tr>
                    </>
                  )}
                  <tr>
                    <th>Uloge</th>
                    <td>
                      {p.roles.map((r) => (
                        <span key={r} className="role-badge" style={{ marginRight: 4 }}>
                          {r}
                        </span>
                      ))}
                    </td>
                  </tr>
                  <tr>
                    <th>Status</th>
                    <td>
                      {p.active ? (
                        <span className="pill ok">Aktivan</span>
                      ) : (
                        <span className="pill warn">Neaktivan</span>
                      )}
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
            <div style={{ flex: 1 }}>
              <div className="card" style={{ margin: 0 }}>
                <h3 style={{ marginTop: 0 }}>Lozinka</h3>
                <button
                  type="button"
                  disabled={resetPassword.isPending}
                  onClick={() => {
                    if (window.confirm(`Resetirati lozinku korisnika ${p.username}?`))
                      resetPassword.mutate();
                  }}
                >
                  {resetPassword.isPending ? 'Resetiram…' : 'Resetiraj lozinku'}
                </button>
                {resetPassword.isError && (
                  <div className="banner err" style={{ marginTop: 8 }}>
                    {(resetPassword.error as Error).message}
                  </div>
                )}
                {reset && (
                  <div className="banner ok" style={{ marginTop: 8 }}>
                    Nova privremena lozinka (prikazuje se samo sada):
                    <div
                      style={{
                        fontFamily: 'monospace',
                        fontSize: 16,
                        fontWeight: 700,
                        marginTop: 4,
                        userSelect: 'all',
                      }}
                    >
                      {reset.temporaryPassword}
                    </div>
                  </div>
                )}
              </div>
            </div>
          </div>

          {/* Study summary */}
          {p.student && p.summary && (
            <div className="card-grid">
              <div className="stat">
                <div className="value">{p.summary.enrolledCourses}</div>
                <div className="label">Upisani kolegiji</div>
              </div>
              <div className="stat">
                <div className="value">{p.summary.passedCourses}</div>
                <div className="label">Položeni</div>
              </div>
              <div className="stat">
                <div className="value">{p.summary.ectsEarned}</div>
                <div className="label">ECTS ostvareno</div>
              </div>
              <div className="stat">
                <div className="value">{p.summary.averageGrade ? p.summary.averageGrade.toFixed(2) : '—'}</div>
                <div className="label">Prosjek</div>
              </div>
            </div>
          )}

          {/* Courses & grades */}
          {p.student && (
            <div>
              <h3 style={{ marginBottom: 8 }}>Kolegiji i ocjene</h3>
              {p.courses.length === 0 ? (
                <p className="muted">Nema upisanih kolegija.</p>
              ) : (
                <table>
                  <thead>
                    <tr>
                      <th>Šifra</th>
                      <th>Kolegij</th>
                      <th>Bodovi</th>
                      <th>Ocjena</th>
                    </tr>
                  </thead>
                  <tbody>
                    {p.courses.map((c) => (
                      <tr key={c.courseId}>
                        <td>{c.courseCode}</td>
                        <td>{c.courseName}</td>
                        <td>
                          {c.totalPoints.toFixed(1)} / {c.maxPoints.toFixed(1)}
                        </td>
                        <td>{c.finalGrade > 0 ? c.finalGrade : '—'}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          )}

          {/* Weekly schedule */}
          <div>
            <h3 style={{ marginBottom: 8 }}>Tjedni raspored</h3>
            {p.weekly.length === 0 ? (
              <p className="muted">Nema termina nastave.</p>
            ) : (
              byDay(p.weekly).map(([day, slots]) => (
                <div key={day} style={{ marginBottom: 10 }}>
                  <div style={{ fontWeight: 600, marginBottom: 4 }}>{DAY_LABELS[day] ?? day}</div>
                  {slots.map((s, i) => (
                    <div
                      key={i}
                      style={{ display: 'flex', gap: 10, alignItems: 'baseline', padding: '2px 0' }}
                    >
                      <span className="muted" style={{ minWidth: 92, fontVariantNumeric: 'tabular-nums' }}>
                        {s.startsAt}–{s.endsAt}
                      </span>
                      <span>
                        <strong>{s.courseCode}</strong> {s.courseName}
                      </span>
                      {s.room && <span className="pill">{s.room}</span>}
                    </div>
                  ))}
                </div>
              ))
            )}
          </div>
        </div>
      )}
    </Modal>
  );
}
