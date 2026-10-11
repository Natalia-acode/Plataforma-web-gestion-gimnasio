import { useEffect, useState } from 'react'
import { colaApi } from '../api/endpoints.js'
import { Tarjeta, Mensaje, Cargando, Etiqueta } from '../componentes/comunes.jsx'

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
      <div className="encabezado-pagina">
        <div>
          <h1>Gestión de la cola virtual</h1>
          <p>Llama al siguiente y marca los turnos atendidos; los no presentados expiran solos.</p>
        </div>
        <button className="boton" onClick={llamarSiguiente} disabled={esperando.length === 0}>
          Llamar al siguiente
        </button>
      </div>

      <Mensaje tipo="error">{error}</Mensaje>
      <Mensaje tipo="exito">{exito}</Mensaje>

      <div className="grid-3">
        <div className="kpi">
          <strong>{esperando.length}</strong>
          <span>Personas esperando</span>
        </div>
        <div className="kpi">
          <strong>{llamados.length}</strong>
          <span>Turnos llamados</span>
        </div>
        <div className="kpi">
          <strong>{cola.length}</strong>
          <span>Total en cola</span>
        </div>
      </div>

      <Tarjeta titulo="Fila completa">
        {cola.length === 0 ? (
          <p className="apagado">La cola está vacía.</p>
        ) : (
          <table className="tabla">
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
                  <td>
                    {turno.estado === 'llamado' && (
                      <button className="boton pequeno" onClick={() => marcarAtendido(turno)}>
                        Marcar atendido
                      </button>
                    )}
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
