import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { Badge, Button, Col, ListGroup, Row, Table } from 'react-bootstrap'
import { useAuth } from '../auth/AuthContext.jsx'
import { aforoApi, clasesApi, suscripcionesApi, usuariosApi } from '../api/endpoints.js'
import {
  Tarjeta,
  Cargando,
  Mensaje,
  EncabezadoPagina,
  formatearMoneda,
} from '../componentes/comunes.jsx'
import SemaforoAforo from '../componentes/SemaforoAforo.jsx'
import MapaCalor from '../componentes/MapaCalor.jsx'

export default function DashboardPage() {
  const { usuario, esCliente } = useAuth()
  const [ocupacion, setOcupacion] = useState(null)
  const [afluencia, setAfluencia] = useState(null)
  const [suscripcion, setSuscripcion] = useState(null)
  const [clasesHoy, setClasesHoy] = useState([])
  const [notificaciones, setNotificaciones] = useState([])
  const [error, setError] = useState('')

  useEffect(() => {
    let activo = true
    const cargar = async () => {
      try {
        const [datosOcupacion, datosAfluencia] = await Promise.all([
          aforoApi.ocupacion(),
          aforoApi.afluencia(),
        ])
        if (activo) {
          setOcupacion(datosOcupacion)
          setAfluencia(datosAfluencia)
        }
      } catch (excepcion) {
        if (activo) setError(excepcion.message)
      }
    }
    cargar()
    const intervalo = setInterval(cargar, 30000)
    return () => {
      activo = false
      clearInterval(intervalo)
    }
  }, [])

  useEffect(() => {
    let activo = true
    const diaHoy = ['domingo', 'lunes', 'martes', 'miercoles', 'jueves', 'viernes', 'sabado'][
      new Date().getDay()
    ]
    clasesApi
      .listar(diaHoy)
      .then((datos) => activo && setClasesHoy(datos.slice(0, 5)))
      .catch(() => {})
    if (esCliente) {
      suscripcionesApi
        .activa()
        .then((datos) => activo && setSuscripcion(datos))
        .catch(() => activo && setSuscripcion(null))
      usuariosApi
        .notificaciones()
        .then((datos) => activo && setNotificaciones(datos.filter((n) => !n.leida).slice(0, 4)))
        .catch(() => {})
    }
    return () => {
      activo = false
    }
  }, [esCliente])

  async function marcarLeida(id) {
    await usuariosApi.marcarLeida(id)
    setNotificaciones((actual) => actual.filter((n) => n.id !== id))
  }

  return (
    <div>
      <EncabezadoPagina
        titulo={`Hola, ${usuario?.nombre}`}
        subtitulo={new Date().toLocaleDateString('es-CO', {
          weekday: 'long',
          day: 'numeric',
          month: 'long',
        })}
      >
        <Button as={Link} to="/clases" variant="outline-secondary">
          Ver clases de hoy
        </Button>
      </EncabezadoPagina>

      <Mensaje tipo="error">{error}</Mensaje>

      <Row className="g-4 mb-4">
        <Col lg={6}>
          <Tarjeta titulo="Ocupación en tiempo real" className="h-100">
            <SemaforoAforo ocupacion={ocupacion} />
            <p className="text-secondary small mb-0">
              Se actualiza automáticamente cada 30 segundos.
            </p>
          </Tarjeta>
        </Col>

        {esCliente && (
          <Col lg={6}>
            <Tarjeta
              titulo="Mi suscripción"
              className="h-100"
              extra={
                <Button as={Link} to="/suscripcion" size="sm" variant="outline-secondary">
                  Gestionar
                </Button>
              }
            >
              {suscripcion ? (
                <>
                  <h3 className="h5">{suscripcion.planNombre}</h3>
                  <p className="text-secondary mb-2">
                    Vigente hasta {suscripcion.fechaFin} ·{' '}
                    {formatearMoneda(suscripcion.precio)} por periodo
                  </p>
                  <p className="mb-0">
                    Renovación automática:{' '}
                    <Badge bg={suscripcion.renovacionAutomatica ? 'success' : 'secondary'}>
                      {suscripcion.renovacionAutomatica ? 'activada' : 'desactivada'}
                    </Badge>
                  </p>
                </>
              ) : (
                <p className="text-secondary mb-0">
                  Aún no tienes una suscripción activa.{' '}
                  <Link to="/planes">Explora los planes disponibles</Link>.
                </p>
              )}
            </Tarjeta>
          </Col>
        )}
      </Row>

      {esCliente && notificaciones.length > 0 && (
        <Tarjeta titulo="Avisos recientes" className="mb-4">
          <ListGroup variant="flush">
            {notificaciones.map((n) => (
              <ListGroup.Item
                key={n.id}
                className="d-flex justify-content-between align-items-center px-0 notificacion-no-leida"
              >
                <span className="me-3">{n.mensaje}</span>
                <Button size="sm" variant="outline-secondary" onClick={() => marcarLeida(n.id)}>
                  Visto
                </Button>
              </ListGroup.Item>
            ))}
          </ListGroup>
        </Tarjeta>
      )}

      <Tarjeta titulo="Mapa de calor de afluencia" className="mb-4">
        <p className="text-secondary">
          Consulta las franjas menos concurridas antes de venir: verde claro = poca gente, verde
          intenso = más gente.
        </p>
        {afluencia ? <MapaCalor celdas={afluencia.celdas} /> : <Cargando />}
        {afluencia?.horaMenosConcurrida && (
          <p className="alert alert-info">
            Sugerencia: la franja más tranquila suele ser{' '}
            <strong>
              {afluencia.horaMenosConcurrida.diaSemana} a las{' '}
              {afluencia.horaMenosConcurrida.hora}:00
            </strong>{' '}
            y la más concurrida{' '}
            <strong>
              {afluencia.horaMasConcurrida?.diaSemana} a las {afluencia.horaMasConcurrida?.hora}:00
            </strong>
            .
          </p>
        )}
      </Tarjeta>

      <Tarjeta titulo="Clases de hoy">
        {clasesHoy.length === 0 ? (
          <p className="text-secondary mb-0">No hay clases programadas para hoy.</p>
        ) : (
          <Table hover responsive className="align-middle mb-0">
            <thead>
              <tr>
                <th>Clase</th>
                <th>Horario</th>
                <th>Entrenador</th>
                <th>Cupos</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {clasesHoy.map((clase) => (
                <tr key={clase.id}>
                  <td>{clase.nombre}</td>
                  <td>
                    {clase.horaInicio.slice(0, 5)} – {clase.horaFin.slice(0, 5)}
                  </td>
                  <td>{clase.entrenadorNombre}</td>
                  <td>
                    {clase.cuposDisponibles} de {clase.cupoMaximo}
                  </td>
                  <td className="text-end">
                    <Button as={Link} to="/clases" size="sm">
                      Reservar
                    </Button>
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
