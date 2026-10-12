import { useEffect, useMemo, useState } from 'react'
import { Badge, Button, Card, Col, Form, Row, Table } from 'react-bootstrap'
import { clasesApi } from '../api/endpoints.js'
import {
  Tarjeta,
  Cargando,
  Mensaje,
  Etiqueta,
  EncabezadoPagina,
  etiquetaEstado,
  nombreDia,
  proximosDias,
} from '../componentes/comunes.jsx'
import { useAuth } from '../auth/AuthContext.jsx'

const DIAS_CLAVE = ['domingo', 'lunes', 'martes', 'miercoles', 'jueves', 'viernes', 'sabado']

export default function ClasesPage() {
  const { esCliente } = useAuth()
  const dias = useMemo(() => proximosDias(14), [])
  const [fecha, setFecha] = useState(dias[0])
  const [clases, setClases] = useState([])
  const [misReservas, setMisReservas] = useState([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState('')
  const [exito, setExito] = useState('')

  async function cargar() {
    try {
      const datos = await clasesApi.listar()
      setClases(datos)
      if (esCliente) {
        setMisReservas(await clasesApi.misReservas())
      }
    } catch (excepcion) {
      setError(excepcion.message)
    } finally {
      setCargando(false)
    }
  }

  useEffect(() => {
    cargar()
  }, [])

  const diaClave = DIAS_CLAVE[new Date(`${fecha}T12:00:00`).getDay()]

  const reservaDe = (claseId) =>
    misReservas.find(
      (reserva) =>
        reserva.claseId === claseId && reserva.fecha === fecha && reserva.estado !== 'cancelada',
    )

  async function reservar(clase) {
    setError('')
    setExito('')
    try {
      const reserva = await clasesApi.reservar(clase.id, fecha)
      setExito(
        reserva.estado === 'en_espera'
          ? `La clase está llena: quedaste en lista de espera (posición ${reserva.posicionEspera}).`
          : `Reserva confirmada para ${clase.nombre} el ${fecha}.`,
      )
      cargar()
    } catch (excepcion) {
      setError(excepcion.message)
    }
  }

  async function cancelar(reserva) {
    setError('')
    setExito('')
    try {
      await clasesApi.cancelarReserva(reserva.id)
      setExito('Reserva cancelada. Si había lista de espera, se promovió al siguiente.')
      cargar()
    } catch (excepcion) {
      setError(excepcion.message)
    }
  }

  if (cargando) {
    return <Cargando>Cargando clases…</Cargando>
  }

  return (
    <div>
      <EncabezadoPagina
        titulo="Clases grupales"
        subtitulo="Reserva tu cupo con anticipación; si se llena, entras automáticamente a la lista de espera."
      />

      <Mensaje tipo="error">{error}</Mensaje>
      <Mensaje tipo="exito">{exito}</Mensaje>

      <Tarjeta titulo="Elige el día" className="mb-4">
        <div className="d-flex flex-wrap align-items-end gap-2">
          {dias.slice(0, 7).map((dia) => (
            <Button
              key={dia}
              size="sm"
              variant={dia === fecha ? 'primary' : 'outline-secondary'}
              onClick={() => setFecha(dia)}
            >
              {nombreDia(dia)} {dia.slice(8)}
            </Button>
          ))}
          <Form.Control
            type="date"
            style={{ width: 'auto' }}
            value={fecha}
            min={dias[0]}
            max={dias[dias.length - 1]}
            onChange={(e) => setFecha(e.target.value)}
          />
        </div>
      </Tarjeta>

      <Row className="g-4 mb-4">
        {clases.map((clase) => {
          const esSuDia = clase.diaSemana === diaClave
          const reserva = reservaDe(clase.id)
          return (
            <Col md={6} lg={4} key={clase.id}>
              <Card className="h-100 shadow-sm">
                <Card.Header className="bg-white d-flex justify-content-between align-items-center">
                  <span className="fw-semibold">{clase.nombre}</span>
                  {esSuDia ? (
                    <Badge bg={clase.cuposDisponibles > 0 ? 'success' : 'warning'} text={clase.cuposDisponibles > 0 ? undefined : 'dark'}>
                      {clase.cuposDisponibles > 0
                        ? `${clase.cuposDisponibles} cupos`
                        : 'lista de espera'}
                    </Badge>
                  ) : (
                    <Badge bg="secondary">no aplica este día</Badge>
                  )}
                </Card.Header>
                <Card.Body className="d-flex flex-column">
                  <p className="mb-1">
                    {clase.horaInicio.slice(0, 5)} – {clase.horaFin.slice(0, 5)} · {clase.zonaNombre}
                  </p>
                  <p className="text-secondary mb-1">Entrenador: {clase.entrenadorNombre}</p>
                  <p className="text-secondary mb-3">
                    {clase.diaSemana} · cupo máximo {clase.cupoMaximo}
                  </p>

                  {esCliente && esSuDia && (
                    <div className="d-flex flex-wrap align-items-center gap-2 mt-auto">
                      {reserva ? (
                        <>
                          <Etiqueta color={etiquetaEstado(reserva.estado)}>
                            {reserva.estado === 'en_espera'
                              ? `en espera (#${reserva.posicionEspera})`
                              : reserva.estado}
                          </Etiqueta>
                          <Button size="sm" variant="danger" onClick={() => cancelar(reserva)}>
                            Cancelar reserva
                          </Button>
                        </>
                      ) : (
                        <Button size="sm" onClick={() => reservar(clase)}>
                          Reservar para {fecha}
                        </Button>
                      )}
                    </div>
                  )}
                </Card.Body>
              </Card>
            </Col>
          )
        })}
      </Row>

      {esCliente && misReservas.length > 0 && (
        <Tarjeta titulo="Mis reservas vigentes">
          <Table hover responsive className="align-middle mb-0">
            <thead>
              <tr>
                <th>Clase</th>
                <th>Fecha</th>
                <th>Horario</th>
                <th>Estado</th>
              </tr>
            </thead>
            <tbody>
              {misReservas.map((reserva) => (
                <tr key={reserva.id}>
                  <td>{reserva.claseNombre}</td>
                  <td>{reserva.fecha}</td>
                  <td>{reserva.horaInicio.slice(0, 5)}</td>
                  <td>
                    <Etiqueta color={etiquetaEstado(reserva.estado)}>
                      {reserva.estado === 'en_espera'
                        ? `en espera (#${reserva.posicionEspera})`
                        : reserva.estado}
                    </Etiqueta>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        </Tarjeta>
      )}
    </div>
  )
}
