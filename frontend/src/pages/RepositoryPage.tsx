import { useRef, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, useParams } from 'react-router-dom';
import { api } from '../api/client';
import { useAuth } from '../auth/AuthContext';
import { useI18n } from '../i18n';

function formatSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} kB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

export function RepositoryPage() {
  const { id } = useParams();
  const courseId = Number(id);
  const { t } = useI18n();
  const { hasRole } = useAuth();
  const queryClient = useQueryClient();
  const canUpload = hasRole('ADMIN', 'NOSITELJ', 'NASTAVNIK', 'ASISTENT_ORGANIZATOR', 'ASISTENT');
  const fileInput = useRef<HTMLInputElement>(null);
  const [selected, setSelected] = useState<File | null>(null);

  const files = useQuery({ queryKey: ['files', courseId], queryFn: () => api.courseFiles(courseId) });

  const upload = useMutation({
    mutationFn: () => api.uploadCourseFile(courseId, selected as File),
    onSuccess: () => {
      setSelected(null);
      if (fileInput.current) fileInput.current.value = '';
      queryClient.invalidateQueries({ queryKey: ['files', courseId] });
    },
  });

  return (
    <div>
      <div className="breadcrumb">
        <Link to="/kolegiji">{t('nav.courses')}</Link> ›{' '}
        <Link to={`/kolegiji/${courseId}`}>Kolegij</Link> › {t('repo.title')}
      </div>
      <h1>{t('repo.title')}</h1>

      {canUpload && (
        <div className="card">
          <h2>{t('repo.upload')}</h2>
          <input
            ref={fileInput}
            type="file"
            onChange={(e) => setSelected(e.target.files?.[0] ?? null)}
          />
          <button
            style={{ marginTop: 12 }}
            disabled={!selected || upload.isPending}
            onClick={() => upload.mutate()}
          >
            {upload.isPending ? t('repo.uploading') : t('repo.uploadBtn')}
          </button>
          {upload.isError && (
            <div className="banner err" style={{ marginTop: 12 }}>
              {(upload.error as Error).message}
            </div>
          )}
        </div>
      )}

      <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
        <table>
          <thead>
            <tr>
              <th>{t('repo.file')}</th>
              <th>{t('repo.size')}</th>
              <th>{t('repo.uploadedBy')}</th>
              <th>{t('repo.date')}</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {files.data?.map((f) => (
              <tr key={f.id}>
                <td>{f.filename}</td>
                <td>{formatSize(f.sizeBytes)}</td>
                <td className="muted">{f.uploadedBy}</td>
                <td className="muted">{new Date(f.uploadedAt).toLocaleString('hr-HR')}</td>
                <td>
                  <a href={api.fileDownloadUrl(f.id)}>{t('repo.download')}</a>
                </td>
              </tr>
            ))}
            {files.data?.length === 0 && (
              <tr>
                <td colSpan={5} className="muted">
                  {t('repo.empty')}
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
