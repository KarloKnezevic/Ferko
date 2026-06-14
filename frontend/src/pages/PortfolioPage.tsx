import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api } from '../api/client';

const CATEGORIES = ['PROJEKT', 'POSTIGNUĆE', 'VJEŠTINA', 'CERTIFIKAT'];

export function PortfolioPage() {
  const queryClient = useQueryClient();
  const entries = useQuery({ queryKey: ['my-portfolio'], queryFn: api.myPortfolio });
  const invalidate = () => queryClient.invalidateQueries({ queryKey: ['my-portfolio'] });

  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [category, setCategory] = useState('PROJEKT');
  const [link, setLink] = useState('');

  const add = useMutation({
    mutationFn: () => api.addPortfolioEntry({ title, description, category, link }),
    onSuccess: () => {
      setTitle('');
      setDescription('');
      setLink('');
      invalidate();
    },
  });
  const remove = useMutation({
    mutationFn: (id: number) => api.removePortfolioEntry(id),
    onSuccess: invalidate,
  });

  const data = entries.data ?? [];

  return (
    <div>
      <h1>e-Portfolio</h1>
      <p className="muted">Vaši projekti, postignuća, vještine i certifikati.</p>

      {data.length === 0 ? (
        <div className="card">
          <p className="muted">Još nema unosa u vašem e-portfoliju.</p>
        </div>
      ) : (
        data.map((e) => (
          <div className="card" key={e.id}>
            <div style={{ display: 'flex', gap: 8, alignItems: 'baseline' }}>
              <h2 style={{ margin: 0 }}>{e.title}</h2>
              {e.category && <span className="pill">{e.category}</span>}
              <span className="muted" style={{ marginLeft: 'auto', fontSize: 13 }}>
                {new Date(e.createdAt).toLocaleDateString('hr-HR')}
              </span>
            </div>
            {e.description && <p style={{ whiteSpace: 'pre-wrap' }}>{e.description}</p>}
            {e.link && (
              <p>
                <a href={e.link} target="_blank" rel="noreferrer">
                  {e.link}
                </a>
              </p>
            )}
            <button className="ghost" disabled={remove.isPending} onClick={() => remove.mutate(e.id)}>
              Ukloni
            </button>
          </div>
        ))
      )}

      <div className="card">
        <h2>Novi unos</h2>
        <div className="form-row">
          <div>
            <label>Naslov</label>
            <input value={title} onChange={(e) => setTitle(e.target.value)} placeholder="Završni rad" />
          </div>
          <div>
            <label>Kategorija</label>
            <select value={category} onChange={(e) => setCategory(e.target.value)}>
              {CATEGORIES.map((c) => (
                <option key={c} value={c}>
                  {c}
                </option>
              ))}
            </select>
          </div>
          <div style={{ flex: 1 }}>
            <label>Poveznica</label>
            <input value={link} onChange={(e) => setLink(e.target.value)} placeholder="https://…" />
          </div>
        </div>
        <label>Opis</label>
        <textarea
          rows={3}
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          style={{ width: '100%', padding: 10, borderRadius: 6, border: '1px solid var(--border)' }}
        />
        <button style={{ marginTop: 10 }} disabled={!title || add.isPending} onClick={() => add.mutate()}>
          Dodaj unos
        </button>
      </div>
    </div>
  );
}
