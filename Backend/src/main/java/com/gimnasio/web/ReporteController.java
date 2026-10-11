package com.gimnasio.web;

import com.gimnasio.dto.ReporteResponse;
import com.gimnasio.service.ReporteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** CU-29: reportes y exportación (solo administrador). */
@RestController
@RequestMapping("/api/reportes")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@Tag(name = "Reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping
    public ReporteResponse generar(@RequestParam(required = false) String desde,
                                   @RequestParam(required = false) String hasta) {
        LocalDate fechaHasta = hasta == null ? LocalDate.now() : LocalDate.parse(hasta);
        LocalDate fechaDesde = desde == null ? fechaHasta.minusDays(30) : LocalDate.parse(desde);
        return reporteService.generar(fechaDesde, fechaHasta);
    }

    @GetMapping("/exportar")
    public ResponseEntity<String> exportar(@RequestParam(required = false) String desde,
                                           @RequestParam(required = false) String hasta) {
        LocalDate fechaHasta = hasta == null ? LocalDate.now() : LocalDate.parse(hasta);
        LocalDate fechaDesde = desde == null ? fechaHasta.minusDays(30) : LocalDate.parse(desde);
        String csv = reporteService.exportarPagosCsv(fechaDesde, fechaHasta);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=pagos_" + fechaDesde + "_" + fechaHasta + ".csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }
}
