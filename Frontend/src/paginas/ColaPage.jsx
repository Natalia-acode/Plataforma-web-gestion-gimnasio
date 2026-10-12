import { useEffect, useState } from 'react'
import { Button, Col, Row } from 'react-bootstrap'
import { colaApi } from '../api/endpoints.js'
import { Tarjeta, Mensaje, Etiqueta, EncabezadoPagina } from '../componentes/comunes.jsx'

export default function ColaPage() {
  const [turno, setTurno] = useState(null)
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState('')
  const [exito, setExito] = useState('')

  async function cargar() {
    try {
      const datos = await colaApi.miTurno()
      setTurno(datos)
    } catch (excepcion) {
      setError(excepcion.message)
    } finally {
      setCargando(false)
    }
  }

  useEffect(() => {
    cargar()
    const intervalo = setInterval(cargar, 15000)
    return () => clearInterval(intervalo)
  }, [])

  async function solicitar(tipo) {
    setError('')
    setExito('')
    try {
      const nuevo = await colaApi.solicitar(tipo)
      setTurno(nuevo)
      setExito(`Turno ${nuevo.posicion} registrado. Te avisaremos cuando sea tu momento.`)
    } catch (excepcion) {
      setError(excepcion.message)
    }
  }

  async function cancelar() {
    setError('')
    setExito('')
    try {
      await colaApi.cancelar()
      setTurno(null)
      setExito('Turno cancelado. Puedes pedir uno nuevo cuando lo necesites.')
    } catch (excepcion) {
      setError(excepcion.message)
    }
  }

  return (
    <div>
      <EncabezadoPagina
        titulo="Cola virtual"
        subtitulo="Sin filas físicas: pide tu turno desde el móvil y llega justo cuando te llamemos."
      />

      <Mensaje tipo="error">{error}</Mensaje>
      <Mensaje tipo="exito">{exito}</Mensaje>

      <Row className="g-4">
        <Col lg={6}>
          <Tarjeta titulo="Mi turno" className="h-100">
            {cargando ? (
              <p className="text-secondary mb-0">Consultando…</p>
            ) : turno ? (
              <div
                className={`text-center p-4 rounded ${
                  turno.estado === 'llamado' ? 'bg-warning-subtle' : 'bg-primary-subtle'
                }`}
              >
                <div className="display-4 fw-bold">#{turno.posicion}</div>
                <Etiqueta color={turno.estado === 'llamado' ? 'amarillo' : 'azul'}>
                  {turno.estado}
                </Etiqueta>
                {turno.estado === 'llamado' ? (
                  <p className="mt-3 mb-1">
                    ¡Es tu turno! Preséntate en recepción dentro de la ventana indicada.
                  </p>
                ) : (
                  <p className="mt-3 mb-1">
                    Personas delante: <strong>{turno.personasDelante}</strong> · Espera estimada:{' '}
                    <strong>{turno.esperaEstimadaMin} min</strong>
                  </p>
                )}
                <p className="text-secondary small">
                  Tipo: {turno.tipo} · solicitado{' '}
                  {new Date(turno.fechaSolicitud).toLocaleTimeString('es-CO')}
                </p>
                <Button size="sm" variant="danger" onClick={cancelar}>
                  Cancelar mi turno
                </Button>
              </div>
            ) : (
              <div>
                <p className="text-secondary">No tienes un turno activo.</p>
                <div className="d-flex flex-wrap gap-2">
                  <Button onClick={() => solicitar('ingreso')}>Turno para ingreso</Button>
                  <Button variant="info" onClick={() => solicitar('clase')}>
                    Turno para clase
                  </Button>
                </div>
              </div>
            )}
          </Tarjeta>
        </Col>

        <Col lg={6}>
          <Tarjeta titulo="¿Cómo funciona?" className="h-100">
            <ul className="mb-2">
              <li>Solicita tu turno virtual sin acercarte a la recepción.</li>
              <li>Consulta tu posición y el tiempo estimado de espera en tiempo real.</li>
              <li>Recibirás una notificación cuando sea tu turno (ventana de 10 minutos).</li>
              <li>Si no puedes asistir, cancela tu turno y libera el lugar para otra persona.</li>
            </ul>
            <p className="text-secondary small mb-0">
              Un turno no atendido dentro de la ventana expira automáticamente y la fila avanza.
            </p>
          </Tarjeta>
        </Col>
      </Row>
    </div>
  )
}
