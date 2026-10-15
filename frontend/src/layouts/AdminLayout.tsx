import { useEffect, useState } from 'react';
import { NavLink, Outlet, useLocation } from 'react-router-dom';
import clsx from 'clsx';
import { useAuth } from '@/hooks/useAuth';
import { Modal } from '@/components/ui/Modal';
import { Button } from '@/components/ui/Button';

const NAV_SECTIONS: { label: string; items: { to: string; label: string }[] }[] = [
  {
    label: 'Overview',
    items: [{ to: '/', label: 'Dashboard' }],
  },
  {
    label: 'Collection',
    items: [
      { to: '/buildings', label: 'Buildings' },
      { to: '/donations', label: 'Donations' },
      { to: '/donations/new', label: 'Add donation' },
      { to: '/analytics', label: 'Analytics' },
    ],
  },
  {
    label: 'WhatsApp',
    items: [
      { to: '/whatsapp/send', label: 'Send message' },
      { to: '/whatsapp/broadcast', label: 'Broadcast' },
      { to: '/whatsapp/templates', label: 'Templates' },
      { to: '/whatsapp/history', label: 'Broadcast history' },
    ],
  },
  {
    label: 'Account',
    items: [
      { to: '/settings', label: 'Settings' },
      { to: '/debug-controller-4719', label: 'Debug clear' },
    ],
  },
];

function SidebarNav({ onNavigate }: { onNavigate?: () => void }) {
  return (
    <nav className="flex-1 px-3 pb-4 overflow-y-auto" aria-label="Main">
      {NAV_SECTIONS.map((section) => (
        <div key={section.label || section.items[0].to} className="mb-5">
          {section.label && (
            <p className="px-2.5 mb-1.5 text-[10px] font-semibold uppercase tracking-[0.14em] text-white/40">
              {section.label}
            </p>
          )}
          <ul className="flex flex-col gap-0.5">
            {section.items.map((item) => (
              <li key={item.to}>
                <NavLink
                  to={item.to}
                  end={item.to === '/'}
                  onClick={onNavigate}
                  className={({ isActive }) =>
                    clsx(
                      'relative flex items-center gap-2 rounded-lg px-3 py-2 text-sm transition-colors',
                      isActive
                        ? 'bg-white/12 text-white shadow-[inset_0_0_0_1px_rgba(255,255,255,0.06)]'
                        : 'text-white/70 hover:bg-white/5 hover:text-white'
                    )
                  }
                >
                  {({ isActive }) => (
                    <>
                      {isActive && (
                        <span
                          className="absolute left-0 top-1.5 bottom-1.5 w-[3px] rounded-full bg-marigold"
                          aria-hidden
                        />
                      )}
                      <span className={clsx(isActive && 'pl-1')}>{item.label}</span>
                    </>
                  )}
                </NavLink>
              </li>
            ))}
          </ul>
        </div>
      ))}
    </nav>
  );
}

function SidebarBrand() {
  return (
    <div className="px-5 pt-6 pb-5 border-b border-white/10">
      <p className="font-display text-[1.15rem] text-white leading-tight tracking-tight">
        Ganpati Vargani
      </p>
      <p className="text-[11px] text-white/45 mt-1 leading-snug">Collection &amp; communication</p>
    </div>
  );
}

function SidebarFooter({
  username,
  onLogout,
}: {
  username: string | null;
  onLogout: () => void;
}) {
  return (
    <div className="mt-auto px-4 py-4 border-t border-white/10 bg-ink-dark/40">
      <p className="text-[10px] uppercase tracking-wide text-white/40">Signed in as</p>
      <p className="text-sm text-white truncate mt-0.5 font-medium">{username}</p>
      <button
        type="button"
        onClick={onLogout}
        className="mt-2 text-xs text-marigold hover:text-marigold-light transition-colors"
      >
        Sign out
      </button>
    </div>
  );
}

export function AdminLayout() {
  const { username, logout } = useAuth();
  const location = useLocation();
  const [mobileOpen, setMobileOpen] = useState(false);
  const [logoutOpen, setLogoutOpen] = useState(false);
  const [loggingOut, setLoggingOut] = useState(false);

  // Close drawer on route change
  useEffect(() => {
    setMobileOpen(false);
  }, [location.pathname]);

  // Escape closes drawer; lock body scroll while open
  useEffect(() => {
    if (!mobileOpen) return;
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') setMobileOpen(false);
    };
    document.addEventListener('keydown', onKey);
    const prev = document.body.style.overflow;
    document.body.style.overflow = 'hidden';
    return () => {
      document.removeEventListener('keydown', onKey);
      document.body.style.overflow = prev;
    };
  }, [mobileOpen]);

  const closeMobile = () => setMobileOpen(false);
  const requestLogout = () => setLogoutOpen(true);

  const confirmLogout = async () => {
    setLoggingOut(true);
    try {
      await logout();
    } finally {
      setLoggingOut(false);
      setLogoutOpen(false);
    }
  };

  return (
    <div className="flex min-h-screen bg-paper">
      <Modal open={logoutOpen} onClose={() => !loggingOut && setLogoutOpen(false)} title="Sign out">
        <p className="text-sm text-ink50">
          Do you want to log out? Your session token will expire and you will need to sign in again.
        </p>
        <div className="mt-6 flex flex-col-reverse sm:flex-row sm:justify-end gap-2">
          <Button type="button" variant="secondary" disabled={loggingOut} onClick={() => setLogoutOpen(false)}>
            Cancel
          </Button>
          <Button type="button" variant="danger" disabled={loggingOut} onClick={confirmLogout}>
            {loggingOut ? 'Signing out…' : 'Yes, sign out'}
          </Button>
        </div>
      </Modal>

      {/* Desktop sidebar */}
      <aside className="hidden lg:flex w-[15.5rem] shrink-0 bg-ink text-white/90 flex-col sticky top-0 h-screen border-r border-ink-dark">
        <SidebarBrand />
        <SidebarNav />
        <SidebarFooter username={username} onLogout={requestLogout} />
      </aside>

      {/* Mobile drawer */}
      <div
        className={clsx(
          'fixed inset-0 z-40 lg:hidden transition-[visibility] duration-200',
          mobileOpen ? 'visible' : 'invisible'
        )}
        aria-hidden={!mobileOpen}
      >
        <button
          type="button"
          className={clsx(
            'absolute inset-0 bg-ink/50 transition-opacity duration-200',
            mobileOpen ? 'opacity-100' : 'opacity-0'
          )}
          aria-label="Close menu"
          onClick={closeMobile}
        />
        <aside
          className={clsx(
            'absolute left-0 top-0 bottom-0 w-[min(18rem,88vw)] bg-ink text-white/90 flex flex-col shadow-2xl',
            'transition-transform duration-200 ease-out',
            mobileOpen ? 'translate-x-0' : '-translate-x-full'
          )}
        >
          <div className="flex items-start justify-between gap-2 border-b border-white/10">
            <SidebarBrand />
            <button
              type="button"
              onClick={closeMobile}
              className="mt-4 mr-3 rounded-lg p-2 text-white/70 hover:bg-white/10 hover:text-white"
              aria-label="Close navigation"
            >
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden>
                <path d="M6 6l12 12M18 6L6 18" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round" />
              </svg>
            </button>
          </div>
          <SidebarNav onNavigate={closeMobile} />
          <SidebarFooter username={username} onLogout={requestLogout} />
        </aside>
      </div>

      {/* Main column */}
      <div className="flex-1 flex flex-col min-w-0 min-h-screen">
        {/* Mobile top bar */}
        <header className="lg:hidden sticky top-0 z-30 flex items-center gap-3 px-3 sm:px-4 h-14 bg-ink text-white border-b border-ink-dark/80">
          <button
            type="button"
            onClick={() => setMobileOpen(true)}
            className="rounded-lg p-2 -ml-1 text-white/90 hover:bg-white/10"
            aria-label="Open navigation"
            aria-expanded={mobileOpen}
          >
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" aria-hidden>
              <path d="M4 7h16M4 12h16M4 17h16" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round" />
            </svg>
          </button>
          <div className="min-w-0 flex-1">
            <p className="font-display text-base text-white truncate leading-tight">Ganpati Vargani</p>
            <p className="text-[10px] text-white/45 truncate">Collection &amp; communication</p>
          </div>
          <button
            type="button"
            onClick={requestLogout}
            className="shrink-0 text-xs text-marigold hover:text-marigold-light px-2 py-1"
          >
            Sign out
          </button>
        </header>

        <main className="flex-1 w-full mx-auto max-w-[1400px] px-4 py-5 sm:px-6 sm:py-6 lg:px-8 lg:py-8">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
