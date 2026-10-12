import { useEffect, useState } from 'react'
import { Button, Col, Form, Row, Table } from 'react-bootstrap'
import { aforoApi, checkinApi, colaApi } from '../api/endpoints.js'
import {
  Tarjeta,
  Mensaje,
  Cargando,
  EncabezadoPagina,
  formatearFechaHora,
} from '../componentes/comunes.jsx'
import SemaforoAforo from '../componentes/SemaforoAforo.jsx'

export default function RecepcionPage() {
  const [ocupacion, setOcupacion] = useState(null)
  const [dentro, setDentro] = useState([])
  const [cola, setCola] = useState([])
  const [idManual, setIdManual] = useState('')
  const [error, setError] = useState('')
  const [exito, setExito] = useState('')
  const [cargando, setCargando] = useState(true)

  async function cargar() {
    try {
      const [datosOcupacion, datosDentro, datosCola] = await Promise.all([
        aforoApi.ocupacion(),
        checkinApi.dentro(),
        colaApi.colaCompleta(),
      ])
      setOcupacion(datosOcupacion)
      setDentro(datosDentro)
      setCola(datosCola)
    } catch (excepcion) {
      setError(excepcion.message)
    } finally {
      setCargando(false)
    }
  }

  useEffect(() => {
    cargar()
    const intervalo = setInterval(cargar, 20000)
    return () => clearInterval(intervalo)
  }, [])

  async function entradaManual(evento) {
    evento.preventDefault()
    setError('')
    setExito('')
    try {
      const respuesta = await checkinApi.entradaManual(Number(idManual))
      setExito(`Ingreso registrado: ${respuesta.usuarioNombre}.`)
      setIdManual('')
      cargar()
    } catch (excepcion) {
      setError(excepcion.message)
    }
  }

  async function registrarSalida(usuarioId, nombre) {
    setError('')
    setExito('')
    try {
      await checkinApi.salida(usuarioId)
      setExito(`Salida registrada: ${nombre}.`)
      cargar()
    } catch (excepcion) {
      setError(excepcion.message)
    }
  }

  if (cargando) {
    return <Cargando>Cargando panel de recepción…</Cargando>
  }

  return (
    <div>
      <EncabezadoPagina
        titulo="Recepción"
        subtitulo="Registra ingresos y salidas, y monitorea la ocupación del gimnasio en vivo."
      />

      <Mensaje tipo="error">{error}</Mensaje>
      <Mensaje tipo="exito">{exito}</Mensaje>

      <Row className="g-4 mb-4">
        <Col lg={6}>
          <Tarjeta titulo="Ocupación actual" className="h-100">
            <SemaforoAforo ocupacion={ocupacion} />
          </Tarjeta>
        </Col>

        <Col lg={6}>
          <Tarjeta titulo="Registrar ingreso manual" className="h-100">
            <Form onSubmit={entradaManual}>
              <Form.Group className="mb-3" controlId="idUsuarioManual" style={{ maxWidth: 280 }}>
                <Form.Label>ID del usuario</Form.Label>
                <Form.Control
                  type="number"
                  min="1"
                  value={idManual}
                  onChange={(e) => setIdManual(e.target.value)}
                  placeholder="Ej. 4"
                  required
                />
              </Form.Group>
              <Button type="submit">Registrar entrada</Button>
            </Form>
            <p className="text-secondary small mt-3 mb-0">
              El sistema valida que la suscripción esté activa y vigente antes de permitir el
              ingreso.
            </p>
          </Tarjeta>
        </Col>
      </Row>

      <Row className="g-4">
        <Col lg={7}>
          <Tarjeta titulo={`Personas dentro (${dentro.length})`} className="h-100">
            {dentro.length === 0 ? (
              <p className="text-secondary mb-0">No hay personas registradas dentro.</p>
            ) : (
              <Table hover responsive className="align-middle mb-0">
                <thead>
                  <tr>
                    <th>Persona</th>
                    <th>Entrada</th>
                    <th>Método</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  {dentro.map((registro) => (
                    <tr key={registro.id}>
                      <td>{registro.usuarioNombre}</td>
                      <td>{formatearFechaHora(registro.fechaHoraEntrada)}</td>
                      <td>{registro.metodo}</td>
                      <td className="text-end">
                        <Button
                          size="sm"
                          variant="outline-secondary"
                          onClick={() =>
                            registrarSalida(registro.usuarioId, registro.usuarioNombre)
                          }
                        >
                          Registrar salida
                        </Button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </Table>
            )}
          </Tarjeta>
        </Col>

        <Col lg={5}>
          <Tarjeta titulo={`Cola virtual (${cola.length})`} className="h-100">
            {cola.length === 0 ? (
              <p className="text-secondary mb-0">No hay turnos activos en la cola.</p>
            ) : (
              <Table hover responsive className="align-middle mb-0">
                <thead>
                  <tr>
                    <th>#</th>
                    <th>Persona</th>
                    <th>Tipo</th>
                    <th>Estado</th>
                  </tr>
                </thead>
                <tbody>
                  {cola.map((turno) => (
                    <tr key={turno.id}>
                      <td>{turno.posicion}</td>
                      <td>Turno {turno.id}</td>
                      <td>{turno.tipo}</td>
                      <td>{turno.estado}</td>
                    </tr>
                  ))}
                </tbody>
              </Table>
            )}
          </Tarjeta>
        </Col>
      </Row>
    </div>
  )
}
