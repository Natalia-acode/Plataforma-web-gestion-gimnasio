import { CSSProperties, useState } from 'react'
import { QRCodeSVG } from 'qrcode.react'
import { Link } from 'react-router-dom'
import { api, ApiError } from '../api'
import type { Afluencia, Aforo, Celda, Clase, MetodoPago, Pago, Plan, Reserva, Suscripcion, Turno, Aviso } from '../api'
import { useAuth } from '../auth'
import { AforoCard, Field, Notice, Page, Status, Table, Tag, cuando, dia, hora, iso, money, useAction, useForm, useLoad } from '../ui'

/* ---------- Membresia: contratar, cambiar, cancelar, renovacion ---------- */
export function Membresia() {
  const sus = useLoad(async () => {
    try { return await api<Suscripcion>('/suscripciones/activa') } catch (e) {
      if ([404, 409].includes((e as ApiError).status)) return null
      throw e
    }
  })
  const planes = useLoad(() => api<Plan[]>('/planes'))
  const metodos = useLoad(() => api<MetodoPago[]>('/pagos/metodos'))
  const act = useAction()
  const { f, on } = useForm({ plan: '', metodo: '', motivo: '', nuevo: '' })
  const [renueva, setRenueva] = useState(false)
  const [cancelando, setCancelando] = useState(false)
  const s = sus.data
  const elegido = planes.data?.find((p) => String(p.id) === f.plan)
  const proximo = elegido ? iso(new Date(Date.now() + elegido.duracionDias * 864e5)) : ''
  const recargar = () => { sus.reload(); metodos.reload() }

  return (
    <Page title="Mi membresía">
      <Status loading={sus.loading} error={sus.error} />
      <Notice m={act.msg} />
      {s && (
        <div className="panel">
          <h3>{s.planNombre} <Tag v={s.estado} /></h3>
          <p>Válida hasta el {dia(s.fechaFin)}. {s.fechaCancelacion ? `Cancelada el ${cuando(s.fechaCancelacion)}; conservas el acceso hasta esa fecha.` : s.renovacionAutomatica ? `Se renovará solo y cobrará ${money(s.precio)}.` : 'No se renovará automáticamente.'}</p>
          {s.estado === 'activa' && !s.fechaCancelacion && (
            <>
              <label className="check">
                <input type="checkbox" checked={s.renovacionAutomatica} disabled={act.busy}
                  onChange={(e) => act.run(() => api(`/suscripciones/${s.id}/renovacion`, 'PUT', { activa: e.target.checked }), e.target.checked ? 'Renovación automática activada.' : 'Renovación automática desactivada. No habrá más cobros.', sus.reload)} />
                <span>Renovar automáticamente</span>
              </label>
              <form className="inline" style={{ marginTop: '1rem' }} onSubmit={(e) => { e.preventDefault(); act.run(() => api(`/suscripciones/${s.id}/cambiar-plan`, 'POST', { idPlan: Number(f.nuevo) }), 'Plan cambiado.', sus.reload) }}>
                <Field label="Cambiar a otro plan">
                  <select required value={f.nuevo} onChange={on('nuevo')}>
                    <option value="">Elige un plan</option>
                    {planes.data?.filter((p) => p.id !== s.planId).map((p) => <option key={p.id} value={p.id}>{p.nombre} ({money(p.precio)})</option>)}
                  </select>
                </Field>
                <button className="btn ghost" disabled={act.busy}>Cambiar plan</button>
              </form>
              <hr style={{ border: 0, borderTop: '1px solid var(--line)', margin: '1.2rem 0' }} />
              {!cancelando ? <button className="btn danger" onClick={() => setCancelando(true)}>Cancelar suscripción</button> : (
                <form className="form" onSubmit={(e) => { e.preventDefault(); act.run(() => api(`/suscripciones/${s.id}/cancelar`, 'POST', { motivo: f.motivo || undefined }), 'Suscripción cancelada. No se hará ningún cargo más.', () => { setCancelando(false); sus.reload() }) }}>
                  <p>Cancelar no tiene costo y conservas el acceso hasta el {dia(s.fechaFin)}.</p>
                  <Field label="Motivo (opcional)"><input maxLength={255} value={f.motivo} onChange={on('motivo')} /></Field>
                  <div className="row"><button className="btn danger" disabled={act.busy}>Confirmar cancelación</button><button type="button" className="btn ghost" onClick={() => setCancelando(false)}>Volver</button></div>
                </form>
              )}
            </>
          )}
        </div>
      )}
      {sus.data === null && !sus.loading && (
        <div className="panel">
          <h3>Contrata un plan</h3>
          <form className="form" onSubmit={(e) => { e.preventDefault(); act.run(() => api('/suscripciones', 'POST', { idPlan: Number(f.plan), idMetodo: f.metodo ? Number(f.metodo) : undefined, renovacionAutomatica: renueva }), 'Plan contratado.', recargar) }}>
            <Field label="Plan">
              <select required value={f.plan} onChange={on('plan')}>
                <option value="">Elige un plan</option>
                {planes.data?.map((p) => <option key={p.id} value={p.id}>{p.nombre} ({money(p.precio)})</option>)}
              </select>
            </Field>
            <Field label="Método de pago">
              <select required value={f.metodo} onChange={on('metodo')}>
                <option value="">Elige una tarjeta</option>
                {metodos.data?.map((m) => <option key={m.id} value={m.id}>{m.tipo.replace('_', ' ')} terminada en {m.ultimosCuatro}</option>)}
              </select>
            </Field>
            {metodos.data?.length === 0 && <p className="muted">Primero registra una tarjeta en <Link to="/app/pagos">Pagos</Link>.</p>}
            <label className="check">
              <input type="checkbox" checked={renueva} onChange={(e) => setRenueva(e.target.checked)} />
              <span>Quiero que mi plan se renueve automáticamente. Puedo desactivarlo cuando quiera.</span>
            </label>
            {elegido && <p className="panel">Hoy se cobrarán <b>{money(elegido.precio)}</b>. Tu plan vence el <b>{dia(proximo)}</b>{renueva ? ` y ese día se cobrarán otros ${money(elegido.precio)}.` : ' y no se cobrará nada más.'}</p>}
            <button className="btn" disabled={act.busy || !elegido}>Contratar plan</button>
          </form>
        </div>
      )}
    </Page>
  )
}

/* ---------- Pagos: tarjetas, historial y facturas ---------- */
export function Pagos() {
  const metodos = useLoad(() => api<MetodoPago[]>('/pagos/metodos'))
  const hist = useLoad(() => api<Pago[]>('/pagos/historial'))
  const act = useAction()
  const { f, on, reset } = useForm({ tipo: 'tarjeta_credito', numeroTarjeta: '', titular: '' })
  return (
    <Page title="Pagos">
      <Notice m={act.msg} />
      <h2>Mis tarjetas</h2>
      <Status {...metodos} />
      <Table head={['Tarjeta', 'Titular', 'Estado', '']} empty="Aún no tienes tarjetas registradas."
        rows={(metodos.data ?? []).map((m) => [
          `${m.tipo.replace('_', ' ')} terminada en ${m.ultimosCuatro}`, m.titular, m.predeterminado ? 'Predeterminada' : '',
          <span className="row" key={m.id}>
            {!m.predeterminado && <button className="btn ghost sm" onClick={() => act.run(() => api(`/pagos/metodos/${m.id}/predeterminado`, 'PUT'), 'Tarjeta predeterminada actualizada.', metodos.reload)}>Hacer predeterminada</button>}
            <button className="btn ghost sm" onClick={() => act.run(() => api(`/pagos/metodos/${m.id}`, 'DELETE'), 'Tarjeta eliminada.', metodos.reload)}>Eliminar</button>
          </span>])} />
      <h2>Agregar tarjeta</h2>
      <form className="form" onSubmit={(e) => { e.preventDefault(); act.run(() => api('/pagos/metodos', 'POST', { ...f, predeterminado: !metodos.data?.length }), 'Tarjeta agregada.', () => { reset(); metodos.reload() }) }}>
        <Field label="Tipo"><select value={f.tipo} onChange={on('tipo')}><option value="tarjeta_credito">Crédito</option><option value="tarjeta_debito">Débito</option></select></Field>
        <Field label="Número de tarjeta"><input required inputMode="numeric" pattern="\d{13,19}" value={f.numeroTarjeta} onChange={on('numeroTarjeta')} autoComplete="cc-number" /></Field>
        <Field label="Titular"><input required maxLength={120} value={f.titular} onChange={on('titular')} autoComplete="cc-name" /></Field>
        <button className="btn" disabled={act.busy}>Guardar tarjeta</button>
      </form>
      <h2>Historial</h2>
      <Status {...hist} />
      <Table head={['Fecha', 'Plan', 'Concepto', 'Monto', 'Estado', 'Factura']} empty="Todavía no tienes pagos."
        rows={(hist.data ?? []).map((p) => [cuando(p.fechaPago), p.planNombre, p.tipo.replace('_', ' '), money(p.monto), <Tag key="e" v={p.estado} />, p.numeroFactura ?? '—'])} />
    </Page>
  )
}

/* ---------- Aforo y mapa de calor ---------- */
const DIAS = ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom']
function Heatmap({ celdas }: { celdas: Celda[] }) {
  const horas = [...new Set(celdas.map((c) => c.hora))].sort((a, b) => a - b)
  const max = Math.max(1, ...celdas.map((c) => c.ocupacionPromedio))
  return (
    <div className="scroll">
      <table className="heat">
        <thead><tr><th />{horas.map((h) => <th key={h}>{h}h</th>)}</tr></thead>
        <tbody>
          {DIAS.map((n, d) => (
            <tr key={n}>
              <th>{n}</th>
              {horas.map((h) => {
                const c = celdas.find((x) => x.diaIndice === d && x.hora === h)
                return <td key={h} style={{ '--v': c ? c.ocupacionPromedio / max : 0 } as CSSProperties}
                  title={c ? `${n} ${h}:00, promedio de ${Math.round(c.ocupacionPromedio)} personas` : 'Sin datos'} />
              })}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

export function AforoCliente() {
  const aforo = useLoad(() => api<Aforo>('/aforo/ocupacion'), [], 20000)
  const afl = useLoad(() => api<Afluencia>('/aforo/afluencia'))
  const txt = (c?: Celda) => (c ? `${c.diaSemana} a las ${c.hora}:00` : 'sin datos')
  return (
    <Page title="Aforo y afluencia">
      <Status {...aforo} />
      {aforo.data && <AforoCard a={aforo.data} />}
      <h2>¿Cuándo conviene ir?</h2>
      <Status {...afl} />
      {afl.data && (
        <>
          <p>Más tranquilo: <b>{txt(afl.data.horaMenosConcurrida)}</b>. Más lleno: <b>{txt(afl.data.horaMasConcurrida)}</b>. Los cuadros más oscuros son las horas con más gente.</p>
          <Heatmap celdas={afl.data.celdas} />
        </>
      )}
    </Page>
  )
}

/* ---------- Clases y reservas ---------- */
const JS_DIA: Record<string, number> = { domingo: 0, lunes: 1, martes: 2, miercoles: 3, jueves: 4, viernes: 5, sabado: 6 }
// La clase es semanal: se ofrecen solo las próximas fechas que caen en su día.
function proximasFechas(diaSemana: string) {
  const d = new Date()
  d.setDate(d.getDate() + ((JS_DIA[diaSemana] - d.getDay() + 7) % 7))
  return [0, 1, 2, 3].map((i) => { const x = new Date(d); x.setDate(d.getDate() + 7 * i); return iso(x) })
}

function ClaseCard({ c, onReservada }: { c: Clase; onReservada: () => void }) {
  const fechas = proximasFechas(c.diaSemana)
  const [fecha, setFecha] = useState(fechas[0])
  const act = useAction()
  return (
    <article className="panel">
      <h3>{c.nombre}</h3>
      <p style={{ textTransform: 'capitalize' }}>{c.diaSemana} {hora(c.horaInicio)} a {hora(c.horaFin)} en {c.zonaNombre}</p>
      <p className="muted">{c.cuposDisponibles > 0 ? `Quedan ${c.cuposDisponibles} cupos.` : 'Clase llena: puedes entrar a la lista de espera.'}</p>
      <div className="row" style={{ marginTop: '.6rem' }}>
        <select aria-label={`Fecha para ${c.nombre}`} value={fecha} onChange={(e) => setFecha(e.target.value)}>{fechas.map((x) => <option key={x} value={x}>{dia(x)}</option>)}</select>
        <button className="btn" disabled={act.busy} onClick={() => act.run(() => api(`/clases/${c.id}/reservas`, 'POST', { idClase: c.id, fecha }), c.cuposDisponibles > 0 ? 'Reserva confirmada.' : 'Estás en la lista de espera.', onReservada)}>
          {c.cuposDisponibles > 0 ? 'Reservar' : 'Unirme a la espera'}
        </button>
      </div>
      <Notice m={act.msg} />
    </article>
  )
}

export function Reservas() {
  const clases = useLoad(() => api<Clase[]>('/clases'))
  const mias = useLoad(() => api<Reserva[]>('/clases/reservas/mias'))
  const act = useAction()
  const refrescar = () => { clases.reload(); mias.reload() }
  return (
    <Page title="Clases">
      <h2>Mis reservas</h2>
      <Notice m={act.msg} />
      <Status {...mias} />
      <Table head={['Clase', 'Fecha', 'Hora', 'Estado', '']} empty="No tienes reservas."
        rows={(mias.data ?? []).map((r) => [r.claseNombre, dia(r.fecha), hora(r.horaInicio),
          <span key="e"><Tag v={r.estado} />{r.posicionEspera ? ` puesto ${r.posicionEspera}` : ''}</span>,
          ['confirmada', 'en_espera'].includes(r.estado) ? <button key="c" className="btn ghost sm" onClick={() => act.run(() => api(`/clases/reservas/${r.id}`, 'DELETE'), 'Reserva cancelada.', refrescar)}>Cancelar</button> : ''])} />
      <h2>Reservar</h2>
      <Status {...clases} />
      <div className="grid">{(clases.data ?? []).filter((c) => c.activa).map((c) => <ClaseCard key={c.id} c={c} onReservada={refrescar} />)}</div>
    </Page>
  )
}

/* ---------- Cola virtual ---------- */
export function Cola() {
  const q = useLoad(() => api<Turno | undefined>('/cola/turno/mi'), [], 15000)
  const act = useAction()
  const t = q.data
  return (
    <Page title="Cola virtual">
      <Notice m={act.msg} />
      <Status {...q} />
      {t ? (
        <div className={`panel ${t.esMiTurno ? 'llamado' : ''}`}>
          {t.esMiTurno ? <h3>Es tu turno. Pasa a recepción ahora.</h3> : <h3>Estás en la cola de {t.tipo}</h3>}
          <p className="turno">{t.posicion}</p>
          <p>Tienes {t.personasDelante} personas delante. Espera estimada: {t.esperaEstimadaMin} minutos.</p>
          <p className="muted">Te avisaremos cuando se acerque tu turno; esta página se actualiza sola.</p>
          <button className="btn danger" disabled={act.busy} onClick={() => act.run(() => api('/cola/turno', 'DELETE'), 'Saliste de la cola.', q.reload)}>Salir de la cola</button>
        </div>
      ) : !q.loading && (
        <div className="panel">
          <p>Si el gimnasio está lleno, pide un turno y vuelve cuando te llamemos.</p>
          <div className="row" style={{ marginTop: '.8rem' }}>
            {(['ingreso', 'clase'] as const).map((tipo) => (
              <button key={tipo} className="btn" disabled={act.busy} onClick={() => act.run(() => api('/cola/turno', 'POST', { tipo }), 'Turno solicitado.', q.reload)}>
                Pedir turno de {tipo}
              </button>
            ))}
          </div>
        </div>
      )}
    </Page>
  )
}

/* ---------- Ingreso con QR ---------- */
export function Ingreso() {
  const { user } = useAuth()
  const act = useAction()
  return (
    <Page title="Mi ingreso">
      <div className="panel">
        <p>Muestra este código en la entrada o registra tu ingreso desde aquí.</p>
        {/* El contenido del QR es provisional: el backend aún no define un formato de código. */}
        <div style={{ margin: '1rem 0' }}><QRCodeSVG value={`gymflex:usuario:${user!.id}`} size={190} title="Código QR de ingreso" /></div>
        <Notice m={act.msg} />
        <button className="btn" disabled={act.busy} onClick={() => act.run(() => api('/checkin/qr', 'POST'), 'Ingreso registrado. ¡Buen entrenamiento!')}>Registrar mi ingreso</button>
        <p className="muted" style={{ marginTop: '.8rem' }}>Si el gimnasio está lleno, usa la <Link to="/app/cola">cola virtual</Link>.</p>
      </div>
    </Page>
  )
}

/* ---------- Avisos ---------- */
export function Avisos() {
  const q = useLoad(() => api<Aviso[]>('/usuarios/notificaciones/mias'))
  return (
    <Page title="Avisos">
      <Status {...q} />
      <Table head={['Fecha', 'Mensaje', '']} empty="No tienes avisos."
        rows={(q.data ?? []).map((a) => [cuando(a.fechaEnvio), a.leida ? a.mensaje : <b key="m">{a.mensaje}</b>,
          a.leida ? '' : <button key="b" className="btn ghost sm" onClick={async () => { await api(`/usuarios/notificaciones/${a.id}/leida`, 'PUT'); q.reload() }}>Marcar como leído</button>])} />
    </Page>
  )
}
