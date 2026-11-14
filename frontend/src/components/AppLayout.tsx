import { Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/useAuth';

export function AppLayout() {
  const { role, logout } = useAuth();
  const navigate = useNavigate();

  async function handleLogout() {
    await logout();
    navigate('/login', { replace: true });
  }

  return (
    <div className="shell">
      <header className="shell__header">
        <span className="shell__brand">RideHailing</span>
        <div className="shell__actions">
          {role && <span className="shell__role">{role}</span>}
          <button className="button button--ghost" onClick={handleLogout}>
            Log out
          </button>
        </div>
      </header>
      <main className="shell__main">
        <Outlet />
      </main>
    </div>
  );
}
