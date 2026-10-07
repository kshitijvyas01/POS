/* =================================================================
   MEDIPOINT AUTHENTICATION & ROLE-BASED ACCESS CONTROL SERVICE
   Architecture: Spring Boot + Spring Security + JWT + MySQL
   No fake tokens or mock logins.
   ================================================================= */

import { api } from './api.js';

class AuthService {
  constructor() {
    this.currentUser = this.loadUserFromStorage();
  }

  loadUserFromStorage() {
    const userJson = localStorage.getItem('medipoint_user');
    if (userJson) {
      try {
        return JSON.parse(userJson);
      } catch (e) {
        localStorage.removeItem('medipoint_user');
      }
    }
    return null;
  }

  isAuthenticated() {
    const token = localStorage.getItem('medipoint_token');
    return !!token && !!this.currentUser;
  }

  getToken() {
    return localStorage.getItem('medipoint_token');
  }

  getUser() {
    return this.currentUser;
  }

  getRole() {
    return this.currentUser?.role || 'GUEST';
  }

  isOwner() {
    return this.getRole() === 'OWNER';
  }

  isWorker() {
    return this.getRole() === 'WORKER';
  }

  hasPermission(permissionCode) {
    if (!this.currentUser || !this.currentUser.permissions) return false;
    return this.currentUser.permissions.includes(permissionCode);
  }

  async login(email, password) {
    const cleanEmail = email.trim();
    const cleanPassword = password.trim();

    if (!cleanEmail || !cleanPassword) {
      throw new Error('Please enter both email and password.');
    }

    // Call Spring Boot REST API for genuine MySQL BCrypt authentication
    const result = await api.login(cleanEmail, cleanPassword);

    if (result && result.success && result.data) {
      const authData = result.data;

      localStorage.setItem('medipoint_token', authData.token);
      const userObj = {
        id: authData.id,
        email: authData.email,
        displayName: authData.fullName,
        role: authData.role,
        status: authData.status,
        permissions: authData.permissions || []
      };

      localStorage.setItem('medipoint_user', JSON.stringify(userObj));
      this.currentUser = userObj;
      return { success: true, user: userObj, message: result.message };
    }

    throw new Error(result?.message || 'Authentication failed. Please verify credentials.');
  }

  async verifySession() {
    if (!this.isAuthenticated()) {
      return false;
    }
    try {
      const res = await api.verifySession();
      if (res && res.success && res.data) {
        // Refresh local user profile details from MySQL
        const u = res.data;
        this.currentUser = {
          ...this.currentUser,
          id: u.id,
          email: u.email,
          displayName: u.fullName,
          role: u.role,
          status: u.status
        };
        localStorage.setItem('medipoint_user', JSON.stringify(this.currentUser));
        return true;
      }
      return false;
    } catch (err) {
      this.clearSession();
      return false;
    }
  }

  async logout() {
    try {
      if (this.isAuthenticated()) {
        await api.logout().catch(() => {});
      }
    } finally {
      this.clearSession();
      window.location.hash = '#login';
    }
  }

  clearSession() {
    localStorage.removeItem('medipoint_token');
    localStorage.removeItem('medipoint_user');
    this.currentUser = null;
  }
}

export const auth = new AuthService();
