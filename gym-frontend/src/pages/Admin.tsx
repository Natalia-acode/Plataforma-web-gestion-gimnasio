import { useState } from 'react'
import { api, descargar } from '../api'
import type { Clase, Config, Plan, Reporte, Usuario, Zona } from '../api'
import { Field, Notice, Page, Status, Table, Tag, cuando, dia, hora, iso, money, useAction, useForm, useLoad } from '../ui'

const ROLES = ['cliente', 'recepcionista', 'entrenador', 'administrador']
const DIAS = ['lunes', 'martes', 'miercoles', 'jueves', 'viernes', 'sabado', 'domingo']

/* ---------- Reportes ---------- */
export function Reportes() {
  const [desde, setDesde] = useState(iso(new Date(Date.now() - 30 * 864e5)))
  const [hasta, setHasta] = useState(iso(new Date()))
  const q = useLoad(() => api<Reporte>(`/reportes?desde=${desde}&hasta=${hasta}`), [desde, hasta])
  const act = useAction()
  const r = q.data
  const stats: [string, string | number][] = r ? [
    ['Ingresos', money(r.ingresosTotales)], ['Ingresos a la sala', r.checkIns], ['Suscripciones nuevas', r.suscripcionesNuevas],
    ['Suscripciones canceladas', r.suscripcionesCanceladas], ['Reservas de clase', r.reservasTomadas], ['Inasistencias', r.inasistencias]] : []
  return (
    <Page title="Reportes">
      <div className="inline">
        <Field label="Desde"><input type="date" value={desde} max={hasta} onChange={(e) => setDesde(e.target.value)} /></Field>
        <Field label="Hasta"><input type="date" value={hasta} min={desde} onChange={(e) => setHasta(e.target.value)} /></Field>
        <button className="btn ghost" onClick={() => act.run(() => descargar(`/reportes/exportar?desde=${desde}&hasta=${hasta}`, `pagos_${desde}_${hasta}.csv`), 'Archivo descargado.')}>Descargar pagos (CSV)</button>
      </div>
      <Notice m={act.msg} />
      <Status {...q} />
      {r && (
        <>
          <div className="stats" style={{ marginTop: '1rem' }}>{stats.map(([k, v]) => <div className="stat" key={k}><b>{v}</b><span>{k}</span></div>)}</div>
          <h2>Pagos del período</h2>
          <Table head={['Fecha', 'Plan', 'Concepto', 'Monto', 'Estado']} empty="No hubo pagos en este período."
            rows={r.pagosDelPeriodo.map((p) => [cuando(p.fechaPago), p.planNombre, p.tipo.replace('_', ' '), money(p.monto), <Tag key="e" v={p.estado} />])} />
        </>
      )}
    </Page>
  )
}

/* ---------- Usuarios y staff ---------- */
export function Usuarios() {
  const q = useLoad(() => api<Usuario[]>('/usuarios'))
  const solicitudes = useLoad(() => api<Usuario[]>('/usuarios/solicitudes'))
  const act = useAction()
  const { f, on, reset } = useForm({ nombre: '', apellidos: '', email: '', contrasena: '', rol: 'recepcionista' })
  return (
    <Page title="Usuarios">
      <Notice m={act.msg} />
      <h2>Solicitudes de personal</h2>
      <Status {...solicitudes} />
      <Table head={['Nombre', 'Correo', 'Rol', 'Solicitada', '']} empty="No hay solicitudes pendientes."
        rows={(solicitudes.data ?? []).map((u) => [u.nombreCompleto, u.email, String(u.rol).toLowerCase(), cuando(u.fechaRegistro),
          <span className="row" key="a">
            <button className="btn sm" onClick={() => act.run(() => api(`/usuarios/${u.id}/aprobar`, 'PUT'), 'Solicitud aprobada.', () => { solicitudes.reload(); q.reload() })}>Aprobar</button>
            <button className="btn ghost sm" onClick={() => act.run(() => api(`/usuarios/${u.id}/rechazar`, 'PUT'), 'Solicitud rechazada.', () => { solicitudes.reload(); q.reload() })}>Rechazar</button>
          </span>])} />
      <h2>Todas las cuentas</h2>
      <Status {...q} />
      <Table head={['Nombre', 'Correo', 'Rol', 'Estado', '']} empty="No hay usuarios."
        rows={(q.data ?? []).map((u) => [u.nombreCompleto, u.email,
          <select key="r" aria-label={`Rol de ${u.nombreCompleto}`} value={String(u.rol).toLowerCase()} onChange={(e) => act.run(() => api(`/usuarios/${u.id}/rol?rol=${e.target.value}`, 'PUT'), 'Rol actualizado.', q.reload)}>{ROLES.map((r) => <option key={r}>{r}</option>)}</select>,
          <Tag key="e" v={u.estado} />,
          u.estado === 'activo'
            ? <button key="a" className="btn ghost sm" onClick={() => act.run(() => api(`/usuarios/${u.id}/suspender`, 'PUT'), 'Usuario suspendido.', q.reload)}>Suspender</button>
            : u.estado === 'pendiente'
              ? <span key="a" className="muted">En bandeja</span>
              : <button key="a" className="btn ghost sm" onClick={() => act.run(() => api(`/usuarios/${u.id}/reactivar`, 'PUT'), 'Usuario reactivado.', () => { q.reload(); solicitudes.reload() })}>Reactivar</button>])} />
      <h2>Crear personal</h2>
      <form className="form" onSubmit={(e) => { e.preventDefault(); act.run(() => api('/auth/staff', 'POST', f), 'Cuenta creada.', () => { reset(); q.reload() }) }}>
        <Field label="Nombre"><input required value={f.nombre} onChange={on('nombre')} /></Field>
        <Field label="Apellidos"><input required value={f.apellidos} onChange={on('apellidos')} /></Field>
        <Field label="Correo"><input type="email" required value={f.email} onChange={on('email')} /></Field>
        <Field label="Contraseña (mínimo 8 caracteres)"><input type="password" required minLength={8} value={f.contrasena} onChange={on('contrasena')} /></Field>
        <Field label="Rol"><select value={f.rol} onChange={on('rol')}>{ROLES.slice(1).map((r) => <option key={r}>{r}</option>)}</select></Field>
        <button className="btn" disabled={act.busy}>Crear cuenta</button>
      </form>
      <p className="muted">Las cuentas creadas aquí nacen activas; las que llegan del registro público esperan aprobación en la bandeja.</p>
    </Page>
  )
}

/* ---------- Planes ---------- */
export function PlanesAdmin() {
  const q = useLoad(() => api<Plan[]>('/planes/todos'))
  const act = useAction()
  const { f, on, reset } = useForm({ nombre: '', descripcion: '', precio: '', duracionDias: '30' })
  const cuerpo = (p: Plan, activo: boolean) => ({ nombre: p.nombre, descripcion: p.descripcion, precio: p.precio, duracionDias: p.duracionDias, activo })
  return (
    <Page title="Planes">
      <Notice m={act.msg} />
      <Status {...q} />
      <Table head={['Plan', 'Precio', 'Duración', 'Estado', '']} empty="No hay planes."
        rows={(q.data ?? []).map((p) => [p.nombre, money(p.precio), `${p.duracionDias} días`, <Tag key="e" v={p.activo ? 'activo' : 'suspendido'} />,
          p.activo
            ? <button key="a" className="btn ghost sm" onClick={() => act.run(() => api(`/planes/${p.id}`, 'DELETE'), 'Plan desactivado.', q.reload)}>Desactivar</button>
            : <button key="a" className="btn ghost sm" onClick={() => act.run(() => api(`/planes/${p.id}`, 'PUT', cuerpo(p, true)), 'Plan activado.', q.reload)}>Activar</button>])} />
      <h2>Nuevo plan</h2>
      <form className="form" onSubmit={(e) => { e.preventDefault(); act.run(() => api('/planes', 'POST', { nombre: f.nombre, descripcion: f.descripcion, precio: Number(f.precio), duracionDias: Number(f.duracionDias) }), 'Plan creado.', () => { reset(); q.reload() }) }}>
        <Field label="Nombre"><input required maxLength={60} value={f.nombre} onChange={on('nombre')} /></Field>
        <Field label="Descripción"><input maxLength={255} value={f.descripcion} onChange={on('descripcion')} /></Field>
        <Field label="Precio"><input type="number" min="0.01" step="0.01" required value={f.precio} onChange={on('precio')} /></Field>
        <Field label="Duración en días"><input type="number" min="1" required value={f.duracionDias} onChange={on('duracionDias')} /></Field>
        <button className="btn" disabled={act.busy}>Crear plan</button>
      </form>
    </Page>
  )
}

/* ---------- Clases ---------- */
export function ClasesAdmin() {
  const q = useLoad(() => api<Clase[]>('/clases'))
  const zonas = useLoad(() => api<Zona[]>('/aforo/zonas'))
  const users = useLoad(() => api<Usuario[]>('/usuarios'))
  const act = useAction()
  const { f, on, reset } = useForm({ nombre: '', diaSemana: 'lunes', horaInicio: '', horaFin: '', cupoMaximo: '20', idEntrenador: '', idZona: '' })
  const entrenadores = (users.data ?? []).filter((u) => String(u.rol).toLowerCase() === 'entrenador')
  return (
    <Page title="Clases">
      <Notice m={act.msg} />
      <Status {...q} />
      <Table head={['Clase', 'Horario', 'Entrenador', 'Zona', 'Cupo', '']} empty="No hay clases."
        rows={(q.data ?? []).filter((c) => c.activa).map((c) => [c.nombre, <span key="h" style={{ textTransform: 'capitalize' }}>{c.diaSemana} {hora(c.horaInicio)} a {hora(c.horaFin)}</span>, c.entrenadorNombre, c.zonaNombre, c.cupoMaximo,
          <button key="d" className="btn ghost sm" onClick={() => act.run(() => api(`/clases/${c.id}`, 'DELETE'), 'Clase desactivada.', q.reload)}>Desactivar</button>])} />
      <h2>Nueva clase</h2>
      <form className="form" onSubmit={(e) => { e.preventDefault(); act.run(() => api('/clases', 'POST', { ...f, cupoMaximo: Number(f.cupoMaximo), idEntrenador: Number(f.idEntrenador), idZona: Number(f.idZona) }), 'Clase creada.', () => { reset(); q.reload() }) }}>
        <Field label="Nombre"><input required value={f.nombre} onChange={on('nombre')} /></Field>
        <Field label="Día"><select value={f.diaSemana} onChange={on('diaSemana')}>{DIAS.map((d) => <option key={d}>{d}</option>)}</select></Field>
        <div className="row">
          <Field label="Inicio"><input type="time" required value={f.horaInicio} onChange={on('horaInicio')} /></Field>
          <Field label="Fin"><input type="time" required value={f.horaFin} onChange={on('horaFin')} /></Field>
          <Field label="Cupo"><input type="number" min={1} required value={f.cupoMaximo} onChange={on('cupoMaximo')} /></Field>
        </div>
        <Field label="Entrenador"><select required value={f.idEntrenador} onChange={on('idEntrenador')}><option value="">Elige un entrenador</option>{entrenadores.map((u) => <option key={u.id} value={u.id}>{u.nombreCompleto}</option>)}</select></Field>
        <Field label="Zona"><select required value={f.idZona} onChange={on('idZona')}><option value="">Elige una zona</option>{zonas.data?.map((z) => <option key={z.id} value={z.id}>{z.nombre}</option>)}</select></Field>
        <button className="btn" disabled={act.busy}>Crear clase</button>
      </form>
    </Page>
  )
}

/* ---------- Configuracion de aforo ---------- */
const CAMPOS: [keyof Config, string][] = [
  ['capacidadMaxima', 'Capacidad máxima (personas)'], ['umbralMedio', 'Ocupación media desde (personas)'], ['umbralAlto', 'Ocupación alta desde (personas)'],
  ['ventanaTurnoMin', 'Minutos para presentarse tras ser llamado'], ['tiempoRotacionMin', 'Tiempo de rotación (minutos)'], ['reintentosCobro', 'Reintentos de cobro'],
]
export function Configuracion() {
  const q = useLoad(() => api<Config>('/aforo/configuracion'))
  const act = useAction()
  const [edit, setEdit] = useState<Record<string, string>>({})
  const valor = (k: keyof Config) => edit[k] ?? String(q.data?.[k] ?? '')
  return (
    <Page title="Configuracion de aforo">
      <Status {...q} />
      {q.data && (
        <form className="form" onSubmit={(e) => { e.preventDefault(); act.run(() => api('/aforo/configuracion', 'PUT', Object.fromEntries(CAMPOS.map(([k]) => [k, Number(valor(k))]))), 'Configuracion guardada.', q.reload) }}>
          {CAMPOS.map(([k, label]) => <Field key={k} label={label}><input type="number" min={0} required value={valor(k)} onChange={(e) => setEdit({ ...edit, [k]: e.target.value })} /></Field>)}
          <p className="muted">Horario del gimnasio: {hora(q.data.horarioApertura)} a {hora(q.data.horarioCierre)}.</p>
          <Notice m={act.msg} />
          <button className="btn" disabled={act.busy}>Guardar cambios</button>
        </form>
      )}
    </Page>
  )
}
