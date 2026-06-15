import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api } from '../api/client';
import { useI18n } from '../i18n';
import { StudentProfileModal } from '../components/StudentProfileModal';

export function AdminPage() {
  const { t } = useI18n();
  const queryClient = useQueryClient();
  const [profileUserId, setProfileUserId] = useState<number | null>(null);

  const users = useQuery({ queryKey: ['admin-users'], queryFn: api.adminUsers });
  const semesters = useQuery({ queryKey: ['semesters'], queryFn: api.semesters });
  const sync = useQuery({ queryKey: ['sync-status'], queryFn: api.syncStatus });
  const settings = useQuery({ queryKey: ['settings'], queryFn: api.settings });
  const audit = useQuery({ queryKey: ['audit'], queryFn: () => api.auditEvents(100) });

  const [code, setCode] = useState('');
  const [academicYear, setAcademicYear] = useState('2025/2026');
  const [term, setTerm] = useState('ZIMSKI');
  const [startsOn, setStartsOn] = useState('2025-10-01');
  const [endsOn, setEndsOn] = useState('2026-02-15');
  const [active, setActive] = useState(false);

  const createSemester = useMutation({
    mutationFn: () =>
      api.createSemester({ code, academicYear, term, startsOn, endsOn, active }),
    onSuccess: () => {
      setCode('');
      queryClient.invalidateQueries({ queryKey: ['semesters'] });
      queryClient.invalidateQueries({ queryKey: ['audit'] });
    },
  });

  return (
    <div>
      <h1>{t('admin.title')}</h1>
      <p className="muted">{t('admin.subtitle')}</p>

      <div className="card">
        <h2>{t('admin.sync')}</h2>
        <p className="muted">{t('admin.syncNote')}</p>
        <div className="card-grid">
          <div className="stat">
            <div className="value">{sync.data?.semesters ?? '—'}</div>
            <div className="label">{t('admin.semestersCount')}</div>
          </div>
          <div className="stat">
            <div className="value">{sync.data?.courses ?? '—'}</div>
            <div className="label">{t('nav.courses')}</div>
          </div>
          <div className="stat">
            <div className="value">{sync.data?.students ?? '—'}</div>
            <div className="label">{t('nav.students')}</div>
          </div>
          <div className="stat">
            <div className="value">{sync.data?.rooms ?? '—'}</div>
            <div className="label">{t('nav.rooms')}</div>
          </div>
        </div>
      </div>

      {settings.data && (
        <div className="card">
          <h2>{t('admin.settings')}</h2>
          <p className="muted">{t('admin.settingsNote')}</p>
          <div className="settings-grid">
            <section>
              <h3>{t('admin.settingsGrading')}</h3>
              <dl className="kv">
                <dt>{t('admin.gradeExcellent')}</dt>
                <dd>≥ {settings.data.grading.excellent}</dd>
                <dt>{t('admin.gradeVeryGood')}</dt>
                <dd>≥ {settings.data.grading.veryGood}</dd>
                <dt>{t('admin.gradeGood')}</dt>
                <dd>≥ {settings.data.grading.good}</dd>
                <dt>{t('admin.gradeSufficient')}</dt>
                <dd>≥ {settings.data.grading.sufficient}</dd>
              </dl>
            </section>
            <section>
              <h3>{t('admin.settingsScheduler')}</h3>
              <dl className="kv">
                <dt>{t('admin.schedPopulation')}</dt>
                <dd>{settings.data.scheduler.defaultPopulationSize}</dd>
                <dt>{t('admin.schedIterations')}</dt>
                <dd>{settings.data.scheduler.defaultIterations}</dd>
                <dt>{t('admin.schedSeed')}</dt>
                <dd>{settings.data.scheduler.defaultSeed}</dd>
              </dl>
            </section>
            <section>
              <h3>{t('admin.settingsSeed')}</h3>
              <dl className="kv">
                <dt>{t('admin.seedMaxCourses')}</dt>
                <dd>{settings.data.seed.maxCourses <= 0 ? '∞' : settings.data.seed.maxCourses}</dd>
                <dt>{t('admin.seedMaxStudents')}</dt>
                <dd>
                  {settings.data.seed.maxStudents <= 0 ? '∞' : settings.data.seed.maxStudents}
                </dd>
                <dt>{t('admin.seedUsers')}</dt>
                <dd>{settings.data.seed.usersEnabled ? t('common.yes') : t('common.no')}</dd>
              </dl>
            </section>
            <section>
              <h3>{t('admin.settingsSecurity')}</h3>
              <dl className="kv">
                <dt>{t('admin.secMail')}</dt>
                <dd>{settings.data.mail.enabled ? t('common.yes') : t('common.no')}</dd>
                <dt>{t('admin.secRateLimit')}</dt>
                <dd>
                  {settings.data.security.loginRateLimitEnabled
                    ? `${settings.data.security.loginRateLimitMaxAttempts} / ${settings.data.security.loginRateLimitWindowSeconds}s`
                    : t('common.no')}
                </dd>
                <dt>{t('admin.secOidc')}</dt>
                <dd>
                  {settings.data.security.oidcIssuerConfigured ? t('common.yes') : t('common.no')}
                </dd>
              </dl>
            </section>
          </div>
        </div>
      )}

      <div className="card">
        <h2>{t('admin.newSemester')}</h2>
        <div className="form-row">
          <div>
            <label>{t('admin.code')}</label>
            <input value={code} onChange={(e) => setCode(e.target.value)} placeholder="2026L" />
          </div>
          <div>
            <label>{t('admin.year')}</label>
            <input value={academicYear} onChange={(e) => setAcademicYear(e.target.value)} />
          </div>
          <div>
            <label>{t('admin.term')}</label>
            <select value={term} onChange={(e) => setTerm(e.target.value)}>
              <option value="ZIMSKI">Zimski</option>
              <option value="LJETNI">Ljetni</option>
            </select>
          </div>
          <div>
            <label>{t('admin.from')}</label>
            <input type="date" value={startsOn} onChange={(e) => setStartsOn(e.target.value)} />
          </div>
          <div>
            <label>{t('admin.to')}</label>
            <input type="date" value={endsOn} onChange={(e) => setEndsOn(e.target.value)} />
          </div>
        </div>
        <label style={{ display: 'flex', alignItems: 'center', gap: 8, marginTop: 10 }}>
          <input
            type="checkbox"
            checked={active}
            onChange={(e) => setActive(e.target.checked)}
            style={{ width: 'auto' }}
          />
          {t('admin.activeSemester')}
        </label>
        <button
          style={{ marginTop: 12 }}
          disabled={!code || createSemester.isPending}
          onClick={() => createSemester.mutate()}
        >
          {t('common.add')}
        </button>
        {createSemester.isError && (
          <div className="banner err" style={{ marginTop: 12 }}>
            {(createSemester.error as Error).message}
          </div>
        )}
      </div>

      <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
        <table>
          <thead>
            <tr>
              <th>{t('admin.semesters')}</th>
              <th>{t('admin.year')}</th>
              <th>{t('admin.term')}</th>
              <th>{t('admin.status')}</th>
            </tr>
          </thead>
          <tbody>
            {semesters.data?.map((s) => (
              <tr key={s.code}>
                <td>
                  <strong>{s.code}</strong>
                </td>
                <td>{s.academicYear}</td>
                <td>{s.term}</td>
                <td>
                  {s.active ? (
                    <span className="pill ok">{t('admin.active')}</span>
                  ) : (
                    <span className="muted">—</span>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
        <table>
          <thead>
            <tr>
              <th>{t('admin.user')}</th>
              <th>{t('admin.username')}</th>
              <th>E-mail</th>
              <th>{t('admin.roles')}</th>
              <th>{t('admin.status')}</th>
            </tr>
          </thead>
          <tbody>
            {users.data?.map((u) => (
              <tr
                key={u.id}
                style={{ cursor: 'pointer' }}
                title="Otvori profil korisnika"
                onClick={() => setProfileUserId(u.id)}
              >
                <td>
                  <a
                    onClick={(e) => {
                      e.preventDefault();
                      setProfileUserId(u.id);
                    }}
                    href="#"
                  >
                    {u.fullName}
                  </a>
                </td>
                <td>{u.username}</td>
                <td className="muted">{u.email}</td>
                <td>
                  {u.roles.map((r) => (
                    <span key={r} className="role-badge" style={{ marginRight: 4 }}>
                      {r}
                    </span>
                  ))}
                </td>
                <td>
                  {u.active ? (
                    <span className="pill ok">{t('admin.active')}</span>
                  ) : (
                    <span className="pill warn">{t('admin.inactive')}</span>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {profileUserId !== null && (
        <StudentProfileModal userId={profileUserId} onClose={() => setProfileUserId(null)} />
      )}

      <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
        <h2 style={{ padding: '16px 16px 0' }}>{t('admin.audit')}</h2>
        <table>
          <thead>
            <tr>
              <th>{t('admin.auditWhen')}</th>
              <th>{t('admin.auditActor')}</th>
              <th>{t('admin.auditAction')}</th>
              <th>{t('admin.auditDetails')}</th>
            </tr>
          </thead>
          <tbody>
            {audit.data?.map((e) => (
              <tr key={e.id}>
                <td className="muted">{new Date(e.occurredAt).toLocaleString('hr-HR')}</td>
                <td>{e.actor}</td>
                <td>
                  <span className="pill">{e.action}</span>
                </td>
                <td className="muted">
                  {[e.entityType, e.entityId].filter(Boolean).join(' #')}
                  {e.details ? ` · ${e.details}` : ''}
                </td>
              </tr>
            ))}
            {(audit.data?.length ?? 0) === 0 && (
              <tr>
                <td colSpan={4} className="muted">
                  {t('admin.auditEmpty')}
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
