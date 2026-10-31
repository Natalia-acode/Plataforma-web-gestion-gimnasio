import { ChangeEvent, ReactNode, useCallback, useEffect, useState } from 'react'
import type { Aforo } from './api'

export const money = (n: number) =>
  new Intl.NumberFormat('es-CO', { style: 'currency', currency: (import.meta.env.VITE_CURRENCY as string | undefined) ?? 'COP' }).format(n)
export const dia = (s?: string) => (s ? s.slice(0, 10).split('-').reverse().join('/') : '—')
export const hora = (s?: string) => (s ? s.slice(0, 5) : '')
export const cuando = (s?: string) => (s ? new Date(s).toLocaleString('es-CO', { dateStyle: 'short', timeStyle: 'short' }) : '—')
export const iso = (d: Date) => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`

/** Carga datos y los refresca cada `every` ms si se indica. */
export function useLoad<T>(fn: () => Promise<T>, deps: unknown[] = [], every?: number) {
  const [data, setData] = useState<T | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)
  // eslint-disable-next-line react-hooks/exhaustive-deps
  const run = useCallback(async () => {
    try { setData(await fn()); setError(null) } catch (e) { setError((e as Error).message) } finally { setLoading(false) }
  }, deps)
  useEffect(() => {
    run()
    if (!every) return
    const t = setInterval(run, every)
    return () => clearInterval(t)
  }, [run, every])
  return { data, error, loading, reload: run }
}

type Msg = { ok: boolean; text: string } | null
/** Ejecuta una acción del usuario y guarda el mensaje de éxito o error. */
export function useAction() {
  const [msg, setMsg] = useState<Msg>(null)
  const [busy, setBusy] = useState(false)
  const run = async (fn: () => Promise<unknown>, okText: string, after?: () => void) => {
    setBusy(true)
    try { await fn(); setMsg({ ok: true, text: okText }); after?.() } catch (e) { setMsg({ ok: false, text: (e as Error).message }) } finally { setBusy(false) }
  }
  return { msg, busy, run }
}

export function useForm<T extends Record<string, string>>(init: T) {
  const [f, setF] = useState(init)
  const on = (k: keyof T) => (e: ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) =>
    setF((p) => ({ ...p, [k]: e.target.value }))
  return { f, on, reset: () => setF(init) }
}

export const Notice = ({ m }: { m: Msg }) =>
  m && m.text ? <p className={`notice ${m.ok ? 'ok' : 'err'}`} role="status">{m.text}</p> : null
export const Status = ({ loading, error }: { loading: boolean; error: string | null }) =>
  error ? <p className="notice err" role="alert">{error}</p> : loading ? <p className="empty">Cargando…</p> : null
export const Tag = ({ v }: { v: string }) => <span className={`tag ${v}`}>{v.replace('_', ' ')}</span>
export const Page = ({ title, children }: { title: string; children: ReactNode }) =>
  <section className="page"><h1>{title}</h1>{children}</section>
export const Field = ({ label, children }: { label: string; children: ReactNode }) =>
  <label className="field"><span>{label}</span>{children}</label>

export function Table({ head, rows, empty = 'Sin registros' }: { head: string[]; rows: ReactNode[][]; empty?: string }) {
  if (!rows.length) return <p className="empty">{empty}</p>
  return (
    <div className="scroll">
      <table>
        <thead><tr>{head.map((h) => <th key={h}>{h}</th>)}</tr></thead>
        <tbody>{rows.map((r, i) => <tr key={i}>{r.map((c, j) => <td key={j}>{c}</td>)}</tr>)}</tbody>
      </table>
    </div>
  )
}

export function AforoCard({ a }: { a: Aforo }) {
  return (
    <div className={`aforo ${a.nivel.toLowerCase()}`}>
      <p className="aforo-num"><b>{a.personasDentro}</b><span>de {a.capacidadMaxima} personas dentro ahora</span></p>
      <div className="bar" role="meter" aria-label="Ocupación" aria-valuemin={0} aria-valuemax={100} aria-valuenow={a.porcentajeOcupacion}>
        <i style={{ width: `${Math.min(100, a.porcentajeOcupacion)}%` }} />
      </div>
      <p>{a.recomendacion}</p>
    </div>
  )
}
