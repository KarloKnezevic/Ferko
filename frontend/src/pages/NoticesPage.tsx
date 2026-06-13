import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api } from '../api/client';
import { useAuth } from '../auth/AuthContext';
import { useI18n } from '../i18n';

export function NoticesPage() {
  const { t } = useI18n();
  const { hasRole } = useAuth();
  const queryClient = useQueryClient();
  const canPublish = hasRole('ADMIN', 'NOSITELJ', 'NASTAVNIK', 'ASISTENT_ORGANIZATOR', 'STUSLU');

  const notices = useQuery({ queryKey: ['notices'], queryFn: () => api.notices(50) });

  const [title, setTitle] = useState('');
  const [body, setBody] = useState('');
  const [pinned, setPinned] = useState(false);

  const publish = useMutation({
    mutationFn: () => api.publishNotice({ courseId: null, title, body, pinned }),
    onSuccess: () => {
      setTitle('');
      setBody('');
      setPinned(false);
      queryClient.invalidateQueries({ queryKey: ['notices'] });
    },
  });

  return (
    <div>
      <h1>{t('nav.notices')}</h1>

      {canPublish && (
        <div className="card">
          <h2>{t('notices.new')}</h2>
          <div className="form-row">
            <div style={{ flex: 2 }}>
              <label>{t('notices.title')}</label>
              <input value={title} onChange={(e) => setTitle(e.target.value)} />
            </div>
          </div>
          <label>{t('notices.body')}</label>
          <textarea
            rows={3}
            value={body}
            onChange={(e) => setBody(e.target.value)}
            style={{ width: '100%', padding: 10, borderRadius: 6, border: '1px solid var(--border)' }}
          />
          <label style={{ display: 'flex', alignItems: 'center', gap: 8, marginTop: 10 }}>
            <input
              type="checkbox"
              checked={pinned}
              onChange={(e) => setPinned(e.target.checked)}
              style={{ width: 'auto' }}
            />
            {t('notices.pin')}
          </label>
          <button
            style={{ marginTop: 12 }}
            disabled={!title || !body || publish.isPending}
            onClick={() => publish.mutate()}
          >
            {t('notices.publish')}
          </button>
        </div>
      )}

      {notices.isLoading && <p className="muted">{t('common.loading')}</p>}
      {notices.data?.length === 0 && <p className="muted">{t('notices.empty')}</p>}
      {notices.data?.map((n) => (
        <div className="card notice" key={n.id}>
          <div className="notice-head">
            <h2 style={{ margin: 0 }}>
              {n.pinned && <span className="pill warn" style={{ marginRight: 8 }}>📌</span>}
              {n.title}
            </h2>
            <span className="muted">{new Date(n.createdAt).toLocaleString('hr-HR')}</span>
          </div>
          <p style={{ whiteSpace: 'pre-wrap', margin: '8px 0 0' }}>{n.body}</p>
          {n.authorName && <div className="muted" style={{ marginTop: 8 }}>— {n.authorName}</div>}
        </div>
      ))}
    </div>
  );
}
