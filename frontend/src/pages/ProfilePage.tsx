import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { api } from '../api/client';
import { useAuth } from '../auth/AuthContext';
import { useI18n } from '../i18n';
import { fmtNum } from '../util/format';

export function ProfilePage() {
  const { t } = useI18n();
  const { hasRole } = useAuth();
  const isStudent = hasRole('STUDENT');
  const isStaff = hasRole('ADMIN', 'STUSLU', 'NOSITELJ', 'NASTAVNIK', 'ASISTENT_ORGANIZATOR', 'ASISTENT');

  const profile = useQuery({ queryKey: ['my-profile'], queryFn: api.myProfile });
  const study = useQuery({
    queryKey: ['my-study-summary'],
    queryFn: api.myStudySummary,
    enabled: isStudent,
  });
  const teaching = useQuery({
    queryKey: ['my-teaching-load'],
    queryFn: api.myTeachingLoad,
    enabled: isStaff && !isStudent,
  });

  if (profile.isLoading) return <p className="muted">Učitavanje…</p>;
  if (!profile.data) return <p className="muted">Profil nije dostupan.</p>;
  const p = profile.data;

  return (
    <div>
      <h1>{t('profile.title')}</h1>
      <div className="card">
        <table>
          <tbody>
            <tr>
              <th style={{ width: 220 }}>{t('profile.fullName')}</th>
              <td>{p.fullName}</td>
            </tr>
            <tr>
              <th>{t('profile.username')}</th>
              <td>{p.username}</td>
            </tr>
            <tr>
              <th>{t('profile.email')}</th>
              <td>{p.email || '—'}</td>
            </tr>
            <tr>
              <th>{t('profile.roles')}</th>
              <td>
                {p.roles.map((r) => (
                  <span key={r} className="pill" style={{ marginRight: 6 }}>
                    {r}
                  </span>
                ))}
              </td>
            </tr>
            {p.jmbag && (
              <>
                <tr>
                  <th>JMBAG</th>
                  <td>{p.jmbag}</td>
                </tr>
                <tr>
                  <th>{t('profile.studyProgram')}</th>
                  <td>{p.studyProgram || '—'}</td>
                </tr>
                <tr>
                  <th>{t('profile.year')}</th>
                  <td>{p.yearOfStudy > 0 ? p.yearOfStudy : '—'}</td>
                </tr>
              </>
            )}
          </tbody>
        </table>
      </div>

      {isStudent && study.data && (
        <div className="card">
          <h2>Studijski uspjeh</h2>
          <div className="card-grid">
            <div className="stat">
              <div className="value">
                {study.data.passedCourses}/{study.data.enrolledCourses}
              </div>
              <div className="label">Položeni / upisani kolegiji</div>
            </div>
            <div className="stat">
              <div className="value">
                {study.data.ectsEarned}/{study.data.ectsEnrolled}
              </div>
              <div className="label">Ostvareni / upisani ECTS</div>
            </div>
            <div className="stat">
              <div className="value">
                {study.data.gradedCourses > 0 ? fmtNum(study.data.averageGrade) : '—'}
              </div>
              <div className="label">Prosjek ocjena</div>
            </div>
            <div className="stat">
              <div className="value">
                {study.data.gradedCourses > 0 ? fmtNum(study.data.weightedGpa) : '—'}
              </div>
              <div className="label">Prosjek (ECTS-ponderiran)</div>
            </div>
          </div>
          <Link to="/moji-bodovi">Detalji bodova i ocjena →</Link>
        </div>
      )}

      {isStaff && !isStudent && teaching.data && (
        <div className="card">
          <h2>Nastavno opterećenje</h2>
          <div className="card-grid">
            <div className="stat">
              <div className="value">{teaching.data.courseCount}</div>
              <div className="label">Kolegiji</div>
            </div>
            <div className="stat">
              <div className="value">{teaching.data.totalStudents}</div>
              <div className="label">Studenti ukupno</div>
            </div>
            <div className="stat">
              <div className="value">{fmtNum(teaching.data.weeklyHours)}</div>
              <div className="label">Sati tjedno</div>
            </div>
          </div>
          {teaching.data.courses.length > 0 && (
            <div style={{ overflow: 'hidden', borderRadius: 6, marginTop: 12 }}>
              <table>
                <thead>
                  <tr>
                    <th>Šifra</th>
                    <th>Kolegij</th>
                    <th>Uloga</th>
                    <th>Studenti</th>
                    <th>Sati tjedno</th>
                  </tr>
                </thead>
                <tbody>
                  {teaching.data.courses.map((c) => (
                    <tr key={c.courseId}>
                      <td>{c.code}</td>
                      <td>
                        <Link to={`/kolegiji/${c.courseId}`}>{c.name}</Link>
                      </td>
                      <td>{c.roles}</td>
                      <td>{c.enrolledStudents}</td>
                      <td>{fmtNum(c.weeklyHours)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
