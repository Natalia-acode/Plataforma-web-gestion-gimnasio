import { useEffect, useState } from 'react'
import { colaApi } from '../api/endpoints.js'
import { Tarjeta, Mensaje, Etiqueta } from '../componentes/comunes.jsx'

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
      <div className="encabezado-pagina">
        <div>
          <h1>Cola virtual</h1>
          <p>
            Sin filas físicas: pide tu turno desde el móvil y llega justo cuando te llamemos.
          </p>
        </div>
      </div>

      <Mensaje tipo="error">{error}</Mensaje>
      <Mensaje tipo="exito">{exito}</Mensaje>

      <div className="grid-2">
        <Tarjeta titulo="Mi turno">
          {cargando ? (
            <p className="cargando">Consultando…</p>
          ) : turno ? (
            <div className={`turno-destacado ${turno.estado === 'llamado' ? 'llamado' : ''}`}>
              <div className="numero">#{turno.posicion}</div>
              <Etiqueta color={turno.estado === 'llamado' ? 'amarillo' : 'azul'}>
                {turno.estado}
              </Etiqueta>
              {turno.estado === 'llamado' ? (
                <p style={{ marginTop: 12 }}>
                  ¡Es tu turno! Preséntate en recepción dentro de la ventana indicada.
                </p>
              ) : (
                <p style={{ marginTop: 12 }}>
                  Personas delante: <strong>{turno.personasDelante}</strong> · Espera estimada:{' '}
                  <strong>{turno.esperaEstimadaMin} min</strong>
                </p>
              )}
              <p className="apagado" style={{ fontSize: '0.8rem' }}>
                Tipo: {turno.tipo} · solicitado {new Date(turno.fechaSolicitud).toLocaleTimeString('es-CO')}
              </p>
              <button className="boton peligro pequeno" onClick={cancelar}>
                Cancelar mi turno
              </button>
            </div>
          ) : (
            <div>
              <p className="apagado">No tienes un turno activo.</p>
              <div className="fila-botones">
                <button className="boton" onClick={() => solicitar('ingreso')}>
                  Turno para ingreso
                </button>
                <button className="boton azul" onClick={() => solicitar('clase')}>
                  Turno para clase
                </button>
              </div>
            </div>
          )}
        </Tarjeta>

        <Tarjeta titulo="¿Cómo funciona?">
          <ul>
            <li>Solicita tu turno virtual sin acercarte a la recepción.</li>
            <li>Consulta tu posición y el tiempo estimado de espera en tiempo real.</li>
            <li>Recibirás una notificación cuando sea tu turno (ventana de 10 minutos).</li>
            <li>Si no puedes asistir, cancela tu turno y libera el lugar para otra persona.</li>
          </ul>
          <p className="apagado" style={{ fontSize: '0.83rem' }}>
            Un turno no atendido dentro de la ventana expira automáticamente y la fila avanza.
          </p>
        </Tarjeta>
      </div>
    </div>
  )
}
