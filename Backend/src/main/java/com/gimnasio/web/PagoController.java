package com.gimnasio.web;

import com.gimnasio.dto.MetodoPagoRequest;
import com.gimnasio.dto.MetodoPagoResponse;
import com.gimnasio.dto.PagoResponse;
import com.gimnasio.security.AuthenticatedUser;
import com.gimnasio.service.MetodoPagoService;
import com.gimnasio.service.PagoService;
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

/** CU-10 métodos de pago, CU-06/11/12 historial y facturas. */
@RestController
@RequestMapping("/api/pagos")
@Tag(name = "Pagos")
public class PagoController {

    private final MetodoPagoService metodoPagoService;
    private final PagoService pagoService;

    public PagoController(MetodoPagoService metodoPagoService, PagoService pagoService) {
        this.metodoPagoService = metodoPagoService;
        this.pagoService = pagoService;
    }

    @GetMapping("/metodos")
    public List<MetodoPagoResponse> misMetodos(@AuthenticationPrincipal AuthenticatedUser principal) {
        return metodoPagoService.listarDe(principal.getId()).stream()
                .map(MetodoPagoResponse::desde).toList();
    }

    /** CU-10: el número completo solo viaja a la pasarela; aquí se tokeniza (RNF-02). */
    @PostMapping("/metodos")
    @ResponseStatus(HttpStatus.CREATED)
    public MetodoPagoResponse registrarMetodo(@AuthenticationPrincipal AuthenticatedUser principal,
                                              @Valid @RequestBody MetodoPagoRequest solicitud) {
        return MetodoPagoResponse.desde(metodoPagoService.registrar(principal.getId(), solicitud));
    }

    @PutMapping("/metodos/{id}/predeterminado")
    public MetodoPagoResponse predeterminar(@AuthenticationPrincipal AuthenticatedUser principal,
                                            @PathVariable Long id) {
        return MetodoPagoResponse.desde(metodoPagoService.marcarPredeterminado(principal.getId(), id));
    }

    @DeleteMapping("/metodos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarMetodo(@AuthenticationPrincipal AuthenticatedUser principal,
                               @PathVariable Long id) {
        metodoPagoService.eliminar(principal.getId(), id);
    }

    @GetMapping("/historial")
    public List<PagoResponse> historial(@AuthenticationPrincipal AuthenticatedUser principal) {
        return pagoService.listarDeUsuario(principal.getId()).stream()
                .map(pago -> PagoResponse.desde(pago, null)).toList();
    }

    /** CU-11: factura del pago (representación en JSON; la descarga PDF sería el siguiente paso). */
    @GetMapping("/{id}/factura")
    public PagoResponse factura(@AuthenticationPrincipal AuthenticatedUser principal,
                                @PathVariable Long id) {
        return PagoResponse.desde(pagoService.obtenerDeUsuario(principal.getId(), id), null);
    }
}
