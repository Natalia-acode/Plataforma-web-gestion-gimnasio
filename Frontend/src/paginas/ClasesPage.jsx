import { useEffect, useMemo, useState } from 'react'
import { clasesApi } from '../api/endpoints.js'
import {
  Tarjeta,
  Cargando,
  Mensaje,
  Etiqueta,
  etiquetaEstado,
  nombreDia,
  proximosDias,
} from '../componentes/comunes.jsx'
import { useAuth } from '../auth/AuthContext.jsx'

const DIAS_CLAVE = ['domingo', 'lunes', 'martes', 'miercoles', 'jueves', 'viernes', 'sabado']

export default function ClasesPage() {
  const { esCliente } = useAuth()
  const dias = useMemo(() => proximosDias(14), [])
  const [fecha, setFecha] = useState(dias[0])
  const [clases, setClases] = useState([])
  const [misReservas, setMisReservas] = useState([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState('')
  const [exito, setExito] = useState('')

  async function cargar() {
    try {
      const datos = await clasesApi.listar()
      setClases(datos)
      if (esCliente) {
        setMisReservas(await clasesApi.misReservas())
      }
    } catch (excepcion) {
      setError(excepcion.message)
    } finally {
      setCargando(false)
    }
  }

  useEffect(() => {
    cargar()
  }, [])

  const diaClave = DIAS_CLAVE[new Date(`${fecha}T12:00:00`).getDay()]

  const reservaDe = (claseId) =>
    misReservas.find(
      (reserva) => reserva.claseId === claseId && reserva.fecha === fecha && reserva.estado !== 'cancelada',
    )

  async function reservar(clase) {
    setError('')
    setExito('')
    try {
      const reserva = await clasesApi.reservar(clase.id, fecha)
      setExito(
        reserva.estado === 'en_espera'
          ? `La clase está llena: quedaste en lista de espera (posición ${reserva.posicionEspera}).`
          : `Reserva confirmada para ${clase.nombre} el ${fecha}.`,
      )
      cargar()
    } catch (excepcion) {
      setError(excepcion.message)
    }
  }

  async function cancelar(reserva) {
    setError('')
    setExito('')
    try {
      await clasesApi.cancelarReserva(reserva.id)
      setExito('Reserva cancelada. Si había lista de espera, se promovió al siguiente.')
      cargar()
    } catch (excepcion) {
      setError(excepcion.message)
    }
  }

  if (cargando) {
    return <Cargando>Cargando clases…</Cargando>
  }

  return (
    <div>
      <div className="encabezado-pagina">
        <div>
          <h1>Clases grupales</h1>
          <p>Reserva tu cupo con anticipación; si se llena, entras automáticamente a la lista de espera.</p>
        </div>
      </div>

      <Mensaje tipo="error">{error}</Mensaje>
      <Mensaje tipo="exito">{exito}</Mensaje>

      <Tarjeta titulo="Elige el día">
        <div className="fila-botones">
          {dias.slice(0, 7).map((dia) => (
            <button
              key={dia}
              className={`boton pequeno ${dia === fecha ? '' : 'secundario'}`}
              onClick={() => setFecha(dia)}
            >
              {nombreDia(dia)} {dia.slice(8)}
            </button>
          ))}
          <input
            type="date"
            style={{ width: 'auto' }}
            value={fecha}
            min={dias[0]}
            max={dias[dias.length - 1]}
            onChange={(e) => setFecha(e.target.value)}
          />
        </div>
      </Tarjeta>

      <div className="grid-3">
        {clases.map((clase) => {
          const esSuDia = clase.diaSemana === diaClave
          const reserva = reservaDe(clase.id)
          return (
            <Tarjeta
              key={clase.id}
              titulo={clase.nombre}
              extra={
                esSuDia ? (
                  <Etiqueta color={clase.cuposDisponibles > 0 ? 'verde' : 'amarillo'}>
                    {clase.cuposDisponibles > 0
                      ? `${clase.cuposDisponibles} cupos`
                      : 'lista de espera'}
                  </Etiqueta>
                ) : (
                  <Etiqueta color="gris">no aplica este día</Etiqueta>
                )
              }
            >
              <p>
                {clase.horaInicio.slice(0, 5)} – {clase.horaFin.slice(0, 5)} · {clase.zonaNombre}
              </p>
              <p className="apagado">Entrenador: {clase.entrenadorNombre}</p>
              <p className="apagado">
                {clase.diaSemana} · cupo máximo {clase.cupoMaximo}
              </p>

              {esCliente && esSuDia && (
                <div className="fila-botones">
                  {reserva ? (
                    <>
                      <Etiqueta color={etiquetaEstado(reserva.estado)}>
                        {reserva.estado === 'en_espera'
                          ? `en espera (#${reserva.posicionEspera})`
                          : reserva.estado}
                      </Etiqueta>
                      <button className="boton pequeno peligro" onClick={() => cancelar(reserva)}>
                        Cancelar reserva
                      </button>
                    </>
                  ) : (
                    <button className="boton pequeno" onClick={() => reservar(clase)}>
                      Reservar para {fecha}
                    </button>
                  )}
                </div>
              )}
            </Tarjeta>
          )
        })}
      </div>

      {esCliente && misReservas.length > 0 && (
        <Tarjeta titulo="Mis reservas vigentes">
          <table className="tabla">
            <thead>
              <tr>
                <th>Clase</th>
                <th>Fecha</th>
                <th>Horario</th>
                <th>Estado</th>
              </tr>
            </thead>
            <tbody>
              {misReservas.map((reserva) => (
                <tr key={reserva.id}>
                  <td>{reserva.claseNombre}</td>
                  <td>{reserva.fecha}</td>
                  <td>{reserva.horaInicio.slice(0, 5)}</td>
                  <td>
                    <Etiqueta color={etiquetaEstado(reserva.estado)}>
                      {reserva.estado === 'en_espera'
                        ? `en espera (#${reserva.posicionEspera})`
                        : reserva.estado}
                    </Etiqueta>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </Tarjeta>
      )}
    </div>
  )
}
