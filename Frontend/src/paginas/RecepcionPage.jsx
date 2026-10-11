import { useEffect, useState } from 'react'
import { aforoApi, checkinApi, colaApi } from '../api/endpoints.js'
import { Tarjeta, Mensaje, Cargando, formatearFechaHora } from '../componentes/comunes.jsx'
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
      <div className="encabezado-pagina">
        <div>
          <h1>Recepción</h1>
          <p>Registra ingresos y salidas, y monitorea la ocupación del gimnasio en vivo.</p>
        </div>
      </div>

      <Mensaje tipo="error">{error}</Mensaje>
      <Mensaje tipo="exito">{exito}</Mensaje>

      <div className="grid-2">
        <Tarjeta titulo="Ocupación actual">
          <SemaforoAforo ocupacion={ocupacion} />
        </Tarjeta>

        <Tarjeta titulo="Registrar ingreso manual">
          <form onSubmit={entradaManual}>
            <label>
              ID del usuario
              <input
                type="number"
                min="1"
                value={idManual}
                onChange={(e) => setIdManual(e.target.value)}
                placeholder="Ej. 4"
                required
              />
            </label>
            <button className="boton">Registrar entrada</button>
          </form>
          <p className="apagado" style={{ fontSize: '0.83rem' }}>
            El sistema valida que la suscripción esté activa y vigente antes de permitir el ingreso.
          </p>
        </Tarjeta>
      </div>

      <div className="grid-2">
        <Tarjeta titulo={`Personas dentro (${dentro.length})`}>
          {dentro.length === 0 ? (
            <p className="apagado">No hay personas registradas dentro.</p>
          ) : (
            <table className="tabla">
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
                    <td>
                      <button
                        className="boton pequeno secundario"
                        onClick={() => registrarSalida(registro.usuarioId, registro.usuarioNombre)}
                      >
                        Registrar salida
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </Tarjeta>

        <Tarjeta titulo={`Cola virtual (${cola.length})`}>
          {cola.length === 0 ? (
            <p className="apagado">No hay turnos activos en la cola.</p>
          ) : (
            <table className="tabla">
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
            </table>
          )}
        </Tarjeta>
      </div>
    </div>
  )
}
