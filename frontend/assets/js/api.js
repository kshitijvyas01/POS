/* =================================================================
   MEDIPOINT CENTRALIZED REST API SERVICE LAYER
   Architecture: Spring Boot + Spring Security + JWT + MySQL
   ================================================================= */

const API_BASE_URL = window.MEDIPOINT_API_BASE_URL || 'http://localhost:8080/api/v1';

class ApiService {
  constructor() {
    this.baseUrl = API_BASE_URL;
  }

  getAuthHeaders() {
    const token = localStorage.getItem('medipoint_token');
    const headers = {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    };

    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
    return headers;
  }

  async request(endpoint, options = {}) {
    const url = `${this.baseUrl}${endpoint}`;
    const config = {
      headers: this.getAuthHeaders(),
      ...options
    };

    try {
      const response = await fetch(url, config);
      const data = await response.json().catch(() => null);

      if (!response.ok) {
        // If 401 Unauthorized and not already on login page, clear token and redirect
        if (response.status === 401 && !endpoint.includes('/auth/login')) {
          localStorage.removeItem('medipoint_token');
          localStorage.removeItem('medipoint_user');
          if (window.location.hash !== '#login') {
            window.location.hash = '#login';
          }
        }

        const errorMessage = data?.message || `Server returned error status ${response.status}`;
        const error = new Error(errorMessage);
        error.status = response.status;
        error.errorCode = data?.errorCode || 'API_ERROR';
        throw error;
      }

      return data;
    } catch (error) {
      if (error.name === 'TypeError' && error.message.includes('fetch')) {
        throw new Error('Cannot connect to MediPoint backend server. Please verify Spring Boot is running on http://localhost:8080.');
      }
      throw error;
    }
  }

  // Authentication Endpoints
  async login(email, password) {
    return this.request('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password })
    });
  }

  async register(registrationData) {
    return this.request('/auth/register', {
      method: 'POST',
      body: JSON.stringify(registrationData)
    });
  }

  async verifySession() {
    return this.request('/auth/me', { method: 'GET' });
  }

  async logout() {
    return this.request('/auth/logout', { method: 'POST' });
  }

  // Dashboard KPI Summary Endpoint
  async getDashboardSummary() {
    return this.request('/dashboard/summary', { method: 'GET' });
  }

  // Settings Endpoints (Owner-Only)
  async getSettings() {
    return this.request('/settings', { method: 'GET' });
  }

  async updateSettings(settingsData) {
    return this.request('/settings', {
      method: 'PUT',
      body: JSON.stringify(settingsData)
    });
  }

  // User Profile
  async getUserProfile() {
    return this.request('/user/profile', { method: 'GET' });
  }

  // --- Medicine Management Endpoints ---
  async getMedicines(params = {}) {
    const query = new URLSearchParams(params).toString();
    return this.request(`/medicines${query ? `?${query}` : ''}`, { method: 'GET' });
  }

  async getMedicine(id) {
    return this.request(`/medicines/${id}`, { method: 'GET' });
  }

  async createMedicine(medicineData) {
    return this.request('/medicines', {
      method: 'POST',
      body: JSON.stringify(medicineData)
    });
  }

  async updateMedicine(id, medicineData) {
    return this.request(`/medicines/${id}`, {
      method: 'PUT',
      body: JSON.stringify(medicineData)
    });
  }

  async toggleMedicineStatus(id) {
    return this.request(`/medicines/${id}/status`, { method: 'PATCH' });
  }

  async deleteMedicine(id) {
    return this.request(`/medicines/${id}`, { method: 'DELETE' });
  }

  async searchPosMedicines(query = '') {
    const cleanQuery = encodeURIComponent(query);
    return this.request(`/medicines/pos-search?query=${cleanQuery}`, { method: 'GET' });
  }

  async getLowStockMedicines(page = 0, size = 15) {
    return this.request(`/medicines/low-stock?page=${page}&size=${size}`, { method: 'GET' });
  }

  async getExpiringMedicines(days = 30, page = 0, size = 15) {
    return this.request(`/medicines/expiring?days=${days}&page=${page}&size=${size}`, { method: 'GET' });
  }

  async getExpiredMedicines(page = 0, size = 15) {
    return this.request(`/medicines/expired?page=${page}&size=${size}`, { method: 'GET' });
  }

  // --- Inventory Management Endpoints ---
  async getInventorySummary() {
    return this.request('/inventory/summary', { method: 'GET' });
  }

  async getInventoryItems(params = {}) {
    const query = new URLSearchParams(params).toString();
    return this.request(`/inventory/items${query ? `?${query}` : ''}`, { method: 'GET' });
  }

  async adjustStock(adjustmentData) {
    return this.request('/inventory/adjust', {
      method: 'POST',
      body: JSON.stringify(adjustmentData)
    });
  }

  async getStockMovements(params = {}) {
    const query = new URLSearchParams(params).toString();
    return this.request(`/inventory/movements${query ? `?${query}` : ''}`, { method: 'GET' });
  }

  // --- Sales & POS Endpoints ---
  async checkoutSale(saleData) {
    return this.request('/sales', {
      method: 'POST',
      body: JSON.stringify(saleData)
    });
  }

  async getSales(params = {}) {
    const query = new URLSearchParams(params).toString();
    return this.request(`/sales${query ? `?${query}` : ''}`, { method: 'GET' });
  }

  async getSale(id) {
    return this.request(`/sales/${id}`, { method: 'GET' });
  }

  async getSaleByInvoice(invoiceNumber) {
    return this.request(`/sales/invoice/${encodeURIComponent(invoiceNumber)}`, { method: 'GET' });
  }
}

export const api = new ApiService();
