const Auth = {
  guardarSesion(token) {
    const payload = JSON.parse(decodeURIComponent(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')).split('').map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)).join('')));
    const usuario = {
      email: payload.sub,
      rol: payload.rol,
      id: payload.id,
      nombre: payload.nombre,
      exp: payload.exp,
    };
    localStorage.setItem("token", token);
    localStorage.setItem("usuario", JSON.stringify(usuario));
    return usuario;
  },

  getUsuario() {
    const data = localStorage.getItem("usuario");
    if (!data) return null;
    try {
      return JSON.parse(data);
    } catch {
      return null;
    }
  },

  getToken() {
    return localStorage.getItem("token");
  },

  estaLogueado() {
    const token = this.getToken();
    if (!token) return false;

    try {
      const payload = JSON.parse(decodeURIComponent(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')).split('').map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)).join('')));
      // Verificar si el token ha expirado
      return payload.exp * 1000 > Date.now();
    } catch {
      return false;
    }
  },

  tieneRol(rol) {
    const usuario = this.getUsuario();
    return usuario?.rol === rol;
  },

  // Calcula la ruta al login desde cualquier página
  _loginUrl() {
    const path = window.location.pathname;
    const depth = path.split("/").filter(Boolean).length - 1;

    // Si estamos en la raíz o en una subcarpeta
    if (depth <= 0 || path.includes("login.html")) {
      return "login.html";
    }

    return "../".repeat(depth) + "login.html";
  },

  proteger(rolRequerido = null) {
    // Si no está logueado, redirigir al login
    if (!this.estaLogueado()) {
      window.location.href = this._loginUrl();
      return false;
    }

    // Si requiere un rol específico y no lo tiene, redirigir
    if (rolRequerido && !this.tieneRol(rolRequerido)) {
      this.redirigirPorRol();
      return false;
    }

    return true;
  },

  redirigirPorRol() {
    const usuario = this.getUsuario();
    if (!usuario) {
      window.location.href = this._loginUrl();
      return;
    }

    const path = window.location.pathname;
    const depth = path.split("/").filter(Boolean).length - 1;
    const base = depth <= 0 ? "" : "../".repeat(depth);

    const rutas = {
      ADMIN: base + "views/admin/dashboard.html",
      RESIDENTE: base + "views/residente/dashboard.html",
      PORTERIA: base + "views/porteria/dashboard.html",
    };

    const ruta = rutas[usuario.rol];
    if (ruta) {
      window.location.href = ruta;
    } else {
      console.error("Rol no reconocido:", usuario.rol);
      window.location.href = this._loginUrl();
    }
  },

  logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("usuario");
    window.location.href = this._loginUrl();
  },
};
