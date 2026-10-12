import { useEffect, useState } from 'react'
import { Button, Col, Form, Row, Table } from 'react-bootstrap'
import { usuariosApi } from '../api/endpoints.js'
import {
  Tarjeta,
  Cargando,
  Mensaje,
  Etiqueta,
  EncabezadoPagina,
  etiquetaEstado,
} from '../componentes/comunes.jsx'

const ROLES = ['CLIENTE', 'RECEPCIONISTA', 'ENTRENADOR', 'ADMINISTRADOR']

export default function UsuariosPage() {
  const [usuarios, setUsuarios] = useState([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState('')
  const [exito, setExito] = useState('')
  const [nuevoStaff, setNuevoStaff] = useState({
    nombre: '',
    apellidos: '',
    email: '',
    contrasena: '',
    rol: 'recepcionista',
  })

  async function cargar() {
    try {
      setUsuarios(await usuariosApi.listar())
    } catch (excepcion) {
      setError(excepcion.message)
    } finally {
      setCargando(false)
    }
  }

  useEffect(() => {
    cargar()
  }, [])

  async function suspender(usuario) {
    setError('')
    setExito('')
    try {
      await usuariosApi.suspender(usuario.id)
      setExito(`Cuenta suspendida: ${usuario.nombreCompleto}.`)
      cargar()
    } catch (excepcion) {
      setError(excepcion.message)
    }
  }

  async function reactivar(usuario) {
    setError('')
    setExito('')
    try {
      await usuariosApi.reactivar(usuario.id)
      setExito(`Cuenta reactivada: ${usuario.nombreCompleto}.`)
      cargar()
    } catch (excepcion) {
      setError(excepcion.message)
    }
  }

  async function cambiarRol(usuario, rol) {
    setError('')
    setExito('')
    try {
      await usuariosApi.asignarRol(usuario.id, rol.toLowerCase())
      setExito(`Rol actualizado para ${usuario.nombreCompleto}: ${rol.toLowerCase()}.`)
      cargar()
    } catch (excepcion) {
      setError(excepcion.message)
    }
  }

  async function crearStaff(evento) {
    evento.preventDefault()
    setError('')
    setExito('')
    try {
      const creado = await usuariosApi.crearStaff(nuevoStaff)
      setExito(`Cuenta de ${nuevoStaff.rol} creada: ${creado.nombreCompleto}.`)
      setNuevoStaff({ nombre: '', apellidos: '', email: '', contrasena: '', rol: 'recepcionista' })
      cargar()
    } catch (excepcion) {
      setError(excepcion.message)
    }
  }

  if (cargando) {
    return <Cargando>Cargando usuarios…</Cargando>
  }

  return (
    <div>
      <EncabezadoPagina
        titulo="Usuarios"
        subtitulo="Administra cuentas, roles y suspensiones del personal y de los clientes."
      />

      <Mensaje tipo="error">{error}</Mensaje>
      <Mensaje tipo="exito">{exito}</Mensaje>

      <Tarjeta titulo="Crear cuenta de personal" className="mb-4">
        <Form onSubmit={crearStaff}>
          <Row className="g-3">
            <Col md={6}>
              <Form.Group controlId="staffNombre">
                <Form.Label>Nombre</Form.Label>
                <Form.Control
                  value={nuevoStaff.nombre}
                  onChange={(e) => setNuevoStaff({ ...nuevoStaff, nombre: e.target.value })}
                  required
                />
              </Form.Group>
            </Col>
            <Col md={6}>
              <Form.Group controlId="staffApellidos">
                <Form.Label>Apellidos</Form.Label>
                <Form.Control
                  value={nuevoStaff.apellidos}
                  onChange={(e) => setNuevoStaff({ ...nuevoStaff, apellidos: e.target.value })}
                  required
                />
              </Form.Group>
            </Col>
            <Col md={6}>
              <Form.Group controlId="staffEmail">
                <Form.Label>Correo</Form.Label>
                <Form.Control
                  type="email"
                  value={nuevoStaff.email}
                  onChange={(e) => setNuevoStaff({ ...nuevoStaff, email: e.target.value })}
                  required
                />
              </Form.Group>
            </Col>
            <Col md={6}>
              <Form.Group controlId="staffContrasena">
                <Form.Label>Contraseña temporal</Form.Label>
                <Form.Control
                  type="password"
                  minLength={8}
                  value={nuevoStaff.contrasena}
                  onChange={(e) => setNuevoStaff({ ...nuevoStaff, contrasena: e.target.value })}
                  required
                />
              </Form.Group>
            </Col>
            <Col md={6}>
              <Form.Group controlId="staffRol">
                <Form.Label>Rol</Form.Label>
                <Form.Select
                  value={nuevoStaff.rol}
                  onChange={(e) => setNuevoStaff({ ...nuevoStaff, rol: e.target.value })}
                >
                  <option value="recepcionista">Recepcionista</option>
                  <option value="entrenador">Entrenador</option>
                  <option value="administrador">Administrador</option>
                </Form.Select>
              </Form.Group>
            </Col>
          </Row>
          <Button type="submit" className="mt-3">
            Crear cuenta
          </Button>
        </Form>
      </Tarjeta>

      <Tarjeta titulo={`Listado de usuarios (${usuarios.length})`}>
        <Table hover responsive className="align-middle mb-0">
          <thead>
            <tr>
              <th>Nombre</th>
              <th>Correo</th>
              <th>Rol</th>
              <th>Estado</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {usuarios.map((usuario) => (
              <tr key={usuario.id}>
                <td>{usuario.nombreCompleto}</td>
                <td>{usuario.email}</td>
                <td>
                  <Form.Select
                    size="sm"
                    style={{ width: 'auto' }}
                    value={usuario.rol}
                    onChange={(e) => cambiarRol(usuario, e.target.value)}
                  >
                    {ROLES.map((rol) => (
                      <option key={rol} value={rol}>
                        {rol.toLowerCase()}
                      </option>
                    ))}
                  </Form.Select>
                </td>
                <td>
                  <Etiqueta color={etiquetaEstado(usuario.estado)}>{usuario.estado}</Etiqueta>
                </td>
                <td>
                  {usuario.estado === 'activo' ? (
                    <Button size="sm" variant="danger" onClick={() => suspender(usuario)}>
                      Suspender
                    </Button>
                  ) : (
                    <Button size="sm" onClick={() => reactivar(usuario)}>
                      Reactivar
                    </Button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </Table>
      </Tarjeta>
    </div>
  )
}
