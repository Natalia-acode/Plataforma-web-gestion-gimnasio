import { Alert, Badge, Card, Spinner } from 'react-bootstrap'

export function Tarjeta({ titulo, extra, children, className = '' }) {
  return (
    <Card className={`shadow-sm ${className}`}>
      {(titulo || extra) && (
        <Card.Header className="bg-white d-flex flex-wrap align-items-center justify-content-between gap-2">
          <h6 className="mb-0 fw-semibold">{titulo}</h6>
          {extra}
        </Card.Header>
      )}
      <Card.Body>{children}</Card.Body>
    </Card>
  )
}

const VARIANTES_MENSAJE = {
  info: 'info',
  error: 'danger',
  exito: 'success',
  advertencia: 'warning',
  aviso: 'warning',
}

export function Mensaje({ tipo = 'info', children }) {
  if (!children) {
    return null
  }
  return (
    <Alert variant={VARIANTES_MENSAJE[tipo] || 'info'} className="py-2">
      {children}
    </Alert>
  )
}

export function Cargando({ children = 'Cargando…' }) {
  return (
    <div className="d-flex align-items-center gap-2 text-secondary py-3">
      <Spinner animation="border" size="sm" role="status" aria-hidden="true" />
      <span>{children}</span>
    </div>
  )
}

const COLORES_ETIQUETA = {
  verde: 'success',
  amarillo: 'warning',
  azul: 'primary',
  rojo: 'danger',
  gris: 'secondary',
}

export function Etiqueta({ color = 'gris', children }) {
  const variante = COLORES_ETIQUETA[color] || 'secondary'
  return (
    <Badge bg={variante} text={variante === 'warning' ? 'dark' : undefined}>
      {children}
    </Badge>
  )
}

export function EncabezadoPagina({ titulo, subtitulo, children }) {
  return (
    <div className="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
      <div>
        <h1 className="h3 mb-1">{titulo}</h1>
        {subtitulo && <p className="text-secondary mb-0">{subtitulo}</p>}
      </div>
      {children && <div className="d-flex flex-wrap gap-2">{children}</div>}
    </div>
  )
}

export function formatearMoneda(valor) {
  if (valor === null || valor === undefined) {
    return '—'
  }
  return `$ ${Number(valor).toFixed(2)}`
}

export function formatearFechaHora(texto) {
  if (!texto) {
    return '—'
  }
  const fecha = new Date(texto)
  return fecha.toLocaleString('es-CO', { dateStyle: 'medium', timeStyle: 'short' })
}

export function hoyIso() {
  const fecha = new Date()
  const mes = String(fecha.getMonth() + 1).padStart(2, '0')
  const dia = String(fecha.getDate()).padStart(2, '0')
  return `${fecha.getFullYear()}-${mes}-${dia}`
}

export function proximosDias(cantidad = 14) {
  const dias = []
  const hoy = new Date()
  for (let i = 0; i < cantidad; i++) {
    const fecha = new Date(hoy)
    fecha.setDate(hoy.getDate() + i)
    dias.push(fecha.toISOString().slice(0, 10))
  }
  return dias
}

const NOMBRES_DIA = ['Domingo', 'Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado']

export function nombreDia(iso) {
  const fecha = new Date(`${iso}T12:00:00`)
  return NOMBRES_DIA[fecha.getDay()]
}

export function etiquetaEstado(estado) {
  const mapa = {
    activa: 'verde',
    aprobado: 'verde',
    confirmada: 'verde',
    asistio: 'verde',
    atendido: 'verde',
    activo: 'verde',
    pendiente: 'amarillo',
    en_espera: 'amarillo',
    esperando: 'amarillo',
    llamado: 'azul',
    cancelada: 'rojo',
    cancelado: 'rojo',
    vencida: 'rojo',
    rechazado: 'rojo',
    suspendida: 'rojo',
    suspendido: 'rojo',
    no_asistio: 'rojo',
    expirado: 'rojo',
    reembolsado: 'azul',
  }
  return mapa[estado] || 'gris'
}
