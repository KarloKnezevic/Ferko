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

function relativeDay(iso: string): string {
  const now = new Date();
  const then = new Date(iso);
  const startToday = new Date(now.getFullYear(), now.getMonth(), now.getDate());
  const startThen = new Date(then.getFullYear(), then.getMonth(), then.getDate());
  const days = Math.round((startThen.getTime() - startToday.getTime()) / 86_400_000);
  if (days < 0) return 'prošlo';
  if (days === 0) return 'danas';
  if (days === 1) return 'sutra';
  if (days < 7) return `za ${days} dana`;
  if (days < 14) return 'za tjedan dana';
  return `za ${Math.round(days / 7)} tj.`;
}

export function CalendarPage() {
  const { t } = useI18n();
  const calendar = useQuery({ queryKey: ['calendar'], queryFn: api.calendar });

  if (calendar.isLoading) return <p className="muted">{t('common.loading')}</p>;

  const weekly = calendar.data?.weekly ?? [];
  const exams = (calendar.data?.exams ?? [])
    .filter((e) => e.startsAt)
    .slice()
    .sort((a, b) => (a.startsAt! < b.startsAt! ? -1 : 1));
  const byDay: Record<string, WeeklySlot[]> = {};
  for (const slot of weekly) {
    (byDay[slot.dayOfWeek] ??= []).push(slot);
  }
  for (const day of DAYS) {
    byDay[day]?.sort((a, b) => a.startsAt.localeCompare(b.startsAt));
  }

  return (
    <div>
      <h1>{t('nav.calendar')}</h1>
      <p className="muted">{t('calendar.subtitle')}</p>

      <div className="card">
        <div className="cal-head">
          <h2 style={{ margin: 0 }}>{t('calendar.weekly')}</h2>
          <div className="cal-legend">
            <span className="legend-item">
              <span className="legend-dot lecture" /> Predavanje
            </span>
            <span className="legend-item">
              <span className="legend-dot lab" /> Laboratorij
            </span>
          </div>
        </div>
        {weekly.length === 0 && (
          <p className="muted" style={{ marginTop: 12 }}>
            {t('calendar.emptyWeekly')}
          </p>
        )}
        {weekly.length > 0 && (
          <div className="week-board">
            {DAYS.map((day) => (
              <div key={day} className="week-col">
                <div className="week-day-name">{DAY_LABELS[day] ?? day}</div>
                {(byDay[day] ?? []).length === 0 && <div className="week-empty">—</div>}
                {(byDay[day] ?? []).map((s, i) => (
                  <div key={i} className={`slot ${s.type === 'LAB' ? 'lab' : 'lecture'}`}>
                    <div className="slot-time">
                      {s.startsAt}–{s.endsAt}
                    </div>
                    <div className="slot-course">{s.courseName}</div>
                    <div className="slot-meta">
                      <span className="pill">{s.courseCode}</span>
                      {s.room && <span className="muted">{s.room}</span>}
                    </div>
                  </div>
                ))}
              </div>
            ))}
          </div>
        )}
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
                  <td style={{ whiteSpace: 'nowrap' }}>
                    {new Date(e.startsAt!).toLocaleString('hr-HR', {
                      day: '2-digit',
                      month: '2-digit',
                      hour: '2-digit',
                      minute: '2-digit',
                    })}
                    <span className="pill" style={{ marginLeft: 8 }}>
                      {relativeDay(e.startsAt!)}
                    </span>
                  </td>
                  <td>
                    <strong>{e.shortName}</strong> — {e.title}
                  </td>
                  <td>
                    <strong>{e.courseCode}</strong> {e.courseName}
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
