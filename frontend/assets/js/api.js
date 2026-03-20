// ══════════════════════════════════════════════════════════════
//  api.js — Capa de comunicación con el backend
//  Todas las llamadas HTTP pasan por aquí
// ══════════════════════════════════════════════════════════════

const API_BASE = 'http://localhost:8097/api';

// ── Helper principal ──────────────────────────────────────────
async function apiRequest(method, endpoint, body = null) {
  const token = localStorage.getItem('token');

  const headers = {
    'Content-Type': 'application/json',
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const config = {
    method,
    headers,
  };

  if (body) {
    config.body = JSON.stringify(body);
  }

  try {
    const response = await fetch(`${API_BASE}${endpoint}`, config);

    // Token expirado o inválido
    if (response.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('usuario');
      window.location.href = '/login.html';
      return null;
    }

    // Error del servidor
    if (!response.ok) {
      const error = await response.json().catch(() => ({}));
      throw new Error(error.message || `Error ${response.status}`);
    }

    // Respuesta vacía (204 No Content)
    if (response.status === 204) return null;

    return await response.json();

  } catch (error) {
    if (error.message !== 'Failed to fetch') {
      console.error(`[API] ${method} ${endpoint}:`, error.message);
    }
    throw error;
  }
}

// ── Métodos HTTP ──────────────────────────────────────────────
const api = {
  get:    (endpoint)       => apiRequest('GET',    endpoint),
  post:   (endpoint, body) => apiRequest('POST',   endpoint, body),
  put:    (endpoint, body) => apiRequest('PUT',    endpoint, body),
  patch:  (endpoint, body) => apiRequest('PATCH',  endpoint, body),
  delete: (endpoint)       => apiRequest('DELETE', endpoint),
};

// ── Endpoints específicos ─────────────────────────────────────

// Auth
const AuthAPI = {
  login:    (email, password) => api.post('/auth/login', { email, password }),
  registro: (datos)           => api.post('/auth/registro', datos),
};

// Solicitudes
const SolicitudAPI = {
  crear:          (datos)            => api.post('/solicitudes', datos),
  listarTodas:    (page = 0)         => api.get(`/solicitudes?page=${page}`),
  listarResidente:(id, page = 0)     => api.get(`/solicitudes/residente/${id}?page=${page}`),
  sinAsignar:     ()                 => api.get('/solicitudes/sin-asignar'),
  buscar:         (numero)           => api.get(`/solicitudes/${numero}`),
  asignar:        (id, personalId)   => api.patch(`/solicitudes/${id}/asignar`, { personalId }),
  cambiarEstado:  (id, estado)       => api.patch(`/solicitudes/${id}/estado`, { estado }),
  conteo:         (estado)           => api.get(`/solicitudes/conteo/${estado}`),
};