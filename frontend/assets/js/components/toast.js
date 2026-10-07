/* =================================================================
   MEDIPOINT TOAST NOTIFICATION SYSTEM
   ================================================================= */

class ToastService {
  constructor() {
    this.container = null;
    this.ensureContainer();
  }

  ensureContainer() {
    if (!this.container) {
      let el = document.getElementById('toast-container');
      if (!el) {
        el = document.createElement('div');
        el.id = 'toast-container';
        el.className = 'toast-container';
        document.body.appendChild(el);
      }
      this.container = el;
    }
  }

  show(title, message, type = 'info', duration = 4000) {
    this.ensureContainer();

    const toast = document.createElement('div');
    toast.className = `toast-item toast-${type}`;

    let iconName = 'info';
    if (type === 'success') iconName = 'check-circle';
    if (type === 'error') iconName = 'alert-triangle';
    if (type === 'warning') iconName = 'alert-circle';

    toast.innerHTML = `
      <div style="color: var(--primary-600); font-weight: bold;">
        <i data-lucide="${iconName}"></i>
      </div>
      <div style="flex: 1;">
        <div style="font-weight: 700; font-size: 0.875rem; color: var(--slate-900); margin-bottom: 2px;">${title}</div>
        <div style="font-size: 0.8125rem; color: var(--slate-600);">${message}</div>
      </div>
    `;

    this.container.appendChild(toast);

    if (window.lucide) {
      window.lucide.createIcons();
    }

    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transform = 'translateX(100%)';
      toast.style.transition = 'all 0.3s ease';
      setTimeout(() => toast.remove(), 300);
    }, duration);
  }

  success(title, message) {
    this.show(title, message, 'success');
  }

  error(title, message) {
    this.show(title, message, 'error');
  }

  warning(title, message) {
    this.show(title, message, 'warning');
  }

  info(title, message) {
    this.show(title, message, 'info');
  }
}

export const toast = new ToastService();
