import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, useParams } from 'react-router-dom';
import { api } from '../api/client';
import { useAuth } from '../auth/AuthContext';
import { useI18n } from '../i18n';

export function GroupExchangePage() {
  const { id } = useParams();
  const courseId = Number(id);
  const { t } = useI18n();
  const { hasRole } = useAuth();
  const queryClient = useQueryClient();
  const canDecide = hasRole('ADMIN', 'NOSITELJ', 'ASISTENT_ORGANIZATOR', 'STUSLU');

  const course = useQuery({ queryKey: ['course', courseId], queryFn: () => api.course(courseId) });
  const requests = useQuery({
    queryKey: ['group-exchange', courseId],
    queryFn: () => api.courseGroupExchange(courseId),
  });

  const invalidate = () => queryClient.invalidateQueries({ queryKey: ['group-exchange', courseId] });

  const [fromGroupId, setFromGroupId] = useState<number | ''>('');
  const [toGroupId, setToGroupId] = useState<number | ''>('');
  const [reason, setReason] = useState('');
  const submit = useMutation({
    mutationFn: () =>
      api.requestGroupExchange(courseId, {
        fromGroupId: fromGroupId === '' ? null : Number(fromGroupId),
        toGroupId: toGroupId === '' ? null : Number(toGroupId),
        reason,
      }),
    onSuccess: () => {
      setReason('');
      invalidate();
    },
  });
  const decide = useMutation({
    mutationFn: ({ requestId, approve }: { requestId: number; approve: boolean }) =>
      api.decideGroupExchange(requestId, approve),
    onSuccess: invalidate,
  });

  const groups = course.data?.groups ?? [];

  return (
    <div>
      <div className="breadcrumb">
        <Link to="/kolegiji">{t('nav.courses')}</Link> ›{' '}
        <Link to={`/kolegiji/${courseId}`}>Kolegij</Link> › {t('burza.title')}
      </div>
      <h1>{t('burza.title')}</h1>
      <p className="muted">{t('burza.subtitle')}</p>

      <div className="card">
        <h2>{t('burza.request')}</h2>
        <div className="form-row">
          <div>
            <label>{t('burza.from')}</label>
            <select value={fromGroupId} onChange={(e) => setFromGroupId(Number(e.target.value))}>
              <option value="">—</option>
              {groups.map((g) => (
                <option key={g.id} value={g.id}>
                  {g.groupCode}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label>{t('burza.to')}</label>
            <select value={toGroupId} onChange={(e) => setToGroupId(Number(e.target.value))}>
              <option value="">—</option>
              {groups.map((g) => (
                <option key={g.id} value={g.id}>
                  {g.groupCode}
                </option>
              ))}
            </select>
          </div>
          <div style={{ flex: 2 }}>
            <label>{t('burza.reason')}</label>
            <input value={reason} onChange={(e) => setReason(e.target.value)} />
          </div>
        </div>
        <button style={{ marginTop: 12 }} disabled={submit.isPending} onClick={() => submit.mutate()}>
          {t('burza.send')}
        </button>
        {submit.isError && (
          <div className="banner err" style={{ marginTop: 12 }}>
            {(submit.error as Error).message}
          </div>
        )}
      </div>

      <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
        <table>
          <thead>
            <tr>
              <th>{t('common.student')}</th>
              <th>{t('burza.switch')}</th>
              <th>{t('burza.reason')}</th>
              <th>{t('admin.status')}</th>
              {canDecide && <th></th>}
            </tr>
          </thead>
          <tbody>
            {requests.data?.map((r) => (
              <tr key={r.id}>
                <td>
                  {r.studentName} <span className="muted">{r.studentJmbag}</span>
                </td>
                <td>
                  {r.fromGroup} → {r.toGroup}
                </td>
                <td className="muted">{r.reason}</td>
                <td>
                  {r.status === 'APPROVED' && <span className="pill ok">Odobreno</span>}
                  {r.status === 'REJECTED' && <span className="pill warn">Odbijeno</span>}
                  {r.status === 'PENDING' && <span className="muted">Na čekanju</span>}
                </td>
                {canDecide && (
                  <td className="row-actions">
                    {r.status === 'PENDING' && (
                      <>
                        <a
                          href="#"
                          onClick={(e) => {
                            e.preventDefault();
                            decide.mutate({ requestId: r.id, approve: true });
                          }}
                        >
                          {t('burza.approve')}
                        </a>
                        <a
                          href="#"
                          onClick={(e) => {
                            e.preventDefault();
                            decide.mutate({ requestId: r.id, approve: false });
                          }}
                        >
                          {t('burza.reject')}
                        </a>
                      </>
                    )}
                  </td>
                )}
              </tr>
            ))}
            {requests.data?.length === 0 && (
              <tr>
                <td colSpan={canDecide ? 5 : 4} className="muted">
                  {t('burza.empty')}
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
