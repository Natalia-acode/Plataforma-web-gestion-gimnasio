import { useEffect, useState } from 'react'
import { usuariosApi } from '../api/endpoints.js'
import { Tarjeta, Cargando, Mensaje, Etiqueta, etiquetaEstado } from '../componentes/comunes.jsx'

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
      <div className="encabezado-pagina">
        <div>
          <h1>Usuarios</h1>
          <p>Administra cuentas, roles y suspensiones del personal y de los clientes.</p>
        </div>
      </div>

      <Mensaje tipo="error">{error}</Mensaje>
      <Mensaje tipo="exito">{exito}</Mensaje>

      <Tarjeta titulo="Crear cuenta de personal">
        <form onSubmit={crearStaff} className="grid-2">
          <div>
            <label>
              Nombre
              <input
                value={nuevoStaff.nombre}
                onChange={(e) => setNuevoStaff({ ...nuevoStaff, nombre: e.target.value })}
                required
              />
            </label>
            <label>
              Apellidos
              <input
                value={nuevoStaff.apellidos}
                onChange={(e) => setNuevoStaff({ ...nuevoStaff, apellidos: e.target.value })}
                required
              />
            </label>
          </div>
          <div>
            <label>
              Correo
              <input
                type="email"
                value={nuevoStaff.email}
                onChange={(e) => setNuevoStaff({ ...nuevoStaff, email: e.target.value })}
                required
              />
            </label>
            <label>
              Contraseña temporal
              <input
                type="password"
                minLength={8}
                value={nuevoStaff.contrasena}
                onChange={(e) => setNuevoStaff({ ...nuevoStaff, contrasena: e.target.value })}
                required
              />
            </label>
            <label>
              Rol
              <select
                value={nuevoStaff.rol}
                onChange={(e) => setNuevoStaff({ ...nuevoStaff, rol: e.target.value })}
              >
                <option value="recepcionista">Recepcionista</option>
                <option value="entrenador">Entrenador</option>
                <option value="administrador">Administrador</option>
              </select>
            </label>
          </div>
          <div style={{ gridColumn: '1 / -1' }}>
            <button className="boton">Crear cuenta</button>
          </div>
        </form>
      </Tarjeta>

      <Tarjeta titulo={`Listado de usuarios (${usuarios.length})`}>
        <table className="tabla">
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
                  <select
                    value={usuario.rol}
                    onChange={(e) => cambiarRol(usuario, e.target.value)}
                    style={{ width: 'auto' }}
                  >
                    {ROLES.map((rol) => (
                      <option key={rol} value={rol}>
                        {rol.toLowerCase()}
                      </option>
                    ))}
                  </select>
                </td>
                <td>
                  <Etiqueta color={etiquetaEstado(usuario.estado)}>{usuario.estado}</Etiqueta>
                </td>
                <td>
                  {usuario.estado === 'activo' ? (
                    <button className="boton pequeno peligro" onClick={() => suspender(usuario)}>
                      Suspender
                    </button>
                  ) : (
                    <button className="boton pequeno" onClick={() => reactivar(usuario)}>
                      Reactivar
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </Tarjeta>
    </div>
  )
}
