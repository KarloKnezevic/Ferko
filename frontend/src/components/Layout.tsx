import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

export function Layout() {
  const { user, logout, hasRole } = useAuth();
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
            Početna
          </NavLink>
          <NavLink to="/kolegiji">Kolegiji</NavLink>
          <NavLink to="/prostorije">Prostorije</NavLink>
          {hasRole('ADMIN', 'STUSLU') && <NavLink to="/studenti">Studenti</NavLink>}
        </nav>
        <div className="user">
          <span>{user?.fullName}</span>
          <span className="role-badge">{user?.roles[0]?.replace('ROLE_', '') ?? ''}</span>
          <button className="ghost" style={{ color: '#fff', borderColor: 'rgba(255,255,255,0.4)' }} onClick={onLogout}>
            Odjava
          </button>
        </div>
      </header>
      <main className="layout-main">
        <Outlet />
      </main>
    </>
  );
}
