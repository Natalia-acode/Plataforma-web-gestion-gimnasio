package com.gimnasio.web;

import com.gimnasio.domain.Zona;
import com.gimnasio.dto.ZonaRequest;
import com.gimnasio.dto.ZonaResponse;
import com.gimnasio.service.ZonaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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

@RestController
@RequestMapping("/api/zonas")
@Tag(name = "Zonas")
public class ZonaController {

    private final ZonaService zonaService;

    public ZonaController(ZonaService zonaService) {
        this.zonaService = zonaService;
    }

    @GetMapping
    public List<ZonaResponse> listar() {
        return zonaService.listarActivas().stream().map(ZonaResponse::desde).toList();
    }

    @GetMapping("/todos")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public List<ZonaResponse> listarTodos() {
        return zonaService.listarTodos().stream().map(ZonaResponse::desde).toList();
    }

    @GetMapping("/{id}")
    public ZonaResponse obtener(@PathVariable Long id) {
        return ZonaResponse.desde(zonaService.obtener(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ResponseStatus(HttpStatus.CREATED)
    public ZonaResponse crear(@Valid @RequestBody ZonaRequest solicitud) {
        return ZonaResponse.desde(zonaService.crear(solicitud));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ZonaResponse actualizar(@PathVariable Long id, @Valid @RequestBody ZonaRequest solicitud) {
        return ZonaResponse.desde(zonaService.actualizar(id, solicitud));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ZonaResponse desactivar(@PathVariable Long id) {
        return ZonaResponse.desde(zonaService.desactivar(id));
    }
}
