import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, useParams } from 'react-router-dom';
import { api } from '../api/client';
import { useAuth } from '../auth/AuthContext';
import { useI18n } from '../i18n';
import type { SurveyResult, SurveyView } from '../api/types';

function SurveyCard({ survey, canManage }: { survey: SurveyView; canManage: boolean }) {
  const { t } = useI18n();
  const [ratings, setRatings] = useState<Record<number, number>>({});
  const [results, setResults] = useState<SurveyResult[] | null>(null);
  const [done, setDone] = useState(false);

  const submit = useMutation({
    mutationFn: () =>
      api.submitSurvey(
        survey.id,
        survey.questions.map((q) => ({ questionId: q.id, rating: ratings[q.id] ?? 3 })),
      ),
    onSuccess: () => setDone(true),
  });
  const loadResults = useMutation({
    mutationFn: () => api.surveyResults(survey.id),
    onSuccess: (data) => setResults(data),
  });

  return (
    <div className="card">
      <h2>{survey.title}</h2>
      {!done ? (
        <>
          {survey.questions.map((q) => (
            <div key={q.id} style={{ marginBottom: 10 }}>
              <label>{q.text}</label>
              <select
                value={ratings[q.id] ?? 3}
                onChange={(e) => setRatings({ ...ratings, [q.id]: Number(e.target.value) })}
                style={{ maxWidth: 220 }}
              >
                {[1, 2, 3, 4, 5].map((n) => (
                  <option key={n} value={n}>
                    {n}
                  </option>
                ))}
              </select>
            </div>
          ))}
          <button disabled={submit.isPending} onClick={() => submit.mutate()}>
            {t('surveys.submit')}
          </button>
        </>
      ) : (
        <div className="banner ok">{t('surveys.thanks')}</div>
      )}

      {canManage && (
        <div style={{ marginTop: 14 }}>
          <button className="ghost" onClick={() => loadResults.mutate()} disabled={loadResults.isPending}>
            {t('surveys.results')}
          </button>
          {results && (
            <table style={{ marginTop: 10 }}>
              <thead>
                <tr>
                  <th>{t('surveys.question')}</th>
                  <th>{t('surveys.average')}</th>
                  <th>{t('surveys.responses')}</th>
                </tr>
              </thead>
              <tbody>
                {results.map((r) => (
                  <tr key={r.questionId}>
                    <td>{r.text}</td>
                    <td>
                      <strong>{r.average.toFixed(2)}</strong>
                    </td>
                    <td>{r.responses}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}
    </div>
  );
}

export function SurveysPage() {
  const { id } = useParams();
  const courseId = Number(id);
  const { t } = useI18n();
  const { hasRole } = useAuth();
  const queryClient = useQueryClient();
  const canManage = hasRole('ADMIN', 'NOSITELJ', 'NASTAVNIK', 'ASISTENT_ORGANIZATOR');

  const surveys = useQuery({
    queryKey: ['surveys', courseId],
    queryFn: () => api.courseSurveys(courseId),
  });

  const [title, setTitle] = useState('');
  const [questions, setQuestions] = useState('Jasnoća izlaganja\nTempo nastave\nKorisnost materijala');
  const create = useMutation({
    mutationFn: () =>
      api.createSurvey(courseId, {
        title,
        questions: questions.split('\n').map((q) => q.trim()).filter(Boolean),
      }),
    onSuccess: () => {
      setTitle('');
      queryClient.invalidateQueries({ queryKey: ['surveys', courseId] });
    },
  });

  return (
    <div>
      <div className="breadcrumb">
        <Link to="/kolegiji">{t('nav.courses')}</Link> ›{' '}
        <Link to={`/kolegiji/${courseId}`}>Kolegij</Link> › {t('surveys.title')}
      </div>
      <h1>{t('surveys.title')}</h1>

      {canManage && (
        <div className="card">
          <h2>{t('surveys.new')}</h2>
          <label>{t('surveys.surveyTitle')}</label>
          <input value={title} onChange={(e) => setTitle(e.target.value)} placeholder="Evaluacija nastave" />
          <label>{t('surveys.questions')}</label>
          <textarea
            rows={4}
            value={questions}
            onChange={(e) => setQuestions(e.target.value)}
            style={{ width: '100%', padding: 10, borderRadius: 6, border: '1px solid var(--border)' }}
          />
          <button style={{ marginTop: 12 }} disabled={!title || create.isPending} onClick={() => create.mutate()}>
            {t('common.add')}
          </button>
        </div>
      )}

      {surveys.data?.length === 0 && <p className="muted">{t('surveys.empty')}</p>}
      {surveys.data?.map((s) => (
        <SurveyCard key={s.id} survey={s} canManage={canManage} />
      ))}
    </div>
  );
}
