import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { api } from '../api/client';

const KIND_LABELS: Record<string, string> = {
  MEDJUISPIT: 'Međuispit',
  ZAVRSNI: 'Završni ispit',
  KRATKA_PROVJERA: 'Kratka provjera',
  NADOKNADA: 'Nadoknada',
};

export function StudentExamsPage() {
  const queryClient = useQueryClient();
  const exams = useQuery({ queryKey: ['my-exams'], queryFn: api.myExams });
  const invalidate = () => queryClient.invalidateQueries({ queryKey: ['my-exams'] });
  const register = useMutation({
    mutationFn: (examId: number) => api.registerForExam(examId),
    onSuccess: invalidate,
  });
  const unregister = useMutation({
    mutationFn: (examId: number) => api.unregisterFromExam(examId),
    onSuccess: invalidate,
  });

  if (exams.isLoading) return <p className="muted">Učitavanje…</p>;

  const data = exams.data ?? [];
  const busy = register.isPending || unregister.isPending;

  return (
    <div>
      <h1>Moje provjere</h1>
      <p className="muted">
        Provjere znanja na kolegijima koje pohađate. Dvorana i mjesto prikazuju se nakon objave
        rasporeda.
      </p>

      {data.length === 0 ? (
        <div className="card">
          <p className="muted">Trenutno nema definiranih provjera na vašim kolegijima.</p>
        </div>
      ) : (
        <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
          <table>
            <thead>
              <tr>
                <th>Provjera</th>
                <th>Kolegij</th>
                <th>Termin</th>
                <th>Prijava</th>
                <th></th>
                <th>Dvorana / mjesto</th>
              </tr>
            </thead>
            <tbody>
              {data.map((e) => (
                <tr key={e.examId}>
                  <td>
                    <strong>{e.shortName}</strong> — {e.title}
                    <div className="muted">
                      {KIND_LABELS[e.kind] ?? e.kind} · {e.durationMinutes} min · {e.maxPoints} bod.
                    </div>
                  </td>
                  <td>
                    <Link to={`/kolegiji/${e.courseId}`}>{e.courseCode}</Link>
                  </td>
                  <td>{e.startsAt ? new Date(e.startsAt).toLocaleString('hr-HR') : '—'}</td>
                  <td>
                    {e.registered ? (
                      <span className="pill ok">Prijavljen</span>
                    ) : (
                      <span className="pill warn">Nije prijavljen</span>
                    )}
                  </td>
                  <td className="row-actions">
                    {e.published ? (
                      <span className="muted">—</span>
                    ) : e.registered ? (
                      <button className="ghost" disabled={busy} onClick={() => unregister.mutate(e.examId)}>
                        Odjavi se
                      </button>
                    ) : (
                      <button className="secondary" disabled={busy} onClick={() => register.mutate(e.examId)}>
                        Prijavi se
                      </button>
                    )}
                  </td>
                  <td>
                    {!e.published ? (
                      <span className="muted">Raspored još nije objavljen.</span>
                    ) : e.roomCode ? (
                      <>
                        <strong>{e.roomCode}</strong>
                        {e.seatNo != null && <> · mjesto {e.seatNo}</>}
                        {e.testGroup && <> · grupa {e.testGroup}</>}
                      </>
                    ) : (
                      <span className="muted">Niste raspoređeni.</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
