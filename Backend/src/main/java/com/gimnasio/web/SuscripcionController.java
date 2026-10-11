package com.gimnasio.web;

import com.gimnasio.dto.CambiarPlanRequest;
import com.gimnasio.dto.CancelarSuscripcionRequest;
import com.gimnasio.dto.ContratarSuscripcionRequest;
import com.gimnasio.dto.RenovacionRequest;
import com.gimnasio.dto.SuscripcionResponse;
import com.gimnasio.security.AuthenticatedUser;
import com.gimnasio.service.SuscripcionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** CU-06 contratar, CU-08 cancelar (opt-out en línea), CU-09 cambio de plan, CU-11 renovación. */
@RestController
@RequestMapping("/api/suscripciones")
@Tag(name = "Suscripciones")
public class SuscripcionController {

    private final SuscripcionService suscripcionService;

    public SuscripcionController(SuscripcionService suscripcionService) {
        this.suscripcionService = suscripcionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SuscripcionResponse contratar(@AuthenticationPrincipal AuthenticatedUser principal,
                                         @Valid @RequestBody ContratarSuscripcionRequest solicitud) {
        return SuscripcionResponse.desde(suscripcionService.contratar(principal.getId(), solicitud));
    }

    @GetMapping("/mis")
    public List<SuscripcionResponse> misSuscripciones(@AuthenticationPrincipal AuthenticatedUser principal) {
        return suscripcionService.listarDe(principal.getId()).stream()
                .map(SuscripcionResponse::desde).toList();
    }

    @GetMapping("/activa")
    public SuscripcionResponse activa(@AuthenticationPrincipal AuthenticatedUser principal) {
        return SuscripcionResponse.desde(suscripcionService.obtenerActivaDe(principal.getId()));
    }

    @GetMapping("/{id}")
    public SuscripcionResponse obtener(@AuthenticationPrincipal AuthenticatedUser principal,
                                       @PathVariable Long id) {
        return SuscripcionResponse.desde(suscripcionService.obtenerDe(principal.getId(), id));
    }

    /** CU-09: cambio de plan. */
    @PostMapping("/{id}/cambiar-plan")
    public SuscripcionResponse cambiarPlan(@AuthenticationPrincipal AuthenticatedUser principal,
                                           @PathVariable Long id,
                                           @Valid @RequestBody CambiarPlanRequest solicitud) {
        return SuscripcionResponse.desde(
                suscripcionService.cambiarPlan(principal.getId(), id, solicitud.idPlan()));
    }

    /** CU-08: cancelación en línea, sin trámites ni llamadas (solución al problema 1). */
    @PostMapping("/{id}/cancelar")
    public SuscripcionResponse cancelar(@AuthenticationPrincipal AuthenticatedUser principal,
                                        @PathVariable Long id,
                                        @RequestBody(required = false) CancelarSuscripcionRequest solicitud) {
        String motivo = solicitud == null ? null : solicitud.motivo();
        return SuscripcionResponse.desde(suscripcionService.cancelar(principal.getId(), id, motivo));
    }

    /** CU-11: activar/desactivar la renovación automática (opt-in, RN-01). */
    @PutMapping("/{id}/renovacion")
    public SuscripcionResponse renovacion(@AuthenticationPrincipal AuthenticatedUser principal,
                                          @PathVariable Long id,
                                          @Valid @RequestBody RenovacionRequest solicitud) {
        return SuscripcionResponse.desde(
                suscripcionService.setRenovacionAutomatica(principal.getId(), id, solicitud.activa()));
    }

    @DeleteMapping("/{id}")
    public SuscripcionResponse cancelarAlias(@AuthenticationPrincipal AuthenticatedUser principal,
                                             @PathVariable Long id) {
        return SuscripcionResponse.desde(suscripcionService.cancelar(principal.getId(), id, null));
    }
}
