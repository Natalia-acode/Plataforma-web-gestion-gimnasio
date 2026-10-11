import { createContext, useContext, useMemo, useState } from 'react'
import { authApi } from '../api/endpoints.js'
import { getToken, setToken } from '../api/client.js'

const AuthContext = createContext(null)

const USUARIO_KEY = 'gimnasio_usuario'

function leerUsuario() {
  try {
    return JSON.parse(localStorage.getItem(USUARIO_KEY))
  } catch {
    return null
  }
}

export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(leerUsuario())
  const [token, setTokenState] = useState(getToken())

  async function login(email, contrasena) {
    const respuesta = await authApi.login(email, contrasena)
    setToken(respuesta.token)
    localStorage.setItem(USUARIO_KEY, JSON.stringify(respuesta.usuario))
    setTokenState(respuesta.token)
    setUsuario(respuesta.usuario)
    return respuesta.usuario
  }

  async function registro(datos) {
    const respuesta = await authApi.registro(datos)
    setToken(respuesta.token)
    localStorage.setItem(USUARIO_KEY, JSON.stringify(respuesta.usuario))
    setTokenState(respuesta.token)
    setUsuario(respuesta.usuario)
    return respuesta.usuario
  }

  function logout() {
    setToken(null)
    localStorage.removeItem(USUARIO_KEY)
    setTokenState(null)
    setUsuario(null)
  }

  const valor = useMemo(
    () => ({
      usuario,
      token,
      autenticado: Boolean(token && usuario),
      rol: usuario ? usuario.rol : null,
      esCliente: usuario?.rol === 'CLIENTE',
      esStaff: ['RECEPCIONISTA', 'ENTRENADOR', 'ADMINISTRADOR'].includes(usuario?.rol),
      esAdministrador: usuario?.rol === 'ADMINISTRADOR',
      login,
      registro,
      logout,
    }),
    [usuario, token],
  )

  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const contexto = useContext(AuthContext)
  if (!contexto) {
    throw new Error('useAuth debe usarse dentro de AuthProvider')
  }
  return contexto
}
