import { useState } from 'react'
import { api } from '../api'
import type { Aforo, CheckIn, Clase, Reserva, Turno } from '../api'
import { useAuth } from '../auth'
import { AforoCard, Field, Notice, Page, Status, Table, Tag, cuando, hora, iso, useAction, useForm, useLoad } from '../ui'

/* ---------- Recepcion: aforo, ingresos y cola ---------- */
export function Recepcion() {
  const aforo = useLoad(() => api<Aforo>('/aforo/ocupacion'), [], 15000)
  const dentro = useLoad(() => api<CheckIn[]>('/checkin/dentro'), [], 15000)
  const cola = useLoad(() => api<Turno[]>('/cola/gestion/cola'), [], 15000)
  const act = useAction()
  const { f, on, reset } = useForm({ id: '' })
  const refrescar = () => { aforo.reload(); dentro.reload(); cola.reload() }
  return (
    <Page title="Recepcion">
      {aforo.data && <AforoCard a={aforo.data} />}
      <Notice m={act.msg} />
      <h2>Registrar ingreso manual</h2>
      {/* El backend no permite al recepcionista buscar clientes; por ahora se ingresa el número de usuario. */}
      <form className="inline" onSubmit={(e) => { e.preventDefault(); act.run(() => api('/checkin/manual', 'POST', { idUsuario: Number(f.id) }), 'Ingreso registrado.', () => { reset(); refrescar() }) }}>
        <Field label="Número de usuario"><input type="number" min={1} required value={f.id} onChange={on('id')} /></Field>
        <button className="btn" disabled={act.busy}>Registrar ingreso</button>
      </form>
      <h2>Dentro ahora</h2>
      <Status {...dentro} />
      <Table head={['Socio', 'Entrada', 'Método', '']} empty="No hay nadie dentro."
        rows={(dentro.data ?? []).map((c) => [c.usuarioNombre, cuando(c.fechaHoraEntrada), c.metodo,
          <button key="s" className="btn ghost sm" onClick={() => act.run(() => api(`/checkin/${c.usuarioId}/salida`, 'POST'), 'Salida registrada.', refrescar)}>Registrar salida</button>])} />
      <h2>Cola virtual</h2>
      <div className="row" style={{ marginBottom: '.8rem' }}>
        <button className="btn" disabled={act.busy} onClick={() => act.run(() => api('/cola/gestion/llamar-siguiente', 'POST'), 'Turno llamado.', refrescar)}>Llamar al siguiente</button>
      </div>
      <Status {...cola} />
      <Table head={['Puesto', 'Tipo', 'Estado', 'Espera', '']} empty="La cola está vacía."
        rows={(cola.data ?? []).map((t) => [t.posicion, t.tipo, <Tag key="e" v={t.estado} />, `${t.esperaEstimadaMin} min`,
          t.estado === 'llamado' ? <button key="a" className="btn ghost sm" onClick={() => act.run(() => api(`/cola/gestion/${t.id}/atendido`, 'POST'), 'Turno atendido.', refrescar)}>Marcar atendido</button> : ''])} />
    </Page>
  )
}

/* ---------- Entrenador: clases y asistencia ---------- */
function Asistencia({ clase }: { clase: Clase }) {
  const [fecha, setFecha] = useState(iso(new Date()))
  const q = useLoad(() => api<Reserva[]>(`/clases/${clase.id}/asistentes?fecha=${fecha}`), [clase.id, fecha])
  const act = useAction()
  const marcar = (id: number, asistio: boolean) =>
    act.run(() => api(`/clases/reservas/${id}/asistencia?asistio=${asistio}`, 'POST'), asistio ? 'Asistencia marcada.' : 'Inasistencia marcada.', q.reload)
  return (
    <div className="panel">
      <h3>{clase.nombre}</h3>
      <Field label="Fecha de la clase"><input type="date" value={fecha} onChange={(e) => setFecha(e.target.value)} /></Field>
      <Notice m={act.msg} />
      <Status {...q} />
      {/* El backend no devuelve el nombre del socio en esta lista, solo el número de reserva. */}
      <Table head={['Reserva', 'Estado', '']} empty="Nadie reservó para esta fecha."
        rows={(q.data ?? []).map((r) => [`n.º ${r.id}`, <Tag key="e" v={r.estado} />,
          r.estado === 'confirmada' ? <span key="b" className="row"><button className="btn sm" onClick={() => marcar(r.id, true)}>Asistió</button><button className="btn ghost sm" onClick={() => marcar(r.id, false)}>No asistió</button></span> : ''])} />
    </div>
  )
}

export function MisClases() {
  const { user } = useAuth()
  const q = useLoad(() => api<Clase[]>('/clases'))
  const mias = (q.data ?? []).filter((c) => c.entrenadorId === user!.id && c.activa)
  const [abierta, setAbierta] = useState<number | null>(null)
  return (
    <Page title="Mis clases">
      <Status {...q} />
      {!q.loading && !mias.length && <p className="empty">Todavía no tienes clases asignadas.</p>}
      <div className="grid">
        {mias.map((c) => (
          <article className="panel" key={c.id}>
            <h3>{c.nombre}</h3>
            <p style={{ textTransform: 'capitalize' }}>{c.diaSemana} {hora(c.horaInicio)} a {hora(c.horaFin)} en {c.zonaNombre}</p>
            <p className="muted">Cupo {c.cupoMaximo}.</p>
            <button className="btn" onClick={() => setAbierta(abierta === c.id ? null : c.id)}>{abierta === c.id ? 'Cerrar lista' : 'Pasar lista'}</button>
          </article>
        ))}
      </div>
      {mias.filter((c) => c.id === abierta).map((c) => <Asistencia key={c.id} clase={c} />)}
    </Page>
  )
}
