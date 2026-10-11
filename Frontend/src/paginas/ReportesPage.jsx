import { useEffect, useState } from 'react'
import { reportesApi } from '../api/endpoints.js'
import { getToken } from '../api/client.js'
import {
  Tarjeta,
  Cargando,
  Mensaje,
  Etiqueta,
  etiquetaEstado,
  formatearMoneda,
  formatearFechaHora,
  hoyIso,
} from '../componentes/comunes.jsx'

export default function ReportesPage() {
  const hoy = hoyIso()
  const haceUnMes = (() => {
    const fecha = new Date()
    fecha.setMonth(fecha.getMonth() - 1)
    return fecha.toISOString().slice(0, 10)
  })()

  const [desde, setDesde] = useState(haceUnMes)
  const [hasta, setHasta] = useState(hoy)
  const [reporte, setReporte] = useState(null)
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState('')

  async function cargar(fechaDesde = desde, fechaHasta = hasta) {
    setCargando(true)
    setError('')
    try {
      setReporte(await reportesApi.generar(fechaDesde, fechaHasta))
    } catch (excepcion) {
      setError(excepcion.message)
    } finally {
      setCargando(false)
    }
  }

  useEffect(() => {
    cargar()
  }, [])

  async function exportar() {
    const respuesta = await fetch(reportesApi.urlExportar(desde, hasta), {
      headers: { Authorization: `Bearer ${getToken()}` },
    })
    if (!respuesta.ok) {
      setError('No fue posible exportar el CSV')
      return
    }
    const blob = await respuesta.blob()
    const enlace = document.createElement('a')
    enlace.href = URL.createObjectURL(blob)
    enlace.download = `pagos_${desde}_${hasta}.csv`
    enlace.click()
    URL.revokeObjectURL(enlace.href)
  }

  return (
    <div>
      <div className="encabezado-pagina">
        <div>
          <h1>Reportes</h1>
          <p>Ingresos, ocupación y uso de clases en el periodo seleccionado.</p>
        </div>
        <button className="boton secundario" onClick={exportar}>
          Exportar pagos (CSV)
        </button>
      </div>

      <Mensaje tipo="error">{error}</Mensaje>

      <Tarjeta titulo="Periodo">
        <div className="fila-botones">
          <label style={{ marginBottom: 0 }}>
            Desde
            <input type="date" value={desde} onChange={(e) => setDesde(e.target.value)} />
          </label>
          <label style={{ marginBottom: 0 }}>
            Hasta
            <input type="date" value={hasta} onChange={(e) => setHasta(e.target.value)} />
          </label>
          <button className="boton" onClick={() => cargar()}>
            Generar reporte
          </button>
        </div>
      </Tarjeta>

      {cargando ? (
        <Cargando>Generando reporte…</Cargando>
      ) : (
        reporte && (
          <>
            <div className="grid-3">
              <div className="kpi">
                <strong>{formatearMoneda(reporte.ingresosTotales)}</strong>
                <span>Ingresos aprobados ({reporte.totalIngresos} pagos)</span>
              </div>
              <div className="kpi">
                <strong>{reporte.checkIns}</strong>
                <span>Ingresos al gimnasio</span>
              </div>
              <div className="kpi">
                <strong>{reporte.suscripcionesNuevas}</strong>
                <span>Suscripciones nuevas</span>
              </div>
              <div className="kpi">
                <strong>{reporte.suscripcionesCanceladas}</strong>
                <span>Suscripciones canceladas</span>
              </div>
              <div className="kpi">
                <strong>{reporte.reservasTomadas}</strong>
                <span>Reservas de clase</span>
              </div>
              <div className="kpi">
                <strong>{reporte.inasistencias}</strong>
                <span>Inasistencias a clase</span>
              </div>
            </div>

            <Tarjeta titulo="Pagos del periodo">
              {reporte.pagosDelPeriodo.length === 0 ? (
                <p className="apagado">No hubo pagos en el periodo.</p>
              ) : (
                <table className="tabla">
                  <thead>
                    <tr>
                      <th>Fecha</th>
                      <th>Plan</th>
                      <th>Tipo</th>
                      <th>Monto</th>
                      <th>Estado</th>
                      <th>Factura</th>
                    </tr>
                  </thead>
                  <tbody>
                    {reporte.pagosDelPeriodo.map((pago) => (
                      <tr key={pago.id}>
                        <td>{formatearFechaHora(pago.fechaPago)}</td>
                        <td>{pago.planNombre}</td>
                        <td>{pago.tipo.replaceAll('_', ' ')}</td>
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
          </>
        )
      )}
    </div>
  )
}
