import { ReactElement } from 'react'
import { Link, NavLink, Navigate, Outlet, Route, Routes } from 'react-router-dom'
import { useAuth, inicioDe } from './auth'
import type { Rol } from './api'
import { Home, Planes, ClasesPublicas, Login, Registro, Recuperar } from './pages/Public'
import { Membresia, Pagos, AforoCliente, Reservas, Cola, Ingreso, Avisos } from './pages/Cliente'
import { Recepcion, MisClases } from './pages/Staff'
import { Reportes, Usuarios, PlanesAdmin, ClasesAdmin, Configuracion } from './pages/Admin'

const NAV: Record<Rol, { to: string; label: string }[]> = {
  cliente: [
    { to: 'membresia', label: 'Mi membresía' }, { to: 'pagos', label: 'Pagos' },
    { to: 'aforo', label: 'Aforo y afluencia' }, { to: 'reservas', label: 'Clases' },
    { to: 'cola', label: 'Cola virtual' }, { to: 'ingreso', label: 'Mi ingreso' }, { to: 'avisos', label: 'Avisos' },
  ],
  recepcionista: [{ to: 'recepcion', label: 'Recepción' }],
  entrenador: [{ to: 'mis-clases', label: 'Mis clases' }],
  administrador: [
    { to: 'reportes', label: 'Reportes' }, { to: 'usuarios', label: 'Usuarios' }, { to: 'planes', label: 'Planes' },
    { to: 'clases', label: 'Clases' }, { to: 'configuracion', label: 'Configuración' }, { to: 'recepcion', label: 'Recepción' },
  ],
}

function PublicLayout() {
  const { user } = useAuth()
  return (
    <>
      <header className="site">
        <Link to="/" className="brand">GymFlex</Link>
        <nav>
          <NavLink to="/planes">Planes</NavLink>
          <NavLink to="/clases">Clases</NavLink>
          {user ? <Link className="btn" to="/app">Mi cuenta</Link> : <><NavLink to="/login">Entrar</NavLink><Link className="btn" to="/registro">Crear cuenta</Link></>}
        </nav>
      </header>
      <main><Outlet /></main>
    </>
  )
}

function Shell() {
  const { user, logout } = useAuth()
  if (!user) return <Navigate to="/login" replace />
  return (
    <div className="shell">
      <aside>
        <Link to="/" className="brand">GymFlex</Link>
        <nav aria-label="Menú principal">
          {NAV[user.rol].map((n) => <NavLink key={n.to} to={`/app/${n.to}`}>{n.label}</NavLink>)}
        </nav>
        <div className="who"><b>{user.nombre}</b><span>{user.rol}</span><button className="btn ghost" onClick={logout}>Cerrar sesión</button></div>
      </aside>
      <main><Outlet /></main>
    </div>
  )
}

function Only({ roles, children }: { roles: Rol[]; children: ReactElement }) {
  const { user } = useAuth()
  return user && roles.includes(user.rol) ? children : <Navigate to="/app" replace />
}

function Inicio() {
  const { user } = useAuth()
  return <Navigate to={user ? inicioDe[user.rol] : '/login'} replace />
}

const C: Rol[] = ['cliente']
const A: Rol[] = ['administrador']

export default function App() {
  return (
    <Routes>
      <Route element={<PublicLayout />}>
        <Route index element={<Home />} />
        <Route path="planes" element={<Planes />} />
        <Route path="clases" element={<ClasesPublicas />} />
        <Route path="login" element={<Login />} />
        <Route path="registro" element={<Registro />} />
        <Route path="recuperar" element={<Recuperar />} />
      </Route>
      <Route path="app" element={<Shell />}>
        <Route index element={<Inicio />} />
        <Route path="membresia" element={<Only roles={C}><Membresia /></Only>} />
        <Route path="pagos" element={<Only roles={C}><Pagos /></Only>} />
        <Route path="aforo" element={<Only roles={C}><AforoCliente /></Only>} />
        <Route path="reservas" element={<Only roles={C}><Reservas /></Only>} />
        <Route path="cola" element={<Only roles={C}><Cola /></Only>} />
        <Route path="ingreso" element={<Only roles={C}><Ingreso /></Only>} />
        <Route path="avisos" element={<Only roles={C}><Avisos /></Only>} />
        <Route path="recepcion" element={<Only roles={['recepcionista', 'administrador']}><Recepcion /></Only>} />
        <Route path="mis-clases" element={<Only roles={['entrenador']}><MisClases /></Only>} />
        <Route path="reportes" element={<Only roles={A}><Reportes /></Only>} />
        <Route path="usuarios" element={<Only roles={A}><Usuarios /></Only>} />
        <Route path="planes" element={<Only roles={A}><PlanesAdmin /></Only>} />
        <Route path="clases" element={<Only roles={A}><ClasesAdmin /></Only>} />
        <Route path="configuracion" element={<Only roles={A}><Configuracion /></Only>} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
