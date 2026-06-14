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
  const canManageGroups = hasRole('ADMIN', 'NOSITELJ', 'STUSLU');
  const enrollments = useQuery({
    queryKey: ['enrollments', courseId],
    queryFn: () => api.courseEnrollments(courseId),
    enabled: canManageGroups,
  });
  const course = useQuery({ queryKey: ['course', courseId], queryFn: () => api.course(courseId) });
  const components = useQuery({
    queryKey: ['components', courseId],
    queryFn: () => api.courseComponents(courseId),
  });
  const literature = useQuery({
    queryKey: ['literature', courseId],
    queryFn: () => api.courseLiterature(courseId),
  });
  const consultations = useQuery({
    queryKey: ['consultations', courseId],
    queryFn: () => api.courseConsultations(courseId),
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

  const [consDay, setConsDay] = useState('Ponedjeljak');
  const [consStart, setConsStart] = useState('10:00');
  const [consEnd, setConsEnd] = useState('11:00');
  const [consLocation, setConsLocation] = useState('');
  const invalidateConsultations = () =>
    queryClient.invalidateQueries({ queryKey: ['consultations', courseId] });
  const addConsultation = useMutation({
    mutationFn: () =>
      api.addConsultation(courseId, {
        dayOfWeek: consDay,
        startsAt: consStart,
        endsAt: consEnd,
        location: consLocation,
      }),
    onSuccess: () => {
      setConsLocation('');
      invalidateConsultations();
    },
  });
  const removeConsultation = useMutation({
    mutationFn: (consultationId: number) => api.removeConsultation(courseId, consultationId),
    onSuccess: invalidateConsultations,
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

      <div className="card">
        <h2>Konzultacije</h2>
        {(consultations.data?.length ?? 0) === 0 ? (
          <p className="muted">Termini konzultacija još nisu objavljeni.</p>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Dan</th>
                <th>Vrijeme</th>
                <th>Mjesto</th>
                <th>Nastavnik</th>
                {canManageContent && <th></th>}
              </tr>
            </thead>
            <tbody>
              {consultations.data?.map((cons) => (
                <tr key={cons.id}>
                  <td>{cons.dayOfWeek}</td>
                  <td>
                    {cons.startsAt}–{cons.endsAt}
                  </td>
                  <td>{cons.location || '—'}</td>
                  <td>{cons.staffName}</td>
                  {canManageContent && (
                    <td className="row-actions">
                      <a
                        href="#ukloni"
                        onClick={(e) => {
                          e.preventDefault();
                          removeConsultation.mutate(cons.id);
                        }}
                      >
                        Ukloni
                      </a>
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        )}
        {canManageContent && (
          <div className="form-row" style={{ marginTop: 14 }}>
            <div>
              <label>Dan</label>
              <select value={consDay} onChange={(e) => setConsDay(e.target.value)}>
                {['Ponedjeljak', 'Utorak', 'Srijeda', 'Četvrtak', 'Petak'].map((d) => (
                  <option key={d} value={d}>
                    {d}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label>Početak</label>
              <input type="time" value={consStart} onChange={(e) => setConsStart(e.target.value)} />
            </div>
            <div>
              <label>Kraj</label>
              <input type="time" value={consEnd} onChange={(e) => setConsEnd(e.target.value)} />
            </div>
            <div>
              <label>Mjesto</label>
              <input
                value={consLocation}
                onChange={(e) => setConsLocation(e.target.value)}
                placeholder="C-04"
              />
            </div>
            <div style={{ display: 'flex', alignItems: 'flex-end' }}>
              <button disabled={addConsultation.isPending} onClick={() => addConsultation.mutate()}>
                Dodaj
              </button>
            </div>
          </div>
        )}
        {addConsultation.isError && (
          <div className="banner err" style={{ marginTop: 12 }}>
            Provjerite unos (vrijeme u obliku HH:mm, kraj nakon početka).
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

      {canManageGroups && (
        <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
          <h2 style={{ padding: '16px 16px 0' }}>Upisani studenti i razmještaj u grupe</h2>
          <table>
            <thead>
              <tr>
                <th>JMBAG</th>
                <th>Student</th>
                <th>Grupe</th>
                <th>Razmjesti</th>
              </tr>
            </thead>
            <tbody>
              {enrollments.data?.map((e) => (
                <RosterRow
                  key={e.id}
                  courseId={courseId}
                  jmbag={e.studentJmbag}
                  fullName={e.studentFullName}
                  groupCodes={e.groupCodes}
                  groups={c.groups}
                  onAssigned={() =>
                    queryClient.invalidateQueries({ queryKey: ['enrollments', courseId] })
                  }
                />
              ))}
              {(enrollments.data?.length ?? 0) === 0 && (
                <tr>
                  <td colSpan={4} className="muted">
                    Nema upisanih studenata.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

function RosterRow({
  courseId,
  jmbag,
  fullName,
  groupCodes,
  groups,
  onAssigned,
}: {
  courseId: number;
  jmbag: string;
  fullName: string;
  groupCodes: string[];
  groups: { id: number; groupCode: string; type: string }[];
  onAssigned: () => void;
}) {
  const [groupId, setGroupId] = useState<number | ''>('');
  const assign = useMutation({
    mutationFn: () => api.assignGroup(courseId, { jmbag, groupId: groupId as number }),
    onSuccess: () => {
      setGroupId('');
      onAssigned();
    },
  });
  return (
    <tr>
      <td>{jmbag}</td>
      <td>{fullName}</td>
      <td>{groupCodes.length > 0 ? groupCodes.join(', ') : <span className="muted">—</span>}</td>
      <td className="row-actions">
        <select value={groupId} onChange={(e) => setGroupId(Number(e.target.value))}>
          <option value="">— grupa —</option>
          {groups.map((g) => (
            <option key={g.id} value={g.id}>
              {g.groupCode} ({g.type === 'LAB' ? 'Lab' : 'Pred'})
            </option>
          ))}
        </select>
        <button
          className="ghost"
          disabled={groupId === '' || assign.isPending}
          onClick={() => assign.mutate()}
        >
          Dodijeli
        </button>
      </td>
    </tr>
  );
}
