import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { Badge, Button, Form, Table } from 'react-bootstrap'
import { planesApi, suscripcionesApi } from '../api/endpoints.js'
import {
  Tarjeta,
  Cargando,
  Mensaje,
  Etiqueta,
  EncabezadoPagina,
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
      <EncabezadoPagina
        titulo="Mi suscripción"
        subtitulo="Administra tu plan, tu renovación automática y tu cancelación: todo desde aquí."
      />

      <Mensaje tipo="error">{error}</Mensaje>
      <Mensaje tipo="exito">{exito}</Mensaje>

      {!activa ? (
        <Tarjeta titulo="Sin suscripción activa" className="mb-4">
          <p className="text-secondary mb-0">
            No tienes un plan activo en este momento. <Link to="/planes">Ver planes disponibles</Link>
          </p>
        </Tarjeta>
      ) : (
        <Tarjeta
          titulo={activa.planNombre}
          className="mb-4"
          extra={<Etiqueta color={etiquetaEstado(activa.estado)}>{activa.estado}</Etiqueta>}
        >
          <p className="text-secondary">
            Vigencia: {activa.fechaInicio} → {activa.fechaFin} · {formatearMoneda(activa.precio)} por
            periodo
          </p>

          <div className="d-flex flex-wrap align-items-center gap-3 mt-3">
            <Button variant="outline-secondary" onClick={alternarRenovacion}>
              {activa.renovacionAutomatica
                ? 'Desactivar renovación automática'
                : 'Activar renovación automática'}
            </Button>
            <span>
              Renovación automática:{' '}
              <Badge bg={activa.renovacionAutomatica ? 'success' : 'secondary'}>
                {activa.renovacionAutomatica ? 'activada' : 'desactivada'}
              </Badge>
            </span>
          </div>
          <p className="text-secondary small mt-2">
            {activa.renovacionAutomatica
              ? 'Se cobrará automáticamente al vencer. Puedes desactivarla cuando quieras, sin llamadas ni trámites.'
              : 'Al vencer no se cobrará nada; podrás renovar manualmente cuando lo desees.'}
          </p>

          <hr />

          <h2 className="h5">Cambiar de plan</h2>
          <div className="d-flex flex-wrap gap-2">
            {planes
              .filter((plan) => plan.id !== activa.planId)
              .map((plan) => (
                <Button
                  key={plan.id}
                  size="sm"
                  variant="outline-secondary"
                  onClick={() => cambiarPlan(plan.id)}
                >
                  {plan.nombre} — {formatearMoneda(plan.precio)}
                </Button>
              ))}
          </div>

          <hr />

          <h2 className="h5">Cancelar suscripción</h2>
          <p className="text-secondary">
            La cancelación es inmediata y en línea: conservas el acceso hasta {activa.fechaFin} y no
            se realizarán más cobros.
          </p>
          {confirmando === activa.id ? (
            <div>
              <Form.Group className="mb-3" controlId="motivoCancelacion" style={{ maxWidth: 480 }}>
                <Form.Label>Motivo (opcional)</Form.Label>
                <Form.Control
                  as="textarea"
                  rows={2}
                  value={motivo}
                  onChange={(e) => setMotivo(e.target.value)}
                  placeholder="¿Por qué cancelas? Tu respuesta nos ayuda a mejorar"
                />
              </Form.Group>
              <div className="d-flex gap-2">
                <Button variant="danger" onClick={cancelar}>
                  Confirmar cancelación
                </Button>
                <Button variant="outline-secondary" onClick={() => setConfirmando(null)}>
                  Volver
                </Button>
              </div>
            </div>
          ) : (
            <Button variant="danger" onClick={() => setConfirmando(activa.id)}>
              Quiero cancelar mi plan
            </Button>
          )}
        </Tarjeta>
      )}

      {suscripciones.length > 0 && (
        <Tarjeta titulo="Historial de suscripciones">
          <Table hover responsive className="align-middle mb-0">
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
          </Table>
        </Tarjeta>
      )}
    </div>
  )
}
