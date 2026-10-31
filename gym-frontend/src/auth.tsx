import { createContext, ReactNode, useContext, useEffect, useState } from 'react'
import { setOnExpire, setToken } from './api'
import type { Rol, User, Usuario } from './api'

interface Sesion { user: User | null; login: (token: string, u: Usuario) => void; logout: () => void }
const Ctx = createContext<Sesion>(null as unknown as Sesion)
export const useAuth = () => useContext(Ctx)

// El backend devuelve el rol como CLIENTE (enum) y dentro del JWT como "cliente": se normaliza a minúsculas.
const normalizar = (u: Usuario): User => ({ ...u, rol: String(u.rol).toLowerCase() as Rol })

export const inicioDe: Record<Rol, string> = {
  cliente: '/app/membresia',
  recepcionista: '/app/recepcion',
  entrenador: '/app/mis-clases',
  administrador: '/app/reportes',
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(() => {
    try { return JSON.parse(localStorage.getItem('gym_user') ?? 'null') } catch { return null }
  })
  const logout = () => { setToken(null); localStorage.removeItem('gym_user'); setUser(null) }
  const login = (token: string, u: Usuario) => {
    const n = normalizar(u)
    setToken(token); localStorage.setItem('gym_user', JSON.stringify(n)); setUser(n)
  }
  useEffect(() => { setOnExpire(logout) }, [])
  return <Ctx.Provider value={{ user, login, logout }}>{children}</Ctx.Provider>
}
