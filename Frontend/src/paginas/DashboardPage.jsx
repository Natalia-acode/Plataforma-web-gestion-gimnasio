import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext.jsx'
import { aforoApi, clasesApi, suscripcionesApi, usuariosApi } from '../api/endpoints.js'
import { Tarjeta, Cargando, Mensaje, formatearMoneda } from '../componentes/comunes.jsx'
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
      <div className="encabezado-pagina">
        <div>
          <h1>Hola, {usuario?.nombre}</h1>
          <p>
            {new Date().toLocaleDateString('es-CO', {
              weekday: 'long',
              day: 'numeric',
              month: 'long',
            })}
          </p>
        </div>
        <Link className="boton secundario" to="/clases">
          Ver clases de hoy
        </Link>
      </div>

      <Mensaje tipo="error">{error}</Mensaje>

      <div className="grid-2">
        <Tarjeta titulo="Ocupación en tiempo real">
          <SemaforoAforo ocupacion={ocupacion} />
          <p className="apagado" style={{ fontSize: '0.8rem', marginBottom: 0 }}>
            Se actualiza automáticamente cada 30 segundos.
          </p>
        </Tarjeta>

        {esCliente && (
          <Tarjeta
            titulo="Mi suscripción"
            extra={
              <Link className="boton pequeno secundario" to="/suscripcion">
                Gestionar
              </Link>
            }
          >
            {suscripcion ? (
              <>
                <h3>{suscripcion.planNombre}</h3>
                <p className="apagado">
                  Vigente hasta {suscripcion.fechaFin} ·{' '}
                  {formatearMoneda(suscripcion.precio)} por periodo
                </p>
                <p>
                  Renovación automática:{' '}
                  <strong>{suscripcion.renovacionAutomatica ? 'activada' : 'desactivada'}</strong>
                </p>
              </>
            ) : (
              <p className="apagado">
                Aún no tienes una suscripción activa.{' '}
                <Link to="/planes">Explora los planes disponibles</Link>.
              </p>
            )}
          </Tarjeta>
        )}
      </div>

      {esCliente && notificaciones.length > 0 && (
        <Tarjeta titulo="Avisos recientes">
          <ul className="lista-simple">
            {notificaciones.map((n) => (
              <li key={n.id} className="notificacion-no-leida">
                <span>{n.mensaje}</span>
                <button
                  className="boton pequeno secundario"
                  onClick={() => marcarLeida(n.id)}
                >
                  Visto
                </button>
              </li>
            ))}
          </ul>
        </Tarjeta>
      )}

      <Tarjeta titulo="Mapa de calor de afluencia">
        <p className="apagado">
          Consulta las franjas menos concurridas antes de venir: verde claro = poca gente, verde
          intenso = más gente.
        </p>
        {afluencia ? <MapaCalor celdas={afluencia.celdas} /> : <Cargando />}
        {afluencia?.horaMenosConcurrida && (
          <p className="mensaje info">
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
          <p className="apagado">No hay clases programadas para hoy.</p>
        ) : (
          <table className="tabla">
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
                  <td>
                    <Link className="boton pequeno" to="/clases">
                      Reservar
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </Tarjeta>
    </div>
  )
}
