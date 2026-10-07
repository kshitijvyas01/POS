/* =================================================================
   MEDIPOINT SINGLE PAGE APPLICATION ROUTER
   Architecture: Spring Boot + Spring Security + JWT + MySQL
   ================================================================= */

import { auth } from './auth.js';
import { renderHeader, attachHeaderEvents } from './components/header.js';
import { renderSidebar } from './components/sidebar.js';
import { renderLoginPage, attachLoginPageEvents } from './pages/login.js';
import { renderDashboardPage } from './pages/dashboard.js';
import { renderMedicinesPage, attachMedicinesEvents } from './pages/medicines.js';
import { renderInventoryPage, attachInventoryEvents } from './pages/inventory.js';
import { renderPosPage, attachPosEvents } from './pages/pos.js';
import { renderSalesPage, attachSalesEvents } from './pages/sales.js';
import { renderSettingsPage, attachSettingsEvents } from './pages/settings.js';
import { renderComingSoonPage } from './pages/comingSoon.js';
import { toast } from './components/toast.js';

const PAGE_TITLES = {
  dashboard: 'Dashboard',
  medicines: 'Medicines Directory',
  inventory: 'Inventory Management',
  pos: 'Point of Sale (POS)',
  sales: 'Sales Register',
  customers: 'Customer Directory',
  vendors: 'Vendor Directory',
  purchases: 'Purchases & Orders',
  payments: 'Payments & Collections',
  invoices: 'Invoices & Billing',
  analytics: 'Analytics & Reports',
  employees: 'Employee Management',
  attendance: 'Attendance Management',
  leave: 'Leave Management',
  salary: 'Salary & Payroll',
  settings: 'Store Settings'
};

const OWNER_ONLY_ROUTES = ['settings', 'purchases', 'vendors', 'analytics', 'employees'];

class Router {
  constructor() {
    this.appContainer = document.getElementById('app');
    window.addEventListener('hashchange', () => this.handleRoute());
  }

  init() {
    this.handleRoute();
  }

  async handleRoute() {
    const rawHash = window.location.hash.replace('#', '') || 'dashboard';
    const route = rawHash.split('?')[0];

    // 1. Auth Guard: If not authenticated, always show login
    if (!auth.isAuthenticated()) {
      this.renderLogin();
      return;
    }

    // If authenticated user visits login route, redirect to dashboard
    if (route === 'login') {
      window.location.hash = '#dashboard';
      return;
    }

    // Role Guard: If worker attempts to navigate to owner-only route
    if (OWNER_ONLY_ROUTES.includes(route) && !auth.isOwner()) {
      toast.error('Access Restricted', 'This module is restricted to Store Owner access only.');
      window.location.hash = '#dashboard';
      return;
    }

    // 2. Render App Shell Layout (Header + Sidebar + Content Area)
    const pageTitle = PAGE_TITLES[route] || 'MediPoint System';

    this.appContainer.innerHTML = `
      <div class="app-wrapper">
        ${renderSidebar(route)}
        <div class="main-wrapper">
          ${renderHeader(pageTitle)}
          <main class="main-content" id="main-content">
            <div class="d-flex justify-content-center py-5">
              <div class="spinner-border text-success" role="status">
                <span class="visually-hidden">Loading...</span>
              </div>
            </div>
          </main>
        </div>
      </div>
    `;

    // Attach Header event listeners
    attachHeaderEvents();

    // Re-initialize Lucide Icons for Header/Sidebar
    if (window.lucide) {
      window.lucide.createIcons();
    }

    const contentContainer = document.getElementById('main-content');

    // 3. Render Target Page View
    try {
      if (route === 'dashboard') {
        contentContainer.innerHTML = await renderDashboardPage();
      } else if (route === 'medicines') {
        contentContainer.innerHTML = await renderMedicinesPage();
        attachMedicinesEvents();
      } else if (route === 'inventory') {
        contentContainer.innerHTML = await renderInventoryPage();
        attachInventoryEvents();
      } else if (route === 'pos') {
        contentContainer.innerHTML = await renderPosPage();
        attachPosEvents();
      } else if (route === 'sales') {
        contentContainer.innerHTML = await renderSalesPage();
        attachSalesEvents();
      } else if (route === 'settings') {
        contentContainer.innerHTML = await renderSettingsPage();
        attachSettingsEvents();
      } else {
        contentContainer.innerHTML = renderComingSoonPage(route);
      }
    } catch (e) {
      console.error("Router view rendering error:", e);
      contentContainer.innerHTML = `
        <div class="alert alert-danger">
          <strong>Error loading page:</strong> ${e.message}
        </div>
      `;
    }

    // Re-initialize Lucide Icons for Page Content
    if (window.lucide) {
      window.lucide.createIcons();
    }
  }

  renderLogin() {
    this.appContainer.innerHTML = renderLoginPage();
    attachLoginPageEvents();
    if (window.lucide) {
      window.lucide.createIcons();
    }
  }
}

export const router = new Router();
