import { Navigate, Route, Routes } from 'react-router-dom'
import Layout from './layout/Layout.jsx'
import { RutaProtegida } from './auth/RutaProtegida.jsx'
import LoginPage from './paginas/LoginPage.jsx'
import DashboardPage from './paginas/DashboardPage.jsx'
import PlanesPage from './paginas/PlanesPage.jsx'
import SuscripcionPage from './paginas/SuscripcionPage.jsx'
import PagosPage from './paginas/PagosPage.jsx'
import ClasesPage from './paginas/ClasesPage.jsx'
import ColaPage from './paginas/ColaPage.jsx'
import RecepcionPage from './paginas/RecepcionPage.jsx'
import ColaGestionPage from './paginas/ColaGestionPage.jsx'
import UsuariosPage from './paginas/UsuariosPage.jsx'
import ReportesPage from './paginas/ReportesPage.jsx'

const STAFF = ['RECEPCIONISTA', 'ENTRENADOR', 'ADMINISTRADOR']

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />

      <Route
        path="/"
        element={
          <RutaProtegida>
            <Layout />
          </RutaProtegida>
        }
      >
        <Route index element={<DashboardPage />} />
        <Route path="planes" element={<PlanesPage />} />
        <Route path="suscripcion" element={<SuscripcionPage />} />
        <Route path="pagos" element={<PagosPage />} />
        <Route path="clases" element={<ClasesPage />} />
        <Route path="cola" element={<ColaPage />} />
        <Route
          path="recepcion"
          element={
            <RutaProtegida roles={STAFF}>
              <RecepcionPage />
            </RutaProtegida>
          }
        />
        <Route
          path="cola-gestion"
          element={
            <RutaProtegida roles={STAFF}>
              <ColaGestionPage />
            </RutaProtegida>
          }
        />
        <Route
          path="usuarios"
          element={
            <RutaProtegida roles={['ADMINISTRADOR']}>
              <UsuariosPage />
            </RutaProtegida>
          }
        />
        <Route
          path="reportes"
          element={
            <RutaProtegida roles={['ADMINISTRADOR']}>
              <ReportesPage />
            </RutaProtegida>
          }
        />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  )
}
