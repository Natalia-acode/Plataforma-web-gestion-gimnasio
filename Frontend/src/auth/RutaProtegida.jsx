import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from './AuthContext.jsx'

export function RutaProtegida({ children, roles }) {
  const { autenticado, rol } = useAuth()
  const ubicacion = useLocation()

  if (!autenticado) {
    return <Navigate to="/login" state={{ desde: ubicacion.pathname }} replace />
  }
  if (roles && !roles.includes(rol)) {
    return <Navigate to="/" replace />
  }
  return children
}
