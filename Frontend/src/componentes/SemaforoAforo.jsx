/** Semáforo de ocupación en tiempo real (CU-15) — solución al problema de saturación. */
const NIVELES = {
  BAJO: { clase: 'semaforo-verde', texto: 'Baja ocupación', icono: '🟢' },
  MEDIO: { clase: 'semaforo-amarillo', texto: 'Ocupación moderada', icono: '🟡' },
  ALTO: { clase: 'semaforo-rojo', texto: 'Alta ocupación', icono: '🔴' },
}

export default function SemaforoAforo({ ocupacion }) {
  if (!ocupacion) {
    return <div className="cargando">Consultando ocupación…</div>
  }
  const nivel = NIVELES[ocupacion.nivel] || NIVELES.BAJO

  return (
    <div className={`semaforo ${nivel.clase}`}>
      <div className="semaforo-circulo">{nivel.icono}</div>
      <div className="semaforo-datos">
        <strong>{nivel.texto}</strong>
        <span>
          {ocupacion.personasDentro} de {ocupacion.capacidadMaxima} personas dentro
        </span>
        <div className="barra-ocupacion">
          <div style={{ width: `${Math.min(100, ocupacion.porcentajeOcupacion)}%` }} />
        </div>
        <small>{ocupacion.porcentajeOcupacion}% de capacidad</small>
      </div>
      <p className="semaforo-recomendacion">{ocupacion.recomendacion}</p>
    </div>
  )
}
