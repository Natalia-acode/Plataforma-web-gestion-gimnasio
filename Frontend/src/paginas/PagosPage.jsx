import { useEffect, useState } from 'react'
import { pagosApi } from '../api/endpoints.js'
import {
  Tarjeta,
  Cargando,
  Mensaje,
  Etiqueta,
  etiquetaEstado,
  formatearMoneda,
  formatearFechaHora,
} from '../componentes/comunes.jsx'

export default function PagosPage() {
  const [metodos, setMetodos] = useState([])
  const [historial, setHistorial] = useState([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState('')
  const [exito, setExito] = useState('')
  const [formulario, setFormulario] = useState({
    tipo: 'tarjeta_credito',
    numeroTarjeta: '',
    titular: '',
    predeterminado: false,
  })

  async function cargar() {
    try {
      const [datosMetodos, datosHistorial] = await Promise.all([
        pagosApi.metodos(),
        pagosApi.historial(),
      ])
      setMetodos(datosMetodos)
      setHistorial(datosHistorial)
    } catch (excepcion) {
      setError(excepcion.message)
    } finally {
      setCargando(false)
    }
  }

  useEffect(() => {
    cargar()
  }, [])

  async function registrar(evento) {
    evento.preventDefault()
    setError('')
    setExito('')
    try {
      await pagosApi.registrarMetodo(formulario)
      setExito('Tarjeta verificada y guardada de forma segura (solo se conserva el token y los últimos 4 dígitos).')
      setFormulario({ tipo: 'tarjeta_credito', numeroTarjeta: '', titular: '', predeterminado: false })
      cargar()
    } catch (excepcion) {
      setError(excepcion.message)
    }
  }

  async function predeterminar(id) {
    try {
      await pagosApi.predeterminar(id)
      cargar()
    } catch (excepcion) {
      setError(excepcion.message)
    }
  }

  async function eliminar(id) {
    setError('')
    try {
      await pagosApi.eliminarMetodo(id)
      setExito('Método de pago eliminado.')
      cargar()
    } catch (excepcion) {
      setError(excepcion.message)
    }
  }

  if (cargando) {
    return <Cargando>Cargando tus pagos…</Cargando>
  }

  return (
    <div>
      <div className="encabezado-pagina">
        <div>
          <h1>Pagos y facturación</h1>
          <p>Transparencia total: consulta cada cobro, su factura y tus tarjetas registradas.</p>
        </div>
      </div>

      <Mensaje tipo="error">{error}</Mensaje>
      <Mensaje tipo="exito">{exito}</Mensaje>

      <div className="grid-2">
        <Tarjeta titulo="Métodos de pago">
          {metodos.length === 0 ? (
            <p className="apagado">No tienes tarjetas registradas todavía.</p>
          ) : (
            <table className="tabla">
              <thead>
                <tr>
                  <th>Tarjeta</th>
                  <th>Titular</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {metodos.map((metodo) => (
                  <tr key={metodo.id}>
                    <td>
                      {metodo.tipo.replace('_', ' ')} •••• {metodo.ultimosCuatro}{' '}
                      {metodo.predeterminado && <Etiqueta color="verde">predeterminada</Etiqueta>}
                    </td>
                    <td>{metodo.titular}</td>
                    <td className="fila-botones">
                      {!metodo.predeterminado && (
                        <button
                          className="boton pequeno secundario"
                          onClick={() => predeterminar(metodo.id)}
                        >
                          Predeterminar
                        </button>
                      )}
                      <button className="boton pequeno peligro" onClick={() => eliminar(metodo.id)}>
                        Eliminar
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}

          <hr style={{ borderColor: 'var(--borde)', margin: '18px 0' }} />
          <h2>Registrar tarjeta</h2>
          <p className="apagado" style={{ fontSize: '0.83rem' }}>
            Los datos completos viajan solo a la pasarela de pago (PCI-DSS); el sistema guarda
            únicamente un token y los últimos cuatro dígitos.
          </p>
          <form onSubmit={registrar}>
            <label>
              Tipo
              <select
                value={formulario.tipo}
                onChange={(e) => setFormulario({ ...formulario, tipo: e.target.value })}
              >
                <option value="tarjeta_credito">Tarjeta de crédito</option>
                <option value="tarjeta_debito">Tarjeta de débito</option>
              </select>
            </label>
            <label>
              Número de tarjeta
              <input
                inputMode="numeric"
                pattern="\d{13,19}"
                maxLength={19}
                placeholder="4111111111111111"
                value={formulario.numeroTarjeta}
                onChange={(e) =>
                  setFormulario({ ...formulario, numeroTarjeta: e.target.value.replace(/\D/g, '') })
                }
                required
              />
            </label>
            <label>
              Titular
              <input
                value={formulario.titular}
                onChange={(e) => setFormulario({ ...formulario, titular: e.target.value })}
                required
              />
            </label>
            <label style={{ display: 'flex', gap: 10, alignItems: 'center' }}>
              <input
                type="checkbox"
                style={{ width: 'auto' }}
                checked={formulario.predeterminado}
                onChange={(e) =>
                  setFormulario({ ...formulario, predeterminado: e.target.checked })
                }
              />
              Usar como tarjeta predeterminada
            </label>
            <button className="boton">Guardar tarjeta</button>
          </form>
        </Tarjeta>

        <Tarjeta titulo="Historial de pagos">
          {historial.length === 0 ? (
            <p className="apagado">Aún no tienes pagos registrados.</p>
          ) : (
            <table className="tabla">
              <thead>
                <tr>
                  <th>Fecha</th>
                  <th>Concepto</th>
                  <th>Monto</th>
                  <th>Estado</th>
                  <th>Factura</th>
                </tr>
              </thead>
              <tbody>
                {historial.map((pago) => (
                  <tr key={pago.id}>
                    <td>{formatearFechaHora(pago.fechaPago)}</td>
                    <td>
                      {pago.tipo.replaceAll('_', ' ')} · {pago.planNombre}
                      {pago.metodoResumen ? ` · ${pago.metodoResumen}` : ''}
                    </td>
                    <td>{formatearMoneda(pago.monto)}</td>
                    <td>
                      <Etiqueta color={etiquetaEstado(pago.estado)}>{pago.estado}</Etiqueta>
                    </td>
                    <td>{pago.numeroFactura || '—'}</td>
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
