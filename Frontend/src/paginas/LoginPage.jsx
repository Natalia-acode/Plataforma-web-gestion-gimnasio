import { useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { Button, Card, Form, Nav } from 'react-bootstrap'
import { useAuth } from '../auth/AuthContext.jsx'
import { Mensaje } from '../componentes/comunes.jsx'

export default function LoginPage() {
  const { login, registro } = useAuth()
  const navegar = useNavigate()
  const ubicacion = useLocation()
  const [modo, setModo] = useState('login')
  const [error, setError] = useState('')
  const [enviando, setEnviando] = useState(false)
  const [formulario, setFormulario] = useState({
    email: '',
    contrasena: '',
    nombre: '',
    apellidos: '',
    telefono: '',
  })

  function actualizar(campo, valor) {
    setFormulario((actual) => ({ ...actual, [campo]: valor }))
  }

  async function enviar(evento) {
    evento.preventDefault()
    setError('')
    setEnviando(true)
    try {
      if (modo === 'login') {
        await login(formulario.email, formulario.contrasena)
      } else {
        await registro({
          nombre: formulario.nombre,
          apellidos: formulario.apellidos,
          email: formulario.email,
          contrasena: formulario.contrasena,
          telefono: formulario.telefono || null,
        })
      }
      navegar(ubicacion.state?.desde || '/', { replace: true })
    } catch (excepcion) {
      setError(excepcion.message)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="min-vh-100 d-flex align-items-center justify-content-center bg-body-tertiary py-5">
      <Card className="shadow-sm" style={{ width: '100%', maxWidth: 460 }}>
        <Card.Body className="p-4">
          <div className="text-center mb-4">
            <div className="fs-1">💪</div>
            <h1 className="h3 mb-1">Gestión de Gimnasio</h1>
            <p className="text-secondary mb-0">
              Entrena con información: ocupación en vivo, reservas y cola virtual.
            </p>
          </div>

          <Nav
            variant="pills"
            className="nav-fill mb-4 bg-body-tertiary rounded p-1"
            activeKey={modo}
            onSelect={(clave) => clave && setModo(clave)}
          >
            <Nav.Item>
              <Nav.Link eventKey="login">Iniciar sesión</Nav.Link>
            </Nav.Item>
            <Nav.Item>
              <Nav.Link eventKey="registro">Crear cuenta</Nav.Link>
            </Nav.Item>
          </Nav>

          <Form onSubmit={enviar}>
            {modo === 'registro' && (
              <>
                <Form.Group className="mb-3" controlId="registroNombre">
                  <Form.Label>Nombre</Form.Label>
                  <Form.Control
                    value={formulario.nombre}
                    onChange={(e) => actualizar('nombre', e.target.value)}
                    required
                  />
                </Form.Group>
                <Form.Group className="mb-3" controlId="registroApellidos">
                  <Form.Label>Apellidos</Form.Label>
                  <Form.Control
                    value={formulario.apellidos}
                    onChange={(e) => actualizar('apellidos', e.target.value)}
                    required
                  />
                </Form.Group>
                <Form.Group className="mb-3" controlId="registroTelefono">
                  <Form.Label>Teléfono (opcional)</Form.Label>
                  <Form.Control
                    value={formulario.telefono}
                    onChange={(e) => actualizar('telefono', e.target.value)}
                  />
                </Form.Group>
              </>
            )}

            <Form.Group className="mb-3" controlId="loginEmail">
              <Form.Label>Correo electrónico</Form.Label>
              <Form.Control
                type="email"
                value={formulario.email}
                onChange={(e) => actualizar('email', e.target.value)}
                required
              />
            </Form.Group>
            <Form.Group className="mb-3" controlId="loginContrasena">
              <Form.Label>Contraseña</Form.Label>
              <Form.Control
                type="password"
                value={formulario.contrasena}
                onChange={(e) => actualizar('contrasena', e.target.value)}
                minLength={8}
                required
              />
            </Form.Group>

            <Mensaje tipo="error">{error}</Mensaje>

            <Button type="submit" className="w-100" disabled={enviando}>
              {enviando ? 'Procesando…' : modo === 'login' ? 'Entrar' : 'Crear cuenta y entrar'}
            </Button>
          </Form>
        </Card.Body>
      </Card>
    </div>
  )
}
