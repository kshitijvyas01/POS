/* =================================================================
   MEDIPOINT SIDEBAR NAVIGATION COMPONENT (16 MODULES ARCHITECTURE)
   ================================================================= */

import { auth } from '../auth.js';

export function renderSidebar(activeRoute = 'dashboard') {
  const userRole = auth.getRole();
  const isOwner = userRole === 'OWNER';

  const menuItems = [
    { id: 'dashboard', label: 'Dashboard', icon: 'layout-dashboard', phase: 1, reqOwner: false },
    { id: 'medicines', label: 'Medicines', icon: 'pill', phase: 2, reqOwner: false },
    { id: 'inventory', label: 'Inventory', icon: 'boxes', phase: 2, reqOwner: false },
    { id: 'pos', label: 'POS', icon: 'shopping-cart', phase: 2, reqOwner: false },
    { id: 'sales', label: 'Sales', icon: 'trending-up', phase: 2, reqOwner: false },
    { id: 'customers', label: 'Customers', icon: 'users', phase: 2, reqOwner: false },
    { id: 'vendors', label: 'Vendors', icon: 'truck', phase: 2, reqOwner: true },
    { id: 'purchases', label: 'Purchases', icon: 'shopping-bag', phase: 2, reqOwner: true },
    { id: 'payments', label: 'Payments', icon: 'credit-card', phase: 2, reqOwner: false },
    { id: 'invoices', label: 'Invoices', icon: 'file-text', phase: 2, reqOwner: false },
    { id: 'analytics', label: 'Analytics', icon: 'bar-chart-3', phase: 2, reqOwner: true },
    { id: 'employees', label: 'Employees', icon: 'user-check', phase: 2, reqOwner: true },
    { id: 'attendance', label: 'Attendance', icon: 'calendar-check', phase: 2, reqOwner: false },
    { id: 'leave', label: 'Leave', icon: 'calendar-clock', phase: 2, reqOwner: false },
    { id: 'salary', label: 'Salary', icon: 'indian-rupee', phase: 2, reqOwner: false },
    { id: 'settings', label: 'Settings', icon: 'settings', phase: 1, reqOwner: true }
  ];

  const menuHtml = menuItems.map(item => {
    // If worker tries to access owner-only items, hide them from sidebar navigation
    if (item.reqOwner && !isOwner) {
      return '';
    }

    const isActive = activeRoute === item.id ? 'active' : '';
    const isImplemented = item.phase <= 2;

    return `
      <a href="#${item.id}" class="menu-item ${isActive} ${!isImplemented ? 'disabled-item' : ''}" data-route="${item.id}">
        <div class="menu-item-left">
          <i data-lucide="${item.icon}"></i>
          <span>${item.label}</span>
        </div>
        ${!isImplemented ? `<span class="badge-coming-soon">Soon</span>` : ''}
      </a>
    `;
  }).join('');

  return `
    <aside class="app-sidebar">
      <div class="sidebar-header">
        <div class="brand-icon">
          <i data-lucide="cross"></i>
        </div>
        <div>
          <div class="brand-title">MediPoint</div>
          <div class="brand-tagline">Smarter Pharmacy. Better Control.</div>
        </div>
      </div>

      <nav class="sidebar-menu">
        ${menuHtml}
      </nav>

      <div class="p-3 border-top border-secondary text-center" style="font-size: 0.75rem; color: var(--slate-400);">
        <div>MediPoint v1.2.0 (Step 2)</div>
        <div class="text-success fw-semibold mt-1">MySQL + Spring Boot</div>
      </div>
    </aside>
  `;
}
