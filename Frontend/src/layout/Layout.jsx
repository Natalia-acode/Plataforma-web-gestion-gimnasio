import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom'
import { Container, Nav, Navbar, NavDropdown } from 'react-bootstrap'
import { useAuth } from '../auth/AuthContext.jsx'

export default function Layout() {
  const { usuario, esCliente, esStaff, esAdministrador, logout } = useAuth()
  const navegar = useNavigate()

  function cerrarSesion() {
    logout()
    navegar('/login')
  }

  return (
    <div className="d-flex flex-column min-vh-100 bg-body-tertiary">
      <Navbar bg="dark" data-bs-theme="dark" expand="lg" className="shadow-sm">
        <Container fluid="xl">
          <Navbar.Brand as={Link} to="/" className="fw-bold">
            💪 Gimnasio
          </Navbar.Brand>
          <Navbar.Toggle aria-controls="menu-principal" />
          <Navbar.Collapse id="menu-principal">
            <Nav className="me-auto">
              <NavLink to="/" end className="nav-link">
                <i className="bi bi-house-door me-1" aria-hidden="true" />
                Inicio
              </NavLink>
              <NavLink to="/clases" className="nav-link">
                <i className="bi bi-calendar3 me-1" aria-hidden="true" />
                Clases
              </NavLink>
              {esCliente && (
                <>
                  <NavLink to="/cola" className="nav-link">
                    <i className="bi bi-hourglass-split me-1" aria-hidden="true" />
                    Cola virtual
                  </NavLink>
                  <NavLink to="/planes" className="nav-link">
                    <i className="bi bi-card-list me-1" aria-hidden="true" />
                    Planes
                  </NavLink>
                  <NavLink to="/suscripcion" className="nav-link">
                    <i className="bi bi-credit-card me-1" aria-hidden="true" />
                    Mi suscripción
                  </NavLink>
                  <NavLink to="/pagos" className="nav-link">
                    <i className="bi bi-receipt me-1" aria-hidden="true" />
                    Pagos
                  </NavLink>
                </>
              )}
              {esStaff && (
                <>
                  <NavLink to="/recepcion" className="nav-link">
                    <i className="bi bi-person-badge me-1" aria-hidden="true" />
                    Recepción
                  </NavLink>
                  <NavLink to="/cola-gestion" className="nav-link">
                    <i className="bi bi-list-check me-1" aria-hidden="true" />
                    Cola (staff)
                  </NavLink>
                </>
              )}
              {esAdministrador && (
                <>
                  <NavLink to="/usuarios" className="nav-link">
                    <i className="bi bi-people me-1" aria-hidden="true" />
                    Usuarios
                  </NavLink>
                  <NavLink to="/reportes" className="nav-link">
                    <i className="bi bi-bar-chart me-1" aria-hidden="true" />
                    Reportes
                  </NavLink>
                </>
              )}
            </Nav>
            <NavDropdown
              align="end"
              id="menu-usuario"
              title={
                <span>
                  <i className="bi bi-person-circle me-1" aria-hidden="true" />
                  {usuario?.nombreCompleto}
                </span>
              }
            >
              <NavDropdown.Header>
                {usuario?.nombreCompleto}
                <div className="text-secondary small">{usuario?.rol?.toLowerCase()}</div>
              </NavDropdown.Header>
              <NavDropdown.Divider />
              <NavDropdown.Item onClick={cerrarSesion}>
                <i className="bi bi-box-arrow-right me-2" aria-hidden="true" />
                Cerrar sesión
              </NavDropdown.Item>
            </NavDropdown>
          </Navbar.Collapse>
        </Container>
      </Navbar>

      <Container fluid="xl" className="py-4 flex-grow-1">
        <Outlet />
      </Container>
    </div>
  )
}
