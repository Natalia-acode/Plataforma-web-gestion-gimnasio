import { api } from './client.js'

export const authApi = {
  login: (email, contrasena) => api.post('/auth/login', { email, contrasena }),
  registro: (datos) => api.post('/auth/registro', datos),
  perfil: () => api.get('/auth/perfil'),
}

export const planesApi = {
  listar: () => api.get('/planes'),
  listarTodos: () => api.get('/planes/todos'),
  crear: (datos) => api.post('/planes', datos),
  actualizar: (id, datos) => api.put(`/planes/${id}`, datos),
  desactivar: (id) => api.del(`/planes/${id}`),
}

export const suscripcionesApi = {
  contratar: (datos) => api.post('/suscripciones', datos),
  mias: () => api.get('/suscripciones/mis'),
  activa: () => api.get('/suscripciones/activa'),
  cambiarPlan: (id, idPlan) => api.post(`/suscripciones/${id}/cambiar-plan`, { idPlan }),
  cancelar: (id, motivo) => api.post(`/suscripciones/${id}/cancelar`, { motivo }),
  renovacion: (id, activa) => api.put(`/suscripciones/${id}/renovacion`, { activa }),
}

export const pagosApi = {
  metodos: () => api.get('/pagos/metodos'),
  registrarMetodo: (datos) => api.post('/pagos/metodos', datos),
  predeterminar: (id) => api.put(`/pagos/metodos/${id}/predeterminado`),
  eliminarMetodo: (id) => api.del(`/pagos/metodos/${id}`),
  historial: () => api.get('/pagos/historial'),
  factura: (id) => api.get(`/pagos/${id}/factura`),
}

export const aforoApi = {
  ocupacion: () => api.get('/aforo/ocupacion'),
  afluencia: (horaDesde = 5, horaHasta = 22) => api.get('/aforo/afluencia', { horaDesde, horaHasta }),
  zonas: () => api.get('/aforo/zonas'),
  configuracion: () => api.get('/aforo/configuracion'),
  actualizarConfiguracion: (datos) => api.put('/aforo/configuracion', datos),
}

export const checkinApi = {
  entradaQr: () => api.post('/checkin/qr'),
  entradaManual: (idUsuario) => api.post('/checkin/manual', { idUsuario }),
  salida: (usuarioId) => api.post(`/checkin/${usuarioId}/salida`),
  dentro: () => api.get('/checkin/dentro'),
}

export const clasesApi = {
  listar: (dia) => api.get('/clases', { dia }),
  obtener: (id) => api.get(`/clases/${id}`),
  crear: (datos) => api.post('/clases', datos),
  reservar: (id, fecha) => api.post(`/clases/${id}/reservas`, { idClase: id, fecha }),
  misReservas: () => api.get('/clases/reservas/mias'),
  cancelarReserva: (reservaId) => api.del(`/clases/reservas/${reservaId}`),
  asistentes: (id, fecha) => api.get(`/clases/${id}/asistentes`, { fecha }),
  marcarAsistencia: (reservaId, asistio) =>
    api.post(`/clases/reservas/${reservaId}/asistencia?asistio=${asistio}`),
}

export const colaApi = {
  solicitar: (tipo) => api.post('/cola/turno', { tipo }),
  miTurno: () => api.get('/cola/turno/mi'),
  cancelar: () => api.del('/cola/turno'),
  colaCompleta: () => api.get('/cola/gestion/cola'),
  llamarSiguiente: () => api.post('/cola/gestion/llamar-siguiente'),
  atendido: (turnoId) => api.post(`/cola/gestion/${turnoId}/atendido`),
}

export const usuariosApi = {
  listar: () => api.get('/usuarios'),
  suspender: (id) => api.put(`/usuarios/${id}/suspender`),
  reactivar: (id) => api.put(`/usuarios/${id}/reactivar`),
  asignarRol: (id, rol) => api.put(`/usuarios/${id}/rol?rol=${rol}`),
  crearStaff: (datos) => api.post('/auth/staff', datos),
  notificaciones: () => api.get('/usuarios/notificaciones/mias'),
  marcarLeida: (id) => api.put(`/usuarios/notificaciones/${id}/leida`),
}

export const reportesApi = {
  generar: (desde, hasta) => api.get('/reportes', { desde, hasta }),
  urlExportar: (desde, hasta) => `/api/reportes/exportar?desde=${desde}&hasta=${hasta}`,
}
