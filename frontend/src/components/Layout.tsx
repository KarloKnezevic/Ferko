import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { useI18n, type Lang } from '../i18n';

export function Layout() {
  const { user, logout, hasRole } = useAuth();
  const { t, lang, setLang } = useI18n();
  const navigate = useNavigate();

  const onLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <>
      <header className="topbar">
        <div className="brand">
          FER<span>KO</span>
        </div>
        <nav>
          <NavLink to="/" end>
            {t('nav.home')}
          </NavLink>
          <NavLink to="/kolegiji">{t('nav.courses')}</NavLink>
          <NavLink to="/kalendar">{t('nav.calendar')}</NavLink>
          <NavLink to="/obavijesti">{t('nav.notices')}</NavLink>
          <NavLink to="/prostorije">{t('nav.rooms')}</NavLink>
          {hasRole('ADMIN', 'STUSLU') && <NavLink to="/studenti">{t('nav.students')}</NavLink>}
        </nav>
        <div className="user">
          <span>{user?.fullName}</span>
          <span className="role-badge">{user?.roles[0]?.replace('ROLE_', '') ?? ''}</span>
          <button
            className="ghost"
            style={{ color: '#fff', borderColor: 'rgba(255,255,255,0.4)' }}
            onClick={onLogout}
          >
            {t('nav.logout')}
          </button>
        </div>
      </header>
      <main className="layout-main">
        <Outlet />
      </main>
      <footer className="sitefooter">
        <div>
          <strong>{t('footer.tagline')}</strong>
          <div className="muted">{t('footer.faculty')}</div>
        </div>
        <div className="lang-switch">
          <span className="muted">{t('footer.language')}:</span>
          {(['hr', 'en'] as Lang[]).map((l) => (
            <button
              key={l}
              className={`lang-btn ${lang === l ? 'active' : ''}`}
              onClick={() => setLang(l)}
            >
              {l.toUpperCase()}
            </button>
          ))}
        </div>
      </footer>
    </>
  );
}
