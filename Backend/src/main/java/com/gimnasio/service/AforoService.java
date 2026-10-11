package com.gimnasio.service;

import com.gimnasio.domain.Configuracion;
import com.gimnasio.domain.RegistroAforo;
import com.gimnasio.domain.enums.DiaSemana;
import com.gimnasio.domain.enums.NivelAforo;
import com.gimnasio.dto.AfluenciaResponse;
import com.gimnasio.dto.AforoResponse;
import com.gimnasio.repository.CheckInRepository;
import com.gimnasio.repository.RegistroAforoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** CU-15 consulta de ocupación en vivo, CU-16 mapa de calor de afluencia. */
@Service
public class AforoService {

    private final CheckInService checkInService;
    private final CheckInRepository checkInRepository;
    private final RegistroAforoRepository registroAforoRepository;
    private final ConfiguracionService configuracionService;

    public AforoService(CheckInService checkInService, CheckInRepository checkInRepository,
                        RegistroAforoRepository registroAforoRepository, ConfiguracionService configuracionService) {
        this.checkInService = checkInService;
        this.checkInRepository = checkInRepository;
        this.registroAforoRepository = registroAforoRepository;
        this.configuracionService = configuracionService;
    }

    /** Semáforo en tiempo real: verde/amarillo/rojo según umbrales configurados. */
    public AforoResponse ocupacionActual() {
        Configuracion configuracion = configuracionService.obtener();
        int dentro = (int) checkInService.personasDentro();
        int capacidad = configuracion.getCapacidadMaxima();
        int porcentaje = capacidad == 0 ? 0 : (int) Math.round(dentro * 100.0 / capacidad);

        NivelAforo nivel;
        String recomendacion;
        if (porcentaje >= configuracion.getUmbralAlto()) {
            nivel = NivelAforo.ALTO;
            recomendacion = "Momento de alta ocupación. Considere venir más tarde o reservar su turno virtual.";
        } else if (porcentaje >= configuracion.getUmbralMedio()) {
            nivel = NivelAforo.MEDIO;
            recomendacion = "Ocupación moderada. Es un buen momento para entrenar.";
        } else {
            nivel = NivelAforo.BAJO;
            recomendacion = "Baja ocupación: excelente momento para entrenar.";
        }
        return new AforoResponse(dentro, capacidad, porcentaje, nivel.name(), recomendacion);
    }

    /** CU-16: mapa de calor desde la vista vw_afluencia_por_hora. */
    public AfluenciaResponse afluencia(int horaDesde, int horaHasta) {
        List<RegistroAforoRepository.AfluenciaHoraProjection> filas =
                registroAforoRepository.afluenciaPorHora(horaDesde, horaHasta);
        List<AfluenciaResponse.CeldaAfluencia> celdas = new ArrayList<>();
        for (RegistroAforoRepository.AfluenciaHoraProjection fila : filas) {
            DiaSemana dia = DiaSemana.desdeIndice(fila.getDia_semana());
            celdas.add(new AfluenciaResponse.CeldaAfluencia(
                    dia.getValor(),
                    dia.getIndice(),
                    fila.getHora(),
                    fila.getOcupacion_promedio() == null ? 0 : fila.getOcupacion_promedio().doubleValue(),
                    fila.getOcupacion_maxima() == null ? 0 : fila.getOcupacion_maxima()));
        }
        AfluenciaResponse.CeldaAfluencia masConcurrida = celdas.stream()
                .max(Comparator.comparingDouble(AfluenciaResponse.CeldaAfluencia::ocupacionPromedio))
                .orElse(null);
        AfluenciaResponse.CeldaAfluencia menosConcurrida = celdas.stream()
                .min(Comparator.comparingDouble(AfluenciaResponse.CeldaAfluencia::ocupacionPromedio))
                .orElse(null);
        return new AfluenciaResponse(celdas, masConcurrida, menosConcurrida);
    }

    /**
     * Snapshot horario para el mapa de calor (proceso programado).
     * Promedio y máximo de las lecturas de ocupación registradas durante la hora.
     */
    @Transactional
    public void generarSnapshotAforo() {
        LocalDateTime fin = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);
        LocalDateTime inicio = fin.minusHours(1);
        LocalDate fecha = inicio.toLocalDate();

        List<com.gimnasio.domain.CheckIn> ingresos = checkInRepository.findByFechaHoraEntradaBetween(inicio, fin);
        List<com.gimnasio.domain.CheckIn> dentro = checkInRepository.findByFechaHoraSalidaIsNullAndFechaHoraEntradaBefore(fin);

        int ocupacionActual = dentro.size();
        int ocupacionMaxima = Math.max(ocupacionActual, ingresos.size());
        int totalIngresos = ingresos.size();

        byte hora = (byte) inicio.getHour();
        RegistroAforo registro = registroAforoRepository.findByFecha(fecha).stream()
                .filter(r -> r.getHora() == hora)
                .findFirst()
                .orElseGet(RegistroAforo::new);
        registro.setFecha(fecha);
        registro.setHora(hora);
        // Promedio simple entre la ocupación al inicio y al final de la ventana.
        registro.setOcupacionPromedio(Math.max(0, ocupacionActual));
        registro.setOcupacionMaxima(Math.max(ocupacionMaxima, ocupacionActual));
        registro.setTotalIngresos(totalIngresos);
        registroAforoRepository.save(registro);
    }
}
