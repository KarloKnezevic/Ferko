import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, useParams } from 'react-router-dom';
import { api } from '../api/client';
import { useAuth } from '../auth/AuthContext';
import { Sparkline } from '../components/Sparkline';
import type { AlgorithmRun, SeatingResult } from '../api/types';

const ALGO_LABELS: Record<string, string> = {
  GENETIC: 'Genetski algoritam',
  DIFFERENTIAL_EVOLUTION: 'Diferencijska evolucija',
  MAX_MIN_ANT_SYSTEM: 'Max-Min mravlji sustav',
  PARTICLE_SWARM: 'Roj čestica (PSO)',
  IMMUNE_ALGORITHM: 'Imunološki algoritam',
  CLONALG: 'CLONALG',
};

const STRATEGIES = [
  { value: 'GENETIC', label: 'Genetski algoritam (optimizacija)' },
  { value: 'SORTED_GREEDY', label: 'Sortirano — pohlepno' },
  { value: 'SORTED_PROPORTIONAL', label: 'Sortirano — proporcionalno' },
  { value: 'RANDOM_GREEDY', label: 'Slučajno — pohlepno' },
  { value: 'RANDOM_PROPORTIONAL', label: 'Slučajno — proporcionalno' },
];

export function ExamSchedulingPage() {
  const { id } = useParams();
  const courseId = Number(id);
  const queryClient = useQueryClient();
  const { hasRole } = useAuth();
  const canManage = hasRole('ADMIN', 'NOSITELJ', 'ASISTENT_ORGANIZATOR');

  const exams = useQuery({ queryKey: ['exams', courseId], queryFn: () => api.exams(courseId) });
  const rooms = useQuery({ queryKey: ['rooms'], queryFn: api.rooms });

  const [selectedExam, setSelectedExam] = useState<number | null>(null);
  const [result, setResult] = useState<SeatingResult | null>(null);
  const [comparison, setComparison] = useState<AlgorithmRun[] | null>(null);
  const [strategy, setStrategy] = useState('GENETIC');
  const [roomId, setRoomId] = useState<number | ''>('');
  const [roomCapacity, setRoomCapacity] = useState(100);
  const [title, setTitle] = useState('');
  const [shortName, setShortName] = useState('');
  const [kind, setKind] = useState('MEDJUISPIT');

  const invalidate = () => queryClient.invalidateQueries({ queryKey: ['exams', courseId] });

  const createExam = useMutation({
    mutationFn: () =>
      api.createExam(courseId, { title, shortName, kind, durationMinutes: 90, maxPoints: 20 }),
    onSuccess: () => {
      setTitle('');
      setShortName('');
      invalidate();
    },
  });
  const reserveRoom = useMutation({
    mutationFn: (examId: number) =>
      api.reserveRoom(examId, { roomId, capacity: roomCapacity, requiredAssistants: 2 }),
    onSuccess: invalidate,
  });
  const register = useMutation({
    mutationFn: (examId: number) => api.registerFromCourse(examId, courseId),
    onSuccess: invalidate,
  });
  const generate = useMutation({
    mutationFn: (examId: number) => api.generateSeating(examId, strategy),
    onSuccess: (data) => {
      setResult(data);
      invalidate();
    },
  });
  const publish = useMutation({
    mutationFn: (examId: number) => api.publishExam(examId),
    onSuccess: invalidate,
  });
  const compare = useMutation({
    mutationFn: (examId: number) => api.compareAlgorithms(examId),
    onSuccess: (data) => setComparison(data),
  });
  const applyAlgorithm = useMutation({
    mutationFn: ({ examId, algorithm }: { examId: number; algorithm: string }) =>
      api.seatingWithAlgorithm(examId, algorithm),
    onSuccess: (data) => {
      setResult(data);
      invalidate();
    },
  });

  return (
    <div>
      <div className="breadcrumb">
        <Link to="/kolegiji">Kolegiji</Link> › <Link to={`/kolegiji/${courseId}`}>Kolegij</Link> ›
        Administracija provjera znanja
      </div>
      <h1>Administracija provjera znanja</h1>

      {canManage && (
        <div className="card">
          <h2>Definiranje nove provjere</h2>
          <div className="form-row">
            <div>
              <label>Naziv</label>
              <input value={title} onChange={(e) => setTitle(e.target.value)} placeholder="Prvi međuispit" />
            </div>
            <div>
              <label>Kratica</label>
              <input value={shortName} onChange={(e) => setShortName(e.target.value)} placeholder="MI1" />
            </div>
            <div>
              <label>Vrsta</label>
              <select value={kind} onChange={(e) => setKind(e.target.value)}>
                <option value="MEDJUISPIT">Međuispit</option>
                <option value="ZAVRSNI">Završni ispit</option>
                <option value="KRATKA_PROVJERA">Kratka provjera</option>
                <option value="NADOKNADA">Nadoknada</option>
              </select>
            </div>
          </div>
          <button
            style={{ marginTop: 14 }}
            disabled={!title || !shortName || createExam.isPending}
            onClick={() => createExam.mutate()}
          >
            Dodaj provjeru
          </button>
        </div>
      )}

      <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
        <table>
          <thead>
            <tr>
              <th>Provjera</th>
              <th>Vrsta</th>
              <th>Prijavljeni</th>
              <th>Dvorane</th>
              <th>Raspoređeni</th>
              <th>Status</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {exams.data?.map((exam) => (
              <tr key={exam.id}>
                <td>
                  <strong>{exam.shortName}</strong> — {exam.title}
                </td>
                <td>{exam.kind}</td>
                <td>{exam.registeredStudents}</td>
                <td>
                  {exam.reservedRooms} ({exam.totalRoomCapacity} mj.)
                </td>
                <td>{exam.seatedStudents}</td>
                <td>
                  {exam.published ? (
                    <span className="pill ok">Objavljeno</span>
                  ) : (
                    <span className="pill warn">U pripremi</span>
                  )}
                </td>
                <td className="row-actions">
                  <a
                    href="#raspored"
                    onClick={(e) => {
                      e.preventDefault();
                      setSelectedExam(exam.id);
                      setResult(null);
                    }}
                  >
                    Uredi raspored
                  </a>
                </td>
              </tr>
            ))}
            {exams.data?.length === 0 && (
              <tr>
                <td colSpan={7} className="muted">
                  Još nema definiranih provjera.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {selectedExam != null && canManage && (
        <div className="card" id="raspored">
          <h2>Uređivanje rasporeda — provjera #{selectedExam}</h2>
          <div className="form-row">
            <div>
              <label>Dvorana</label>
              <select value={roomId} onChange={(e) => setRoomId(Number(e.target.value))}>
                <option value="">— odaberi —</option>
                {rooms.data?.map((r) => (
                  <option key={r.id} value={r.id}>
                    {r.code} (kap. {r.capacity})
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label>Kapacitet za provjeru</label>
              <input
                type="number"
                value={roomCapacity}
                onChange={(e) => setRoomCapacity(Number(e.target.value))}
              />
            </div>
            <div style={{ display: 'flex', alignItems: 'flex-end' }}>
              <button
                className="secondary"
                disabled={roomId === '' || reserveRoom.isPending}
                onClick={() => reserveRoom.mutate(selectedExam)}
              >
                Rezerviraj dvoranu
              </button>
            </div>
          </div>

          <div style={{ marginTop: 16, display: 'flex', gap: 10, flexWrap: 'wrap' }}>
            <button className="secondary" onClick={() => register.mutate(selectedExam)} disabled={register.isPending}>
              Dohvati studente za provjeru
            </button>
            <select value={strategy} onChange={(e) => setStrategy(e.target.value)} style={{ width: 280 }}>
              {STRATEGIES.map((s) => (
                <option key={s.value} value={s.value}>
                  {s.label}
                </option>
              ))}
            </select>
            <button onClick={() => generate.mutate(selectedExam)} disabled={generate.isPending}>
              {generate.isPending ? 'Generiranje…' : 'Napravi raspored studenata'}
            </button>
            <button className="ghost" onClick={() => publish.mutate(selectedExam)} disabled={publish.isPending}>
              Objavi raspored
            </button>
            <button
              className="secondary"
              onClick={() => compare.mutate(selectedExam)}
              disabled={compare.isPending}
            >
              {compare.isPending ? 'Uspoređivanje…' : 'Usporedi algoritme'}
            </button>
          </div>

          {comparison && (
            <div className="card" style={{ marginTop: 16 }}>
              <h2>Usporedba algoritama raspoređivanja</h2>
              <p className="muted">
                Svih šest metaheuristika izvedeno nad istim problemom razmještaja (jednak budžet i
                sjeme). Manja kazna je bolja; krivulja prikazuje konvergenciju.
              </p>
              <table>
                <thead>
                  <tr>
                    <th>Algoritam</th>
                    <th>Kazna</th>
                    <th>Iteracija</th>
                    <th>Vrijeme</th>
                    <th>Konvergencija</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  {comparison.map((run, idx) => (
                    <tr key={run.algorithm}>
                      <td>
                        {ALGO_LABELS[run.algorithm] ?? run.algorithm}
                        {idx === 0 && <span className="pill ok" style={{ marginLeft: 8 }}>najbolji</span>}
                      </td>
                      <td>{run.penalty.toFixed(2)}</td>
                      <td>{run.iterations}</td>
                      <td>{run.durationMillis} ms</td>
                      <td>
                        <Sparkline values={run.penaltyHistory} />
                      </td>
                      <td>
                        <button
                          className="ghost"
                          disabled={applyAlgorithm.isPending}
                          onClick={() =>
                            applyAlgorithm.mutate({ examId: selectedExam, algorithm: run.algorithm })
                          }
                        >
                          Primijeni
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          {register.data && (
            <div className="banner info" style={{ marginTop: 14 }}>
              Prijavljeno studenata: {register.data.registered}
            </div>
          )}

          {result && (
            <div style={{ marginTop: 16 }}>
              <div className={`banner ${result.feasible ? 'ok' : 'err'}`}>
                Strategija: {result.strategy} · Raspoređeno: {result.seatedStudents} ·{' '}
                {result.feasible
                  ? 'Svi studenti smješteni unutar kapaciteta.'
                  : `Prekapacitiranost (kazna ${result.overCapacityPenalty}).`}
              </div>
              {result.rooms.map((room) => (
                <div key={room.roomId} className="card" style={{ marginTop: 12 }}>
                  <h2>
                    {room.roomCode} — {room.assignedStudents}/{room.capacity}
                  </h2>
                  <table>
                    <thead>
                      <tr>
                        <th>Mjesto</th>
                        <th>JMBAG</th>
                        <th>Student</th>
                      </tr>
                    </thead>
                    <tbody>
                      {room.seats.map((seat) => (
                        <tr key={seat.studentId}>
                          <td>{seat.seatNo}</td>
                          <td>{seat.studentJmbag}</td>
                          <td>{seat.studentFullName}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
