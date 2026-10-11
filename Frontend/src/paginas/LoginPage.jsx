import { useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
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
    <div className="pantalla-login">
      <div className="login-caja">
        <div className="login-logo">💪</div>
        <h1>Gestión de Gimnasio</h1>
        <p>Entrena con información: ocupación en vivo, reservas y cola virtual.</p>

        <div className="pestanas">
          <button
            type="button"
            className={modo === 'login' ? 'activa' : ''}
            onClick={() => setModo('login')}
          >
            Iniciar sesión
          </button>
          <button
            type="button"
            className={modo === 'registro' ? 'activa' : ''}
            onClick={() => setModo('registro')}
          >
            Crear cuenta
          </button>
        </div>

        <form onSubmit={enviar}>
          {modo === 'registro' && (
            <>
              <label>
                Nombre
                <input
                  value={formulario.nombre}
                  onChange={(e) => actualizar('nombre', e.target.value)}
                  required
                />
              </label>
              <label>
                Apellidos
                <input
                  value={formulario.apellidos}
                  onChange={(e) => actualizar('apellidos', e.target.value)}
                  required
                />
              </label>
              <label>
                Teléfono (opcional)
                <input
                  value={formulario.telefono}
                  onChange={(e) => actualizar('telefono', e.target.value)}
                />
              </label>
            </>
          )}

          <label>
            Correo electrónico
            <input
              type="email"
              value={formulario.email}
              onChange={(e) => actualizar('email', e.target.value)}
              required
            />
          </label>
          <label>
            Contraseña
            <input
              type="password"
              value={formulario.contrasena}
              onChange={(e) => actualizar('contrasena', e.target.value)}
              minLength={8}
              required
            />
          </label>

          <Mensaje tipo="error">{error}</Mensaje>

          <button className="boton bloque" disabled={enviando}>
            {enviando ? 'Procesando…' : modo === 'login' ? 'Entrar' : 'Crear cuenta y entrar'}
          </button>
        </form>
      </div>
    </div>
  )
}
