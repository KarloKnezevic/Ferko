import { useQuery } from '@tanstack/react-query';
import { api } from '../api/client';
import { useI18n } from '../i18n';
import type { WeeklySlot } from '../api/types';

const DAYS = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY'];
const DAY_LABELS: Record<string, string> = {
  MONDAY: 'Ponedjeljak',
  TUESDAY: 'Utorak',
  WEDNESDAY: 'Srijeda',
  THURSDAY: 'Četvrtak',
  FRIDAY: 'Petak',
};

export function CalendarPage() {
  const { t } = useI18n();
  const calendar = useQuery({ queryKey: ['calendar'], queryFn: api.calendar });

  if (calendar.isLoading) return <p className="muted">{t('common.loading')}</p>;

  const weekly = calendar.data?.weekly ?? [];
  const exams = calendar.data?.exams ?? [];
  const byDay: Record<string, WeeklySlot[]> = {};
  for (const slot of weekly) {
    (byDay[slot.dayOfWeek] ??= []).push(slot);
  }

  return (
    <div>
      <h1>{t('nav.calendar')}</h1>
      <p className="muted">{t('calendar.subtitle')}</p>

      <div className="card">
        <h2>{t('calendar.weekly')}</h2>
        {weekly.length === 0 && <p className="muted">{t('calendar.emptyWeekly')}</p>}
        <div className="week-grid">
          {DAYS.filter((d) => byDay[d]?.length).map((day) => (
            <div key={day} className="week-day">
              <div className="week-day-name">{DAY_LABELS[day] ?? day}</div>
              {byDay[day].map((s, i) => (
                <div key={i} className={`slot ${s.type === 'LAB' ? 'lab' : 'lecture'}`}>
                  <div className="slot-time">
                    {s.startsAt}–{s.endsAt}
                  </div>
                  <div className="slot-course">{s.courseCode}</div>
                  <div className="muted">
                    {s.type === 'LAB' ? 'Laboratorij' : 'Predavanje'}
                    {s.room ? ` · ${s.room}` : ''}
                  </div>
                </div>
              ))}
            </div>
          ))}
        </div>
      </div>

      <div className="card">
        <h2>{t('calendar.exams')}</h2>
        {exams.length === 0 && <p className="muted">{t('calendar.emptyExams')}</p>}
        {exams.length > 0 && (
          <table>
            <thead>
              <tr>
                <th>{t('calendar.when')}</th>
                <th>{t('calendar.exam')}</th>
                <th>Kolegij</th>
                <th>Trajanje</th>
              </tr>
            </thead>
            <tbody>
              {exams.map((e, i) => (
                <tr key={i}>
                  <td>{new Date(e.startsAt).toLocaleString('hr-HR')}</td>
                  <td>
                    <strong>{e.shortName}</strong> — {e.title}
                  </td>
                  <td>
                    {e.courseCode} · {e.courseName}
                  </td>
                  <td>{e.durationMinutes} min</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
