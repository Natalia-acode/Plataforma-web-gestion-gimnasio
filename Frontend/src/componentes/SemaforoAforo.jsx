import { ProgressBar } from 'react-bootstrap'

/** Semáforo de ocupación en tiempo real (CU-15) — solución al problema de saturación. */
const NIVELES = {
  BAJO: { variante: 'success', texto: 'Baja ocupación' },
  MEDIO: { variante: 'warning', texto: 'Ocupación moderada' },
  ALTO: { variante: 'danger', texto: 'Alta ocupación' },
}

export default function SemaforoAforo({ ocupacion }) {
  if (!ocupacion) {
    return (
      <div className="d-flex align-items-center gap-2 text-secondary py-3">
        <span className="spinner-border spinner-border-sm" aria-hidden="true" />
        <span>Consultando ocupación…</span>
      </div>
    )
  }
  const nivel = NIVELES[ocupacion.nivel] || NIVELES.BAJO

  return (
    <div>
      <div className="d-flex align-items-center gap-3 mb-3">
        <i className={`bi bi-circle-fill fs-3 text-${nivel.variante}`} aria-hidden="true" />
        <div>
          <h4 className="mb-1">{nivel.texto}</h4>
          <p className="mb-0 text-secondary">
            <strong>{ocupacion.personasDentro}</strong> de {ocupacion.capacidadMaxima} personas dentro
          </p>
        </div>
      </div>
      <ProgressBar
        now={ocupacion.porcentajeOcupacion}
        variant={nivel.variante}
        className="mb-2"
        aria-label={`Ocupación al ${ocupacion.porcentajeOcupacion}%`}
      />
      <p className="text-secondary small mb-2">{ocupacion.porcentajeOcupacion}% de capacidad</p>
      {ocupacion.recomendacion && <p className="mb-0">{ocupacion.recomendacion}</p>}
    </div>
  )
}
