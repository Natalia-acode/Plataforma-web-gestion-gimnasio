// Cliente HTTP y tipos del contrato (basados en docs/api/openapi.json y los DTO del backend).

export type Rol = 'cliente' | 'recepcionista' | 'entrenador' | 'administrador'

export interface Usuario {
  id: number; nombre: string; apellidos: string; nombreCompleto: string
  email: string; telefono?: string; rol: string; estado: string; fechaRegistro?: string
}
export type User = Omit<Usuario, 'rol'> & { rol: Rol }
export interface LoginResp { token: string; usuario: Usuario }
export interface RegistroResp { pendienteDeAprobacion: boolean; token?: string; usuario: Usuario }
export interface Plan { id: number; nombre: string; descripcion?: string; precio: number; duracionDias: number; activo: boolean }
export interface Clase {
  id: number; nombre: string; diaSemana: string; horaInicio: string; horaFin: string
  cupoMaximo: number; activa: boolean; entrenadorId: number; entrenadorNombre: string
  zonaId: number; zonaNombre: string; reservasConfirmadas: number; cuposDisponibles: number
}
export interface Reserva { id: number; claseId: number; claseNombre: string; diaSemana: string; horaInicio: string; fecha: string; estado: string; posicionEspera?: number }
export interface Suscripcion {
  id: number; planId: number; planNombre: string; precio: number; fechaInicio: string; fechaFin: string
  estado: string; renovacionAutomatica: boolean; fechaCancelacion?: string; motivoCancelacion?: string
}
export interface MetodoPago { id: number; tipo: string; ultimosCuatro: string; titular: string; predeterminado: boolean; activo: boolean }
export interface Pago {
  id: number; planNombre: string; monto: number; fechaPago: string; tipo: string
  estado: string; numeroFactura?: string; metodoResumen: string
}
export interface Aforo { personasDentro: number; capacidadMaxima: number; porcentajeOcupacion: number; nivel: 'BAJO' | 'MEDIO' | 'ALTO'; recomendacion: string }
export interface Celda { diaSemana: string; diaIndice: number; hora: number; ocupacionPromedio: number; ocupacionMaxima: number }
export interface Afluencia { celdas: Celda[]; horaMasConcurrida?: Celda; horaMenosConcurrida?: Celda }
export interface CheckIn { id: number; usuarioId: number; usuarioNombre: string; fechaHoraEntrada: string; fechaHoraSalida?: string; metodo: string }
export interface Turno { id: number; tipo: string; estado: string; posicion: number; personasDelante: number; esperaEstimadaMin: number; esMiTurno: boolean }
export interface Aviso { id: number; tipo: string; mensaje: string; fechaEnvio: string; leida: boolean }
export interface Zona { id: number; nombre: string; tipo: string; capacidadMaxima: number }
export interface Config {
  capacidadMaxima: number; umbralMedio: number; umbralAlto: number; ventanaTurnoMin: number
  tiempoRotacionMin: number; reintentosCobro: number; horarioApertura: string; horarioCierre: string
}
export interface Reporte {
  desde: string; hasta: string; ingresosTotales: number; checkIns: number; suscripcionesNuevas: number
  suscripcionesCanceladas: number; reservasTomadas: number; inasistencias: number; pagosDelPeriodo: Pago[]
}

const BASE = (import.meta.env.VITE_API_URL as string | undefined) ?? 'http://localhost:8080/api'
let token: string | null = localStorage.getItem('gym_token')
let onExpire: () => void = () => {}

export function setToken(t: string | null) {
  token = t
  if (t) localStorage.setItem('gym_token', t)
  else localStorage.removeItem('gym_token')
}
export const setOnExpire = (fn: () => void) => { onExpire = fn }

export class ApiError extends Error {
  constructor(public status: number, message: string) { super(message) }
}

async function request(path: string, method: string, body?: unknown): Promise<Response> {
  let res: Response
  try {
    res = await fetch(BASE + path, {
      method,
      headers: {
        ...(body !== undefined ? { 'Content-Type': 'application/json' } : {}),
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
      body: body !== undefined ? JSON.stringify(body) : undefined,
    })
  } catch {
    throw new ApiError(0, 'No hay conexión con el servidor. Revisa que la API esté encendida en ' + BASE)
  }
  if (!res.ok) {
    let data: { error?: string; campos?: Record<string, string> } = {}
    try { data = await res.json() } catch { /* cuerpo vacío */ }
    if (res.status === 401 && token && !path.startsWith('/auth/')) onExpire()
    const detalle = data.campos ? Object.values(data.campos).join('. ') : data.error
    const porDefecto = res.status === 403 ? 'Tu rol no tiene permiso para esta acción' : `Error ${res.status}`
    throw new ApiError(res.status, detalle || porDefecto)
  }
  return res
}

export async function api<T>(path: string, method = 'GET', body?: unknown): Promise<T> {
  const res = await request(path, method, body)
  const text = await res.text()
  return (text ? JSON.parse(text) : undefined) as T
}

export async function descargar(path: string, nombre: string) {
  const blob = await (await request(path, 'GET')).blob()
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = nombre
  a.click()
  URL.revokeObjectURL(a.href)
}
