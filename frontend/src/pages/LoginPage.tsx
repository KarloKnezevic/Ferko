import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

export function LoginPage() {
  const { login, user } = useAuth();
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  if (user) {
    navigate('/', { replace: true });
  }

  const onSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    setError(null);
    setBusy(true);
    try {
      await login(username, password);
      navigate('/', { replace: true });
    } catch {
      setError('Neispravno korisničko ime ili lozinka.');
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="login-wrap">
      <form className="login-card" onSubmit={onSubmit}>
        <div className="brand">
          FER<span>KO</span>
        </div>
        <div className="subtitle">Fakultet elektrotehnike i računarstva</div>
        {error && <div className="banner err">{error}</div>}
        <label htmlFor="username">Korisničko ime</label>
        <input id="username" value={username} onChange={(e) => setUsername(e.target.value)} autoFocus />
        <label htmlFor="password">Lozinka</label>
        <input
          id="password"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />
        <button type="submit" disabled={busy}>
          {busy ? 'Prijava…' : 'Prijava'}
        </button>
        <div className="demo-hint">
          Demo: admin.ferko · lecturer.marko · student.ana
          <br />
          lozinka: ferko123
        </div>
      </form>
    </div>
  );
}
