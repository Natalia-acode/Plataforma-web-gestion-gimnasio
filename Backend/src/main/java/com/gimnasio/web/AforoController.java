package com.gimnasio.web;

import com.gimnasio.domain.Configuracion;
import com.gimnasio.domain.Zona;
import com.gimnasio.dto.AforoResponse;
import com.gimnasio.dto.AfluenciaResponse;
import com.gimnasio.dto.ConfiguracionResponse;
import com.gimnasio.dto.AforoConfigRequest;
import com.gimnasio.repository.ZonaRepository;
import com.gimnasio.service.AforoService;
import com.gimnasio.service.ConfiguracionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** CU-15 ocupación en vivo, CU-16 mapa de calor, CU-17 configuración de aforo. */
@RestController
@RequestMapping("/api/aforo")
@Tag(name = "Aforo y afluencia")
public class AforoController {

    private final AforoService aforoService;
    private final ConfiguracionService configuracionService;
    private final ZonaRepository zonaRepository;

    public AforoController(AforoService aforoService, ConfiguracionService configuracionService,
                           ZonaRepository zonaRepository) {
        this.aforoService = aforoService;
        this.configuracionService = configuracionService;
        this.zonaRepository = zonaRepository;
    }

    /** Problema 2 (saturación): semáforo público de ocupación en tiempo real. */
    @GetMapping("/ocupacion")
    public AforoResponse ocupacion() {
        return aforoService.ocupacionActual();
    }

    /** Problema 2: mapa de calor por día y hora (promedios históricos). */
    @GetMapping("/afluencia")
    public AfluenciaResponse afluencia(@RequestParam(defaultValue = "5") int horaDesde,
                                       @RequestParam(defaultValue = "22") int horaHasta) {
        return aforoService.afluencia(horaDesde, horaHasta);
    }

    @GetMapping("/configuracion")
    public ConfiguracionResponse configuracion() {
        return mapear(configuracionService.obtener());
    }

    @PutMapping("/configuracion")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ConfiguracionResponse actualizarConfiguracion(@Valid @RequestBody AforoConfigRequest solicitud) {
        return mapear(configuracionService.actualizar(solicitud));
    }

    /** CU-28: zonas del gimnasio con su capacidad. */
    @GetMapping("/zonas")
    public List<ZonaResponse> zonas() {
        return zonaRepository.findByActivaTrue().stream()
                .map(z -> new ZonaResponse(z.getId(), z.getNombre(), z.getTipo().getValor(), z.getCapacidadMaxima()))
                .toList();
    }

    private ConfiguracionResponse mapear(Configuracion configuracion) {
        return new ConfiguracionResponse(
                configuracion.getId(),
                configuracion.getCapacidadMaxima(),
                configuracion.getUmbralMedio(),
                configuracion.getUmbralAlto(),
                configuracion.getVentanaTurnoMin(),
                configuracion.getTiempoRotacionMin(),
                configuracion.getReintentosCobro(),
                configuracion.getHorarioApertura().toString(),
                configuracion.getHorarioCierre().toString());
    }

    public record ZonaResponse(Long id, String nombre, String tipo, int capacidadMaxima) {
    }
}
