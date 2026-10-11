export function Tarjeta({ titulo, extra, children, className = '' }) {
  return (
    <section className={`tarjeta ${className}`}>
      {(titulo || extra) && (
        <header className="tarjeta-cabecera">
          <h2>{titulo}</h2>
          {extra}
        </header>
      )}
      {children}
    </section>
  )
}

export function Mensaje({ tipo = 'info', children }) {
  if (!children) {
    return null
  }
  return <div className={`mensaje ${tipo}`}>{children}</div>
}

export function Cargando({ children = 'Cargando…' }) {
  return <div className="cargando">{children}</div>
}

export function Etiqueta({ color = 'gris', children }) {
  return <span className={`etiqueta ${color}`}>{children}</span>
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
