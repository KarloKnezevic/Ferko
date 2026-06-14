import { useMemo, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { api } from '../api/client';
import { useAuth } from '../auth/AuthContext';
import { useI18n } from '../i18n';
import type { TimetableSlot } from '../api/types';

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
  const isAdmin = hasRole('ADMIN');
  const [filter, setFilter] = useState('');

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
