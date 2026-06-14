import { useQuery } from '@tanstack/react-query';
import { api } from '../api/client';
import { useI18n } from '../i18n';

export function ProfilePage() {
  const { t } = useI18n();
  const profile = useQuery({ queryKey: ['my-profile'], queryFn: api.myProfile });

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
    </div>
  );
}
