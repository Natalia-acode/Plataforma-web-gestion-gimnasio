package com.gimnasio.scheduler;

import com.gimnasio.service.CobroService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Procesos programados de suscripciones (CU-12):
 *  - 05:00 diario: recordatorios de vencimiento y ejecucion de cobros de renovacion.
 */
@Component
public class RenovacionScheduler {

    private static final Logger log = LoggerFactory.getLogger(RenovacionScheduler.class);

    private final CobroService cobroService;

    public RenovacionScheduler(CobroService cobroService) {
        this.cobroService = cobroService;
    }

    @Scheduled(cron = "0 0 5 * * *")
    public void recordatoriosCobro() {
        int enviados = cobroService.enviarRecordatorios(3);
        log.info("Recordatorios de cobro enviados: {}", enviados);
    }

    @Scheduled(cron = "0 30 5 * * *")
    public void ejecutarCobros() {
        var resultados = cobroService.ejecutarCobros();
        log.info("Proceso de renovación finalizado: {} cobros procesados", resultados.size());
    }
}
