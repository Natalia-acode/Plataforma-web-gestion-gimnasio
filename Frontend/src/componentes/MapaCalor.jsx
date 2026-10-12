/**
 * Mapa de calor de afluencia: filas = días, columnas = horas.
 * La intensidad del color refleja la ocupación promedio histórica (CU-16).
 */
const DIAS = ['lunes', 'martes', 'miercoles', 'jueves', 'viernes', 'sabado', 'domingo']
const ETIQUETAS_DIA = ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom']

export default function MapaCalor({ celdas, horaDesde = 5, horaHasta = 22 }) {
  if (!celdas || celdas.length === 0) {
    return (
      <p className="text-secondary mb-0">
        Aún no hay datos históricos de afluencia. Se alimentará con los registros de ocupación.
      </p>
    )
  }

  const maximo = Math.max(...celdas.map((c) => c.ocupacionPromedio), 1)
  const busqueda = new Map(celdas.map((c) => [`${c.diaIndice}-${c.hora}`, c]))
  const horas = []
  for (let h = horaDesde; h <= horaHasta; h++) {
    horas.push(h)
  }

  function color(valor) {
    if (!valor) {
      return 'var(--bs-secondary-bg)'
    }
    const intensidad = Math.min(1, valor / maximo)
    return `rgba(25, 135, 84, ${0.15 + intensidad * 0.85})`
  }

  return (
    <div>
      <div className="mapa-calor-grid" style={{ gridTemplateColumns: `48px repeat(${horas.length}, 1fr)` }}>
        <div />
        {horas.map((hora) => (
          <div key={hora} className="mapa-hora">
            {hora}
          </div>
        ))}
        {DIAS.map((dia, indiceDia) => (
          <FilaDia
            key={dia}
            etiqueta={ETIQUETAS_DIA[indiceDia]}
            indiceDia={indiceDia}
            horas={horas}
            busqueda={busqueda}
            color={color}
          />
        ))}
      </div>
      <div className="mapa-leyenda">
        <span>Menos concurrido</span>
        <span className="escala" />
        <span>Más concurrido</span>
      </div>
    </div>
  )
}

function FilaDia({ etiqueta, indiceDia, horas, busqueda, color }) {
  return (
    <>
      <div className="mapa-dia">{etiqueta}</div>
      {horas.map((hora) => {
        const celda = busqueda.get(`${indiceDia}-${hora}`)
        const valor = celda ? celda.ocupacionPromedio : 0
        return (
          <div
            key={hora}
            className="mapa-celda"
            style={{ background: color(valor) }}
            title={
              celda
                ? `${etiqueta} ${hora}:00 — promedio ${valor.toFixed(0)} personas`
                : `${etiqueta} ${hora}:00 — sin datos`
            }
          />
        )
      })}
    </>
  )
}
