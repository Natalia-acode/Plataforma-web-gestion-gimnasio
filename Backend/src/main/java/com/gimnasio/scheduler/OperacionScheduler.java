package com.gimnasio.scheduler;

import com.gimnasio.service.AforoService;
import com.gimnasio.service.CheckInService;
import com.gimnasio.service.ColaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Procesos programados de operación diaria:
 *  - cada minuto: expiración de turnos de cola no atendidos (RN-08);
 *  - cada hora: snapshot de ocupación para el mapa de calor (CU-16);
 *  - 23:30: cierre de registros de check-in olvidados (RN-11).
 */
@Component
public class OperacionScheduler {

    private static final Logger log = LoggerFactory.getLogger(OperacionScheduler.class);

    private final ColaService colaService;
    private final AforoService aforoService;
    private final CheckInService checkInService;

    public OperacionScheduler(ColaService colaService, AforoService aforoService,
                              CheckInService checkInService) {
        this.colaService = colaService;
        this.aforoService = aforoService;
        this.checkInService = checkInService;
    }

    @Scheduled(fixedDelay = 60_000)
    public void expirarTurnosVencidos() {
        int expirados = colaService.expirarTurnosVencidos();
        if (expirados > 0) {
            log.info("Turnos de cola expirados por inasistencia: {}", expirados);
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    public void generarSnapshotAforo() {
        aforoService.generarSnapshotAforo();
        log.debug("Snapshot horario de aforo registrado");
    }

    @Scheduled(cron = "0 30 23 * * *")
    public void cierreJornada() {
        int cerrados = checkInService.cerrarRegistrosInactivos(14);
        log.info("Cierre de jornada: {} registros de check-in cerrados automáticamente", cerrados);
    }
}
