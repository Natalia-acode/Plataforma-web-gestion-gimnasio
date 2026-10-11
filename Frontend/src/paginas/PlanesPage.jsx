import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { pagosApi, planesApi, suscripcionesApi } from '../api/endpoints.js'
import { Tarjeta, Cargando, Mensaje, formatearMoneda } from '../componentes/comunes.jsx'

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
      <div className="encabezado-pagina">
        <div>
          <h1>Planes del gimnasio</h1>
          <p>Elige tu plan. La renovación automática es opcional: tú decides si activarla.</p>
        </div>
        <Link className="boton secundario" to="/pagos">
          Mis métodos de pago
        </Link>
      </div>

      <Mensaje tipo="error">{error}</Mensaje>
      <Mensaje tipo="exito">{exito}</Mensaje>

      <div className="grid-3">
        {planes.map((plan) => (
          <Tarjeta key={plan.id} titulo={plan.nombre}>
            <h3 style={{ fontSize: '1.8rem' }}>{formatearMoneda(plan.precio)}</h3>
            <p className="apagado">Duración: {plan.duracionDias} días</p>
            <p>{plan.descripcion}</p>
            <button
              className="boton bloque"
              onClick={() => setSeleccion((actual) => ({ ...actual, idPlan: plan.id }))}
              disabled={seleccion.idPlan === plan.id}
            >
              {seleccion.idPlan === plan.id ? 'Seleccionado' : 'Elegir este plan'}
            </button>
          </Tarjeta>
        ))}
      </div>

      {seleccion.idPlan && (
        <Tarjeta titulo="Confirmar contratación">
          <form onSubmit={contratar}>
            {metodos.length === 0 ? (
              <Mensaje tipo="info">
                No tienes métodos de pago registrados. Puedes contratar y pagar en efectivo en
                recepción, o <Link to="/pagos">registrar una tarjeta</Link> para el cobro automático.
              </Mensaje>
            ) : (
              <label>
                Tarjeta a usar
                <select
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
                </select>
              </label>
            )}

            <label style={{ display: 'flex', gap: 10, alignItems: 'center' }}>
              <input
                type="checkbox"
                style={{ width: 'auto' }}
                checked={seleccion.renovacionAutomatica}
                onChange={(e) =>
                  setSeleccion((actual) => ({ ...actual, renovacionAutomatica: e.target.checked }))
                }
              />
              Activar renovación automática (puedes desactivarla cuando quieras)
            </label>

            <button className="boton" disabled={enviando}>
              {enviando ? 'Procesando…' : 'Contratar plan'}
            </button>
          </form>
        </Tarjeta>
      )}
    </div>
  )
}
