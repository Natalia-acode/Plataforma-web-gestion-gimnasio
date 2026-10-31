import { FormEvent, useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { api } from '../api'
import type { Aforo, Clase, LoginResp, Plan, RegistroResp } from '../api'
import { useAuth } from '../auth'
import { AforoCard, Field, Notice, Page, Status, money, hora, useAction, useForm, useLoad } from '../ui'

function textoPlanLegible(texto: string) {
  const caracteres = Array.from(texto)
  if (!/[ÃÂ]/.test(texto) || caracteres.some((caracter) => caracter.charCodeAt(0) > 0xff)) return texto
  try {
    return new TextDecoder('utf-8', { fatal: true }).decode(
      Uint8Array.from(caracteres, (caracter) => caracter.charCodeAt(0)),
    )
  } catch {
    return texto
  }
}

function PlanGrid({ planes }: { planes: Plan[] }) {
  const { user } = useAuth()
  return (
    <div className="grid">
      {planes.map((p) => (
        <article className="plan" key={p.id}>
          <h3>{textoPlanLegible(p.nombre)}</h3>
          <p className="price">{money(p.precio)} <small>por {p.duracionDias === 30 ? 'mes' : `${p.duracionDias} días`}</small></p>
          <p>{p.descripcion ? textoPlanLegible(p.descripcion) : p.descripcion}</p>
          <Link className="btn" to={user ? '/app/membresia' : '/registro'}>Elegir plan</Link>
        </article>
      ))}
    </div>
  )
}

export function Home() {
  const aforo = useLoad(() => api<Aforo>('/aforo/ocupacion'), [], 20000)
  const planes = useLoad(() => api<Plan[]>('/planes'))
  return (
    <>
      <section className="hero">
        <div>
          <h1>Entra cuando haya lugar</h1>
          <p className="lead">Mira cuánta gente hay ahora, reserva tu clase y cancela tu plan desde aquí, sin llamar a nadie.</p>
          <div className="row"><Link className="btn" to="/registro">Crear cuenta</Link><Link className="btn ghost" to="/planes">Ver planes</Link></div>
        </div>
        {aforo.data ? <AforoCard a={aforo.data} /> : <Status {...aforo} />}
      </section>
      <h2>Planes</h2>
      <Status {...planes} />
      {planes.data && <PlanGrid planes={planes.data.slice(0, 3)} />}
    </>
  )
}

export function Planes() {
  const q = useLoad(() => api<Plan[]>('/planes'))
  return <Page title="Planes"><Status {...q} />{q.data && <PlanGrid planes={q.data} />}</Page>
}

const ORDEN = ['lunes', 'martes', 'miercoles', 'jueves', 'viernes', 'sabado', 'domingo']
export function ClasesPublicas() {
  const q = useLoad(() => api<Clase[]>('/clases'))
  return (
    <Page title="Clases de la semana">
      <Status {...q} />
      {ORDEN.map((d) => {
        const del = (q.data ?? []).filter((c) => c.diaSemana === d && c.activa)
        return del.length ? (
          <div key={d}>
            <h2 style={{ textTransform: 'capitalize' }}>{d}</h2>
            <div className="grid">
              {del.map((c) => (
                <article className="panel" key={c.id}>
                  <h3>{c.nombre}</h3>
                  <p>{hora(c.horaInicio)} a {hora(c.horaFin)} en {c.zonaNombre}</p>
                  <p className="muted">Con {c.entrenadorNombre}. Quedan {c.cuposDisponibles} cupos.</p>
                </article>
              ))}
            </div>
          </div>
        ) : null
      })}
    </Page>
  )
}

export function Login() {
  const { user, login } = useAuth()
  const nav = useNavigate()
  const act = useAction()
  const { f, on } = useForm({ email: '', contrasena: '' })
  if (user) return <Navigate to="/app" replace />
  const enviar = (e: FormEvent) => {
    e.preventDefault()
    act.run(async () => { const r = await api<LoginResp>('/auth/login', 'POST', f); login(r.token, r.usuario); nav('/app') }, '')
  }
  return (
    <Page title="Entrar">
      <form className="form" onSubmit={enviar}>
        <Field label="Correo"><input type="email" required value={f.email} onChange={on('email')} autoComplete="email" /></Field>
        <Field label="Contraseña"><input type="password" required value={f.contrasena} onChange={on('contrasena')} autoComplete="current-password" /></Field>
        <Notice m={act.msg} />
        <button className="btn" disabled={act.busy}>Entrar</button>
        <p><Link to="/recuperar">Olvidé mi contraseña</Link></p>
      </form>
    </Page>
  )
}

type Arranque = { faltaAdministrador: boolean; rolesDisponibles: string[] }
const ROLES = ['cliente', 'recepcionista', 'entrenador', 'administrador']

export function Registro() {
  const { login } = useAuth()
  const nav = useNavigate()
  const act = useAction()
  const arranque = useLoad<Arranque>(() => api<Arranque>('/auth/arranque'))
  const [solicitada, setSolicitada] = useState<{ rol: string; email: string } | null>(null)
  const { f, on } = useForm({ nombre: '', apellidos: '', email: '', contrasena: '', telefono: '', fechaNacimiento: '', rol: 'cliente' })
  const sinAdministrador = arranque.data?.faltaAdministrador === true

  const enviar = (e: FormEvent) => {
    e.preventDefault()
    act.run(async () => {
      const r = await api<RegistroResp>('/auth/registro', 'POST', {
        ...f,
        telefono: f.telefono || undefined,
        fechaNacimiento: f.fechaNacimiento || undefined,
      })
      if (r.pendienteDeAprobacion) { setSolicitada({ rol: f.rol, email: f.email }); return }
      if (!r.token) throw new Error('La API no devolvió sesión. Inicie sesión con su correo y contraseña.')
      login(r.token, r.usuario)
      nav('/app')
    }, 'Cuenta creada.')
  }

  if (solicitada) {
    return (
      <Page title="Solicitud enviada">
        <p>Registramos su solicitud de cuenta de <b>{solicitada.rol}</b> para <b>{solicitada.email}</b>.</p>
        <p className="muted">Un administrador debe aprobarla: mientras esté pendiente no podrá iniciar sesión.
          Si no recibe respuesta, escriba a la administración del gimnasio.</p>
        <div className="row">
          <Link className="btn" to="/">Volver al inicio</Link>
          <Link className="btn ghost" to="/login">Ya tengo cuenta</Link>
        </div>
      </Page>
    )
  }

  return (
    <Page title="Crear cuenta">
      <form className="form" onSubmit={enviar}>
        <Field label="Nombre"><input required value={f.nombre} onChange={on('nombre')} /></Field>
        <Field label="Apellidos"><input required value={f.apellidos} onChange={on('apellidos')} /></Field>
        <Field label="Correo"><input type="email" required value={f.email} onChange={on('email')} autoComplete="email" /></Field>
        <Field label="Contraseña (mínimo 8 caracteres)"><input type="password" required minLength={8} maxLength={72} value={f.contrasena} onChange={on('contrasena')} autoComplete="new-password" /></Field>
        <Field label="Tipo de cuenta">
          <select value={f.rol} onChange={on('rol')}>{ROLES.map((r) => <option key={r}>{r}</option>)}</select>
        </Field>
        {f.rol !== 'cliente' && (
          <p className="muted">
            {sinAdministrador
              ? 'Todavía no hay administrador: esta primera cuenta de personal queda activa de inmediato.'
              : 'Las cuentas de personal las aprueba un administrador; la suya quedará pendiente hasta que la revise.'}
          </p>
        )}
        <Field label="Teléfono (opcional)"><input type="tel" value={f.telefono} onChange={on('telefono')} /></Field>
        <Field label="Fecha de nacimiento (opcional)"><input type="date" value={f.fechaNacimiento} onChange={on('fechaNacimiento')} /></Field>
        <Notice m={act.msg} />
        <button className="btn" disabled={act.busy}>Crear cuenta</button>
      </form>
    </Page>
  )
}

export function Recuperar() {
  const act = useAction()
  const { f, on } = useForm({ email: '' })
  return (
    <Page title="Recuperar contraseña">
      <form className="form" onSubmit={(e) => { e.preventDefault(); act.run(() => api('/auth/recuperar-contrasena', 'POST', f), 'Si el correo existe, recibirás un enlace para cambiar tu contraseña.') }}>
        <Field label="Correo"><input type="email" required value={f.email} onChange={on('email')} /></Field>
        <Notice m={act.msg} />
        <button className="btn" disabled={act.busy}>Enviar enlace</button>
      </form>
    </Page>
  )
}
