import { useEffect, useState } from 'react'
import { Button, Col, Form, Row, Table } from 'react-bootstrap'
import { pagosApi } from '../api/endpoints.js'
import {
  Tarjeta,
  Cargando,
  Mensaje,
  Etiqueta,
  EncabezadoPagina,
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
      setExito(
        'Tarjeta verificada y guardada de forma segura (solo se conserva el token y los últimos 4 dígitos).',
      )
      setFormulario({
        tipo: 'tarjeta_credito',
        numeroTarjeta: '',
        titular: '',
        predeterminado: false,
      })
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
      <EncabezadoPagina
        titulo="Pagos y facturación"
        subtitulo="Transparencia total: consulta cada cobro, su factura y tus tarjetas registradas."
      />

      <Mensaje tipo="error">{error}</Mensaje>
      <Mensaje tipo="exito">{exito}</Mensaje>

      <Row className="g-4">
        <Col lg={5}>
          <Tarjeta titulo="Métodos de pago" className="h-100">
            {metodos.length === 0 ? (
              <p className="text-secondary">No tienes tarjetas registradas todavía.</p>
            ) : (
              <Table hover responsive className="align-middle">
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
                      <td className="text-end">
                        <div className="d-flex gap-2 justify-content-end">
                          {!metodo.predeterminado && (
                            <Button
                              size="sm"
                              variant="outline-secondary"
                              onClick={() => predeterminar(metodo.id)}
                            >
                              Predeterminar
                            </Button>
                          )}
                          <Button size="sm" variant="danger" onClick={() => eliminar(metodo.id)}>
                            Eliminar
                          </Button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </Table>
            )}

            <hr />
            <h2 className="h5">Registrar tarjeta</h2>
            <p className="text-secondary small">
              Los datos completos viajan solo a la pasarela de pago (PCI-DSS); el sistema guarda
              únicamente un token y los últimos cuatro dígitos.
            </p>
            <Form onSubmit={registrar}>
              <Form.Group className="mb-3" controlId="tipoTarjeta">
                <Form.Label>Tipo</Form.Label>
                <Form.Select
                  value={formulario.tipo}
                  onChange={(e) => setFormulario({ ...formulario, tipo: e.target.value })}
                >
                  <option value="tarjeta_credito">Tarjeta de crédito</option>
                  <option value="tarjeta_debito">Tarjeta de débito</option>
                </Form.Select>
              </Form.Group>
              <Form.Group className="mb-3" controlId="numeroTarjeta">
                <Form.Label>Número de tarjeta</Form.Label>
                <Form.Control
                  inputMode="numeric"
                  pattern="\d{13,19}"
                  maxLength={19}
                  placeholder="4111111111111111"
                  value={formulario.numeroTarjeta}
                  onChange={(e) =>
                    setFormulario({
                      ...formulario,
                      numeroTarjeta: e.target.value.replace(/\D/g, ''),
                    })
                  }
                  required
                />
              </Form.Group>
              <Form.Group className="mb-3" controlId="titularTarjeta">
                <Form.Label>Titular</Form.Label>
                <Form.Control
                  value={formulario.titular}
                  onChange={(e) => setFormulario({ ...formulario, titular: e.target.value })}
                  required
                />
              </Form.Group>
              <Form.Check
                className="mb-3"
                type="checkbox"
                id="tarjetaPredeterminada"
                label="Usar como tarjeta predeterminada"
                checked={formulario.predeterminado}
                onChange={(e) => setFormulario({ ...formulario, predeterminado: e.target.checked })}
              />
              <Button type="submit">Guardar tarjeta</Button>
            </Form>
          </Tarjeta>
        </Col>

        <Col lg={7}>
          <Tarjeta titulo="Historial de pagos" className="h-100">
            {historial.length === 0 ? (
              <p className="text-secondary mb-0">Aún no tienes pagos registrados.</p>
            ) : (
              <Table hover responsive className="align-middle mb-0">
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
              </Table>
            )}
          </Tarjeta>
        </Col>
      </Row>
    </div>
  )
}
