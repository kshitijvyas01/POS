/* =================================================================
   MEDIPOINT TOP HEADER COMPONENT
   ================================================================= */

import { auth } from '../auth.js';

export function renderHeader(pageTitle = 'Dashboard') {
  const user = auth.getUser();
  if (!user) return '';

  const userInitial = (user.displayName || user.email || 'M').charAt(0).toUpperCase();
  const roleBadgeClass = user.role === 'OWNER' ? 'badge-owner' : 'badge-worker';
  const roleBadgeText = user.role === 'OWNER' ? 'Store Owner' : 'Pharmacy Worker';

  return `
    <header class="top-header">
      <div class="header-left">
        <button id="mobile-sidebar-toggle" class="icon-btn d-lg-none" title="Toggle Navigation">
          <i data-lucide="menu"></i>
        </button>
        <div>
          <h2 style="font-size: 1.25rem; font-weight: 700; color: var(--slate-900); margin: 0;">${pageTitle}</h2>
          <span style="font-size: 0.75rem; color: var(--slate-500); font-weight: 500;">MediPoint Pharmacy Management · SQL Edition</span>
        </div>
      </div>

      <div class="header-right">
        <!-- Global Search Bar Placeholder -->
        <div class="header-search d-none d-md-block">
          <i data-lucide="search"></i>
          <input type="text" placeholder="Search medicines, invoices, POS..." />
        </div>

        <!-- Notification Bell -->
        <button class="icon-btn" id="btn-notifications" title="Notifications">
          <i data-lucide="bell"></i>
          <span class="notification-badge"></span>
        </button>

        <!-- User Profile Dropdown Menu -->
        <div class="dropdown">
          <button class="profile-dropdown-btn" id="profileMenuDropdown" data-bs-toggle="dropdown" aria-expanded="false">
            <div class="avatar-circle">${userInitial}</div>
            <div class="d-none d-sm-block text-start" style="line-height: 1.2;">
              <div style="font-size: 0.875rem; font-weight: 700; color: var(--slate-900);">${user.displayName || 'User'}</div>
              <span class="badge-role ${roleBadgeClass}">${roleBadgeText}</span>
            </div>
            <i data-lucide="chevron-down" style="width: 16px; height: 16px; color: var(--slate-400);"></i>
          </button>

          <ul class="dropdown-menu dropdown-menu-end shadow-sm" aria-labelledby="profileMenuDropdown" style="border-radius: var(--radius-sm); font-size: 0.875rem;">
            <li class="px-3 py-2 border-bottom">
              <div class="fw-bold text-dark">${user.displayName || 'User'}</div>
              <div class="text-muted small">${user.email}</div>
            </li>
            ${user.role === 'OWNER' ? `
              <li><a class="dropdown-item py-2" href="#settings"><i data-lucide="settings" class="me-2" style="width:16px;"></i> Store Settings</a></li>
            ` : ''}
            <li><hr class="dropdown-divider"></li>
            <li><button class="dropdown-item py-2 text-danger fw-semibold" id="header-logout-btn"><i data-lucide="log-out" class="me-2" style="width:16px;"></i> Logout</button></li>
          </ul>
        </div>
      </div>
    </header>
  `;
}

export function attachHeaderEvents() {
  const logoutBtn = document.getElementById('header-logout-btn');
  if (logoutBtn) {
    logoutBtn.addEventListener('click', () => {
      auth.logout();
    });
  }

  const mobileToggle = document.getElementById('mobile-sidebar-toggle');
  if (mobileToggle) {
    mobileToggle.addEventListener('click', () => {
      const sidebar = document.querySelector('.app-sidebar');
      if (sidebar) {
        sidebar.classList.toggle('show-mobile');
      }
    });
  }

  const notifBtn = document.getElementById('btn-notifications');
  if (notifBtn) {
    notifBtn.addEventListener('click', () => {
      alert("System Status: MediPoint Step 1 architecture active. Database notifications will arrive here once real-time triggers are connected.");
    });
  }
}
