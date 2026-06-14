import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, useParams } from 'react-router-dom';
import { api } from '../api/client';
import { useAuth } from '../auth/AuthContext';

export function CourseDetailPage() {
  const { id } = useParams();
  const courseId = Number(id);
  const queryClient = useQueryClient();
  const { hasRole } = useAuth();
  const canManageStaff = hasRole('ADMIN', 'NOSITELJ');
  const canManageContent = hasRole('ADMIN', 'NOSITELJ', 'NASTAVNIK', 'ASISTENT_ORGANIZATOR');
  const course = useQuery({ queryKey: ['course', courseId], queryFn: () => api.course(courseId) });
  const components = useQuery({
    queryKey: ['components', courseId],
    queryFn: () => api.courseComponents(courseId),
  });
  const literature = useQuery({
    queryKey: ['literature', courseId],
    queryFn: () => api.courseLiterature(courseId),
  });

  const [staffUser, setStaffUser] = useState('');
  const [staffRole, setStaffRole] = useState('ASISTENT');
  const assignStaff = useMutation({
    mutationFn: () => api.assignCourseStaff(courseId, { username: staffUser, role: staffRole }),
    onSuccess: () => {
      setStaffUser('');
      queryClient.invalidateQueries({ queryKey: ['course', courseId] });
    },
  });

  const [litTitle, setLitTitle] = useState('');
  const [litAuthor, setLitAuthor] = useState('');
  const [litMandatory, setLitMandatory] = useState(true);
  const addLiterature = useMutation({
    mutationFn: () =>
      api.addCourseLiterature(courseId, {
        title: litTitle,
        author: litAuthor,
        mandatory: litMandatory,
        ordinal: literature.data?.length ?? 0,
      }),
    onSuccess: () => {
      setLitTitle('');
      setLitAuthor('');
      queryClient.invalidateQueries({ queryKey: ['literature', courseId] });
    },
  });

  const [compTitle, setCompTitle] = useState('');
  const [compContent, setCompContent] = useState('');
  const addComponent = useMutation({
    mutationFn: () =>
      api.addCourseComponent(courseId, {
        title: compTitle,
        content: compContent,
        ordinal: components.data?.length ?? 0,
        visible: true,
      }),
    onSuccess: () => {
      setCompTitle('');
      setCompContent('');
      queryClient.invalidateQueries({ queryKey: ['components', courseId] });
    },
  });

  if (course.isLoading) return <p className="muted">Učitavanje…</p>;
  if (!course.data) return <p className="muted">Kolegij nije pronađen.</p>;
  const c = course.data;

  return (
    <div>
      <div className="breadcrumb">
        <Link to="/kolegiji">Kolegiji</Link> › {c.code}
      </div>
      <h1>{c.name}</h1>
      <p className="muted">
        {c.code} · {c.ects} ECTS · {c.enrolledStudents} upisanih studenata
      </p>

      <div className="card">
        <h2>O kolegiju</h2>
        <p>{c.description || 'Nema opisa.'}</p>
        {c.literature && (
          <p>
            <strong>Literatura:</strong> {c.literature}
          </p>
        )}
        <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap' }}>
          <Link className="btn" to={`/kolegiji/${c.id}/ispiti`}>
            Administracija provjera znanja
          </Link>
          <Link className="btn" to={`/kolegiji/${c.id}/bodovi`}>
            Preglednik bodova
          </Link>
          <Link className="btn" to={`/kolegiji/${c.id}/ankete`}>
            Ankete
          </Link>
          <Link className="btn" to={`/kolegiji/${c.id}/forum`}>
            Pitanja i problemi
          </Link>
          <Link className="btn" to={`/kolegiji/${c.id}/repozitorij`}>
            Repozitorij
          </Link>
          <Link className="btn" to={`/kolegiji/${c.id}/burza`}>
            Burza grupa
          </Link>
        </div>
      </div>

      <div className="card">
        <h2>Literatura</h2>
        {(literature.data?.length ?? 0) === 0 ? (
          <p className="muted">Literatura još nije unesena.</p>
        ) : (
          <ul style={{ margin: 0, paddingLeft: 18 }}>
            {literature.data?.map((lit) => (
              <li key={lit.id} style={{ marginBottom: 4 }}>
                {lit.title}
                {lit.author && <span className="muted"> — {lit.author}</span>}{' '}
                <span className={`pill ${lit.mandatory ? 'ok' : 'warn'}`}>
                  {lit.mandatory ? 'obavezna' : 'preporučena'}
                </span>
              </li>
            ))}
          </ul>
        )}
        {canManageContent && (
          <div className="form-row" style={{ marginTop: 14 }}>
            <div>
              <label>Naslov</label>
              <input
                value={litTitle}
                onChange={(e) => setLitTitle(e.target.value)}
                placeholder="Uvod u programiranje"
              />
            </div>
            <div>
              <label>Autor</label>
              <input
                value={litAuthor}
                onChange={(e) => setLitAuthor(e.target.value)}
                placeholder="I. Anić"
              />
            </div>
            <div>
              <label>Vrsta</label>
              <select
                value={litMandatory ? 'obavezna' : 'preporucena'}
                onChange={(e) => setLitMandatory(e.target.value === 'obavezna')}
              >
                <option value="obavezna">Obavezna</option>
                <option value="preporucena">Preporučena</option>
              </select>
            </div>
            <div style={{ display: 'flex', alignItems: 'flex-end' }}>
              <button disabled={!litTitle || addLiterature.isPending} onClick={() => addLiterature.mutate()}>
                Dodaj
              </button>
            </div>
          </div>
        )}
      </div>

      {(components.data?.length ?? 0) > 0 &&
        components.data?.map((comp) => (
          <div className="card" key={comp.id}>
            <h2>{comp.title}</h2>
            <p style={{ whiteSpace: 'pre-wrap', margin: 0 }}>{comp.content}</p>
          </div>
        ))}

      {canManageContent && (
        <div className="card">
          <h2>Dodaj komponentu</h2>
          <label>Naslov</label>
          <input value={compTitle} onChange={(e) => setCompTitle(e.target.value)} placeholder="Pravila ocjenjivanja" />
          <label>Sadržaj</label>
          <textarea
            rows={3}
            value={compContent}
            onChange={(e) => setCompContent(e.target.value)}
            style={{ width: '100%', padding: 10, borderRadius: 6, border: '1px solid var(--border)' }}
          />
          <button
            style={{ marginTop: 10 }}
            disabled={!compTitle || addComponent.isPending}
            onClick={() => addComponent.mutate()}
          >
            Dodaj
          </button>
        </div>
      )}

      <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
        <table>
          <thead>
            <tr>
              <th>Nastavno osoblje</th>
              <th>Uloga</th>
            </tr>
          </thead>
          <tbody>
            {c.staff.map((s) => (
              <tr key={`${s.userId}-${s.role}`}>
                <td>{s.fullName}</td>
                <td>{s.role}</td>
              </tr>
            ))}
            {c.staff.length === 0 && (
              <tr>
                <td colSpan={2} className="muted">
                  Nema dodijeljenog osoblja.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {canManageStaff && (
        <div className="card">
          <h2>Dozvole — dodaj nastavnika</h2>
          <div className="form-row">
            <div>
              <label>Korisničko ime</label>
              <input
                value={staffUser}
                onChange={(e) => setStaffUser(e.target.value)}
                placeholder="assistant.iva"
              />
            </div>
            <div>
              <label>Uloga</label>
              <select value={staffRole} onChange={(e) => setStaffRole(e.target.value)}>
                <option value="NOSITELJ">Nositelj</option>
                <option value="NASTAVNIK">Nastavnik</option>
                <option value="ASISTENT_ORGANIZATOR">Asistent organizator</option>
                <option value="ASISTENT">Asistent</option>
              </select>
            </div>
            <div style={{ display: 'flex', alignItems: 'flex-end' }}>
              <button disabled={!staffUser || assignStaff.isPending} onClick={() => assignStaff.mutate()}>
                Dodaj
              </button>
            </div>
          </div>
          {assignStaff.isError && (
            <div className="banner err" style={{ marginTop: 12 }}>
              {(assignStaff.error as Error).message}
            </div>
          )}
        </div>
      )}

      <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
        <table>
          <thead>
            <tr>
              <th>Grupa</th>
              <th>Vrsta</th>
              <th>Kategorija</th>
              <th>Kapacitet</th>
            </tr>
          </thead>
          <tbody>
            {c.groups.map((g) => (
              <tr key={g.id}>
                <td>{g.groupCode}</td>
                <td>{g.type === 'LAB' ? 'Laboratorij' : 'Predavanja'}</td>
                <td>{g.category ?? '—'}</td>
                <td>{g.capacity}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
