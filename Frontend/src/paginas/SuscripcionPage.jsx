import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { planesApi, suscripcionesApi } from '../api/endpoints.js'
import {
  Tarjeta,
  Cargando,
  Mensaje,
  Etiqueta,
  etiquetaEstado,
  formatearMoneda,
  formatearFechaHora,
} from '../componentes/comunes.jsx'

export default function SuscripcionPage() {
  const [suscripciones, setSuscripciones] = useState([])
  const [planes, setPlanes] = useState([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState('')
  const [exito, setExito] = useState('')
  const [confirmando, setConfirmando] = useState(null)
  const [motivo, setMotivo] = useState('')

  const activa = suscripciones.find((s) => s.estado === 'activa')

  async function cargar() {
    try {
      const [datosSuscripciones, datosPlanes] = await Promise.all([
        suscripcionesApi.mias(),
        planesApi.listar(),
      ])
      setSuscripciones(datosSuscripciones)
      setPlanes(datosPlanes)
    } catch (excepcion) {
      setError(excepcion.message)
    } finally {
      setCargando(false)
    }
  }

  useEffect(() => {
    cargar()
  }, [])

  async function alternarRenovacion() {
    setError('')
    try {
      await suscripcionesApi.renovacion(activa.id, !activa.renovacionAutomatica)
      setExito(
        activa.renovacionAutomatica
          ? 'Renovación automática desactivada. No se harán más cobros automáticos.'
          : 'Renovación automática activada.',
      )
      cargar()
    } catch (excepcion) {
      setError(excepcion.message)
    }
  }

  async function cambiarPlan(idPlan) {
    setError('')
    try {
      await suscripcionesApi.cambiarPlan(activa.id, idPlan)
      setExito('Plan actualizado correctamente.')
      cargar()
    } catch (excepcion) {
      setError(excepcion.message)
    }
  }

  async function cancelar() {
    setError('')
    try {
      await suscripcionesApi.cancelar(activa.id, motivo || null)
      setExito(
        'Suscripción cancelada. Conservas el acceso hasta la fecha de vencimiento y no se realizarán más cobros.',
      )
      setConfirmando(null)
      setMotivo('')
      cargar()
    } catch (excepcion) {
      setError(excepcion.message)
    }
  }

  if (cargando) {
    return <Cargando>Cargando tu suscripción…</Cargando>
  }

  return (
    <div>
      <div className="encabezado-pagina">
        <div>
          <h1>Mi suscripción</h1>
          <p>Administra tu plan, tu renovación automática y tu cancelación: todo desde aquí.</p>
        </div>
      </div>

      <Mensaje tipo="error">{error}</Mensaje>
      <Mensaje tipo="exito">{exito}</Mensaje>

      {!activa ? (
        <Tarjeta titulo="Sin suscripción activa">
          <p className="apagado">
            No tienes un plan activo en este momento. <Link to="/planes">Ver planes disponibles</Link>
          </p>
        </Tarjeta>
      ) : (
        <Tarjeta
          titulo={activa.planNombre}
          extra={<Etiqueta color={etiquetaEstado(activa.estado)}>{activa.estado}</Etiqueta>}
        >
          <p className="apagado">
            Vigencia: {activa.fechaInicio} → {activa.fechaFin} · {formatearMoneda(activa.precio)} por
            periodo
          </p>

          <div className="fila-botones" style={{ marginTop: 12 }}>
            <button className="boton secundario" onClick={alternarRenovacion}>
              {activa.renovacionAutomatica
                ? 'Desactivar renovación automática'
                : 'Activar renovación automática'}
            </button>
            <strong>
              Renovación automática:{' '}
              <Etiqueta color={activa.renovacionAutomatica ? 'verde' : 'gris'}>
                {activa.renovacionAutomatica ? 'activada' : 'desactivada'}
              </Etiqueta>
            </strong>
          </div>
          <p className="apagado" style={{ fontSize: '0.85rem' }}>
            {activa.renovacionAutomatica
              ? 'Se cobrará automáticamente al vencer. Puedes desactivarla cuando quieras, sin llamadas ni trámites.'
              : 'Al vencer no se cobrará nada; podrás renovar manualmente cuando lo desees.'}
          </p>

          <hr style={{ borderColor: 'var(--borde)', margin: '18px 0' }} />

          <h2>Cambiar de plan</h2>
          <div className="fila-botones">
            {planes
              .filter((plan) => plan.id !== activa.planId)
              .map((plan) => (
                <button
                  key={plan.id}
                  className="boton secundario pequeno"
                  onClick={() => cambiarPlan(plan.id)}
                >
                  {plan.nombre} — {formatearMoneda(plan.precio)}
                </button>
              ))}
          </div>

          <hr style={{ borderColor: 'var(--borde)', margin: '18px 0' }} />

          <h2>Cancelar suscripción</h2>
          <p className="apagado">
            La cancelación es inmediata y en línea: conservas el acceso hasta {activa.fechaFin} y no
            se realizarán más cobros.
          </p>
          {confirmando === activa.id ? (
            <div>
              <label>
                Motivo (opcional)
                <textarea
                  rows={2}
                  value={motivo}
                  onChange={(e) => setMotivo(e.target.value)}
                  placeholder="¿Por qué cancelas? Tu respuesta nos ayuda a mejorar"
                />
              </label>
              <div className="fila-botones">
                <button className="boton peligro" onClick={cancelar}>
                  Confirmar cancelación
                </button>
                <button className="boton secundario" onClick={() => setConfirmando(null)}>
                  Volver
                </button>
              </div>
            </div>
          ) : (
            <button className="boton peligro" onClick={() => setConfirmando(activa.id)}>
              Quiero cancelar mi plan
            </button>
          )}
        </Tarjeta>
      )}

      {suscripciones.length > 0 && (
        <Tarjeta titulo="Historial de suscripciones">
          <table className="tabla">
            <thead>
              <tr>
                <th>Plan</th>
                <th>Periodo</th>
                <th>Estado</th>
                <th>Cancelación</th>
              </tr>
            </thead>
            <tbody>
              {suscripciones.map((suscripcion) => (
                <tr key={suscripcion.id}>
                  <td>{suscripcion.planNombre}</td>
                  <td>
                    {suscripcion.fechaInicio} → {suscripcion.fechaFin}
                  </td>
                  <td>
                    <Etiqueta color={etiquetaEstado(suscripcion.estado)}>
                      {suscripcion.estado}
                    </Etiqueta>
                  </td>
                  <td>{formatearFechaHora(suscripcion.fechaCancelacion)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </Tarjeta>
      )}
    </div>
  )
}
