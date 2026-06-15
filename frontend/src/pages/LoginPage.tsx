import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

export function LoginPage() {
  const { login, user } = useAuth();
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  // Redirect already-authenticated users away from the login screen. Done in an effect so we never
  // navigate during render (which React forbids and which silently dropped the redirect before).
  useEffect(() => {
    if (user) {
      navigate('/', { replace: true });
    }
  }, [user, navigate]);

  const onSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    if (busy) return;
    setError(null);
    setBusy(true);
    try {
      // On success the auth context sets `user`, and the effect above performs the redirect —
      // a single navigation path for both already-logged-in and just-logged-in cases.
      await login(username, password);
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
        <div className="subtitle">Sveučilišni fakultet</div>
        {error && <div className="banner err">{error}</div>}
        <label htmlFor="username">Korisničko ime</label>
        <input
          id="username"
          name="username"
          type="text"
          autoComplete="username"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          autoFocus
        />
        <label htmlFor="password">Lozinka</label>
        <input
          id="password"
          name="password"
          type="password"
          autoComplete="current-password"
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
