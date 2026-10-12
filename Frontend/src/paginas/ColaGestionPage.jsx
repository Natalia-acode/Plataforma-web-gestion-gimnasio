import { useEffect, useState } from 'react'
import { Button, Card, Col, Row, Table } from 'react-bootstrap'
import { colaApi } from '../api/endpoints.js'
import { Tarjeta, Mensaje, Cargando, Etiqueta, EncabezadoPagina } from '../componentes/comunes.jsx'

export default function ColaGestionPage() {
  const [cola, setCola] = useState([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState('')
  const [exito, setExito] = useState('')

  async function cargar() {
    try {
      setCola(await colaApi.colaCompleta())
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

  async function llamarSiguiente() {
    setError('')
    setExito('')
    try {
      const turno = await colaApi.llamarSiguiente()
      setExito(`Turno #${turno.posicion} llamado. Se notificó al usuario su ventana de presentación.`)
      cargar()
    } catch (excepcion) {
      setError(excepcion.message)
    }
  }

  async function marcarAtendido(turno) {
    setError('')
    setExito('')
    try {
      await colaApi.atendido(turno.id)
      setExito(`Turno #${turno.posicion} marcado como atendido.`)
      cargar()
    } catch (excepcion) {
      setError(excepcion.message)
    }
  }

  if (cargando) {
    return <Cargando>Cargando cola…</Cargando>
  }

  const esperando = cola.filter((t) => t.estado === 'esperando')
  const llamados = cola.filter((t) => t.estado === 'llamado')

  return (
    <div>
      <EncabezadoPagina
        titulo="Gestión de la cola virtual"
        subtitulo="Llama al siguiente y marca los turnos atendidos; los no presentados expiran solos."
      >
        <Button onClick={llamarSiguiente} disabled={esperando.length === 0}>
          Llamar al siguiente
        </Button>
      </EncabezadoPagina>

      <Mensaje tipo="error">{error}</Mensaje>
      <Mensaje tipo="exito">{exito}</Mensaje>

      <Row className="g-3 mb-4">
        {[
          { valor: esperando.length, etiqueta: 'Personas esperando' },
          { valor: llamados.length, etiqueta: 'Turnos llamados' },
          { valor: cola.length, etiqueta: 'Total en cola' },
        ].map((kpi) => (
          <Col md={4} key={kpi.etiqueta}>
            <Card className="text-center shadow-sm h-100">
              <Card.Body>
                <div className="fs-2 fw-bold">{kpi.valor}</div>
                <div className="text-secondary">{kpi.etiqueta}</div>
              </Card.Body>
            </Card>
          </Col>
        ))}
      </Row>

      <Tarjeta titulo="Fila completa">
        {cola.length === 0 ? (
          <p className="text-secondary mb-0">La cola está vacía.</p>
        ) : (
          <Table hover responsive className="align-middle mb-0">
            <thead>
              <tr>
                <th>#</th>
                <th>Solicitado</th>
                <th>Tipo</th>
                <th>Estado</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {cola.map((turno) => (
                <tr key={turno.id}>
                  <td>{turno.posicion}</td>
                  <td>{new Date(turno.fechaSolicitud).toLocaleTimeString('es-CO')}</td>
                  <td>{turno.tipo}</td>
                  <td>
                    <Etiqueta color={turno.estado === 'llamado' ? 'amarillo' : 'azul'}>
                      {turno.estado}
                    </Etiqueta>
                  </td>
                  <td className="text-end">
                    {turno.estado === 'llamado' && (
                      <Button size="sm" onClick={() => marcarAtendido(turno)}>
                        Marcar atendido
                      </Button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        )}
      </Tarjeta>
    </div>
  )
}
