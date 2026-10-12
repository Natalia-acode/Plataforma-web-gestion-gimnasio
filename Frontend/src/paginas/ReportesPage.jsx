import { useEffect, useState } from 'react'
import { Button, Card, Col, Form, Row, Table } from 'react-bootstrap'
import { reportesApi } from '../api/endpoints.js'
import { getToken } from '../api/client.js'
import {
  Tarjeta,
  Cargando,
  Mensaje,
  Etiqueta,
  EncabezadoPagina,
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

  const kpis = reporte
    ? [
        {
          valor: formatearMoneda(reporte.ingresosTotales),
          etiqueta: `Ingresos aprobados (${reporte.totalIngresos} pagos)`,
        },
        { valor: reporte.checkIns, etiqueta: 'Ingresos al gimnasio' },
        { valor: reporte.suscripcionesNuevas, etiqueta: 'Suscripciones nuevas' },
        { valor: reporte.suscripcionesCanceladas, etiqueta: 'Suscripciones canceladas' },
        { valor: reporte.reservasTomadas, etiqueta: 'Reservas de clase' },
        { valor: reporte.inasistencias, etiqueta: 'Inasistencias a clase' },
      ]
    : []

  return (
    <div>
      <EncabezadoPagina
        titulo="Reportes"
        subtitulo="Ingresos, ocupación y uso de clases en el periodo seleccionado."
      >
        <Button variant="outline-secondary" onClick={exportar}>
          Exportar pagos (CSV)
        </Button>
      </EncabezadoPagina>

      <Mensaje tipo="error">{error}</Mensaje>

      <Tarjeta titulo="Periodo" className="mb-4">
        <div className="d-flex flex-wrap align-items-end gap-3">
          <Form.Group controlId="reporteDesde" className="mb-0">
            <Form.Label className="mb-1">Desde</Form.Label>
            <Form.Control
              type="date"
              value={desde}
              onChange={(e) => setDesde(e.target.value)}
            />
          </Form.Group>
          <Form.Group controlId="reporteHasta" className="mb-0">
            <Form.Label className="mb-1">Hasta</Form.Label>
            <Form.Control
              type="date"
              value={hasta}
              onChange={(e) => setHasta(e.target.value)}
            />
          </Form.Group>
          <Button onClick={() => cargar()}>Generar reporte</Button>
        </div>
      </Tarjeta>

      {cargando ? (
        <Cargando>Generando reporte…</Cargando>
      ) : (
        reporte && (
          <>
            <Row className="g-3 mb-4">
              {kpis.map((kpi) => (
                <Col md={6} lg={4} key={kpi.etiqueta}>
                  <Card className="text-center shadow-sm h-100">
                    <Card.Body>
                      <div className="fs-3 fw-bold">{kpi.valor}</div>
                      <div className="text-secondary">{kpi.etiqueta}</div>
                    </Card.Body>
                  </Card>
                </Col>
              ))}
            </Row>

            <Tarjeta titulo="Pagos del periodo">
              {reporte.pagosDelPeriodo.length === 0 ? (
                <p className="text-secondary mb-0">No hubo pagos en el periodo.</p>
              ) : (
                <Table hover responsive className="align-middle mb-0">
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
                </Table>
              )}
            </Tarjeta>
          </>
        )
      )}
    </div>
  )
}
