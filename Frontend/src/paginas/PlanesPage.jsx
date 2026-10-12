import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Button, Card, Col, Form, Row } from 'react-bootstrap'
import { pagosApi, planesApi, suscripcionesApi } from '../api/endpoints.js'
import {
  Tarjeta,
  Cargando,
  Mensaje,
  EncabezadoPagina,
  formatearMoneda,
} from '../componentes/comunes.jsx'

export default function PlanesPage() {
  const navegar = useNavigate()
  const [planes, setPlanes] = useState([])
  const [metodos, setMetodos] = useState([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState('')
  const [exito, setExito] = useState('')
  const [enviando, setEnviando] = useState(false)
  const [seleccion, setSeleccion] = useState({ idPlan: null, idMetodo: '', renovacionAutomatica: false })

  useEffect(() => {
    Promise.all([planesApi.listar(), pagosApi.metodos()])
      .then(([datosPlanes, datosMetodos]) => {
        setPlanes(datosPlanes)
        setMetodos(datosMetodos)
        if (datosMetodos.length > 0) {
          const predeterminado = datosMetodos.find((m) => m.predeterminado) || datosMetodos[0]
          setSeleccion((actual) => ({ ...actual, idMetodo: String(predeterminado.id) }))
        }
      })
      .catch((excepcion) => setError(excepcion.message))
      .finally(() => setCargando(false))
  }, [])

  async function contratar(evento) {
    evento.preventDefault()
    setError('')
    setExito('')
    setEnviando(true)
    try {
      await suscripcionesApi.contratar({
        idPlan: seleccion.idPlan,
        idMetodo: seleccion.idMetodo ? Number(seleccion.idMetodo) : null,
        renovacionAutomatica: seleccion.renovacionAutomatica,
      })
      setExito('¡Suscripción activada! Ya puedes reservar clases y registrar tu ingreso.')
      setTimeout(() => navegar('/suscripcion'), 1200)
    } catch (excepcion) {
      setError(excepcion.message)
    } finally {
      setEnviando(false)
    }
  }

  if (cargando) {
    return <Cargando>Cargando planes…</Cargando>
  }

  return (
    <div>
      <EncabezadoPagina
        titulo="Planes del gimnasio"
        subtitulo="Elige tu plan. La renovación automática es opcional: tú decides si activarla."
      >
        <Button as={Link} to="/pagos" variant="outline-secondary">
          Mis métodos de pago
        </Button>
      </EncabezadoPagina>

      <Mensaje tipo="error">{error}</Mensaje>
      <Mensaje tipo="exito">{exito}</Mensaje>

      <Row className="g-4 mb-4">
        {planes.map((plan) => (
          <Col md={6} lg={4} key={plan.id}>
            <Card className="h-100 shadow-sm">
              <Card.Body className="d-flex flex-column">
                <Card.Title as="h2" className="h5">
                  {plan.nombre}
                </Card.Title>
                <h3 className="fs-2">{formatearMoneda(plan.precio)}</h3>
                <p className="text-secondary mb-2">Duración: {plan.duracionDias} días</p>
                <p>{plan.descripcion}</p>
                <Button
                  className="mt-auto"
                  variant={seleccion.idPlan === plan.id ? 'success' : 'primary'}
                  onClick={() => setSeleccion((actual) => ({ ...actual, idPlan: plan.id }))}
                  disabled={seleccion.idPlan === plan.id}
                >
                  {seleccion.idPlan === plan.id ? 'Seleccionado' : 'Elegir este plan'}
                </Button>
              </Card.Body>
            </Card>
          </Col>
        ))}
      </Row>

      {seleccion.idPlan && (
        <Tarjeta titulo="Confirmar contratación">
          <Form onSubmit={contratar}>
            {metodos.length === 0 ? (
              <Mensaje tipo="info">
                No tienes métodos de pago registrados. Puedes contratar y pagar en efectivo en
                recepción, o <Link to="/pagos">registrar una tarjeta</Link> para el cobro automático.
              </Mensaje>
            ) : (
              <Form.Group className="mb-3" controlId="contratarMetodo" style={{ maxWidth: 420 }}>
                <Form.Label>Tarjeta a usar</Form.Label>
                <Form.Select
                  value={seleccion.idMetodo}
                  onChange={(e) =>
                    setSeleccion((actual) => ({ ...actual, idMetodo: e.target.value }))
                  }
                >
                  {metodos.map((metodo) => (
                    <option key={metodo.id} value={metodo.id}>
                      {metodo.tipo.replace('_', ' ')} •••• {metodo.ultimosCuatro}
                      {metodo.predeterminado ? ' (predeterminada)' : ''}
                    </option>
                  ))}
                </Form.Select>
              </Form.Group>
            )}

            <Form.Check
              className="mb-3"
              type="checkbox"
              id="renovacionAutomatica"
              label="Activar renovación automática (puedes desactivarla cuando quieras)"
              checked={seleccion.renovacionAutomatica}
              onChange={(e) =>
                setSeleccion((actual) => ({ ...actual, renovacionAutomatica: e.target.checked }))
              }
            />

            <Button type="submit" disabled={enviando}>
              {enviando ? 'Procesando…' : 'Contratar plan'}
            </Button>
          </Form>
        </Tarjeta>
      )}
    </div>
  )
}
