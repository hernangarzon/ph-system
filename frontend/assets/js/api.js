// ══════════════════════════════════════════════════════════════
//  api.js — Capa de comunicación con el backend
//  Todas las llamadas HTTP pasan por aquí
// ══════════════════════════════════════════════════════════════

const API_BASE =
  window.location.hostname === "127.0.0.1" ||
  window.location.hostname === "localhost"
    ? "http://localhost:8097/api"
    : "https://ph-system-production-f54c.up.railway.app/api";

// ── Helper principal ──────────────────────────────────────────
async function apiRequest(method, endpoint, body = null) {
  const token = localStorage.getItem("token");

  const headers = {
    "Content-Type": "application/json",
  };

  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
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
      localStorage.removeItem("token");
      localStorage.removeItem("usuario");
      window.location.href = "/login.html";
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
    if (error.message !== "Failed to fetch") {
      console.error(`[API] ${method} ${endpoint}:`, error.message);
    }
    throw error;
  }
}

// ── Métodos HTTP ──────────────────────────────────────────────
const api = {
  get: (endpoint) => apiRequest("GET", endpoint),
  post: (endpoint, body) => apiRequest("POST", endpoint, body),
  put: (endpoint, body) => apiRequest("PUT", endpoint, body),
  patch: (endpoint, body) => apiRequest("PATCH", endpoint, body),
  delete: (endpoint) => apiRequest("DELETE", endpoint),
};

// ── Endpoints específicos ─────────────────────────────────────

// Auth
const AuthAPI = {
  login: (email, password) => api.post("/auth/login", { email, password }),
  registro: (datos) => api.post("/auth/registro", datos),
};

// Solicitudes
const SolicitudAPI = {
  crear: (datos) => api.post("/solicitudes", datos),
  listarTodas: (page = 0) => api.get(`/solicitudes?page=${page}`),
  listarResidente: (id, page = 0) =>
    api.get(`/solicitudes/residente/${id}?page=${page}`),
  sinAsignar: () => api.get("/solicitudes/sin-asignar"),
  buscar: (numero) => api.get(`/solicitudes/${numero}`),
  asignar: (id, personalId) =>
    api.patch(`/solicitudes/${id}/asignar`, { personalId }),
  cambiarEstado: (id, estado) =>
    api.patch(`/solicitudes/${id}/estado`, { estado }),
  conteo: (estado) => api.get(`/solicitudes/conteo/${estado}`),
};

// ── Actualizar sidebar con datos del usuario logueado ──
function actualizarSidebar() {
  const usuario = Auth.getUsuario();
  if (!usuario) return;

  const nombre = usuario.nombre || usuario.email;
  const iniciales = nombre
    .split(" ")
    .map((w) => w[0])
    .join("")
    .substring(0, 2)
    .toUpperCase();
  const rol = {
    ADMIN: "Administrador",
    RESIDENTE: "Residente",
    PORTERIA: "Portero · Turno día",
  };

  // Avatar — texto
  document.querySelectorAll(".avatar").forEach((el) => {
    if (el.textContent.trim().length <= 3) el.textContent = iniciales;
  });

  // Nombre en sidebar
  const userInfoNombre = document.querySelector(".user-info strong");
  if (userInfoNombre) userInfoNombre.textContent = nombre;

  // Rol/subtítulo en sidebar
  const userInfoRol = document.querySelector(".user-info span");
  if (userInfoRol && usuario.rol !== "PORTERIA") {
    userInfoRol.textContent = rol[usuario.rol] || usuario.rol;
  }
}
