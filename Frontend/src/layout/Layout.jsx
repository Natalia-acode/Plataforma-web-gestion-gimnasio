import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext.jsx'

const ICONOS = {
  inicio: '🏠',
  clases: '📅',
  cola: '⏳',
  planes: '🏋️',
  suscripciones: '💳',
  pagos: '🧾',
  recepcion: '🛎️',
  colaGestion: '📋',
  usuarios: '👥',
  reportes: '📊',
}

export default function Layout() {
  const { usuario, esCliente, esStaff, esAdministrador, logout } = useAuth()
  const navegar = useNavigate()

  function cerrarSesion() {
    logout()
    navegar('/login')
  }

  return (
    <div className="layout">
      <aside className="sidebar">
        <div className="marca">
          <span className="marca-logo">💪</span>
          <div>
            <strong>Gimnasio</strong>
            <small>Gestión integral</small>
          </div>
        </div>

        <nav>
          <NavLink to="/" end>
            <span>{ICONOS.inicio}</span> Inicio
          </NavLink>
          <NavLink to="/clases">
            <span>{ICONOS.clases}</span> Clases
          </NavLink>

          {esCliente && (
            <>
              <NavLink to="/cola">
                <span>{ICONOS.cola}</span> Cola virtual
              </NavLink>
              <NavLink to="/planes">
                <span>{ICONOS.planes}</span> Planes
              </NavLink>
              <NavLink to="/suscripcion">
                <span>{ICONOS.suscripciones}</span> Mi suscripción
              </NavLink>
              <NavLink to="/pagos">
                <span>{ICONOS.pagos}</span> Pagos
              </NavLink>
            </>
          )}

          {esStaff && (
            <NavLink to="/recepcion">
              <span>{ICONOS.recepcion}</span> Recepción
            </NavLink>
          )}
          {esStaff && (
            <NavLink to="/cola-gestion">
              <span>{ICONOS.colaGestion}</span> Cola (staff)
            </NavLink>
          )}
          {esAdministrador && (
            <>
              <NavLink to="/usuarios">
                <span>{ICONOS.usuarios}</span> Usuarios
              </NavLink>
              <NavLink to="/reportes">
                <span>{ICONOS.reportes}</span> Reportes
              </NavLink>
            </>
          )}
        </nav>

        <div className="sesion">
          <div className="sesion-datos">
            <strong>{usuario?.nombreCompleto}</strong>
            <small>{usuario?.rol?.toLowerCase()}</small>
          </div>
          <button className="boton secundario" onClick={cerrarSesion}>
            Salir
          </button>
        </div>
      </aside>

      <main className="contenido">
        <Outlet />
      </main>
    </div>
  )
}
