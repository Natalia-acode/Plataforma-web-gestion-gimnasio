package com.gimnasio.web;

import com.gimnasio.dto.NotificacionResponse;
import com.gimnasio.dto.UsuarioResponse;
import com.gimnasio.security.AuthenticatedUser;
import com.gimnasio.service.NotificacionService;
import com.gimnasio.service.UsuarioService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** CU-26 administración de usuarios (solo administrador). */
@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final NotificacionService notificacionService;

    public UsuarioController(UsuarioService usuarioService, NotificacionService notificacionService) {
        this.usuarioService = usuarioService;
        this.notificacionService = notificacionService;
    }

    @GetMapping
    public List<UsuarioResponse> listar() {
        return usuarioService.listar().stream().map(UsuarioResponse::desde).toList();
    }

    @GetMapping("/{id}")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return UsuarioResponse.desde(usuarioService.obtener(id));
    }

    @PutMapping("/{id}/suspender")
    public UsuarioResponse suspender(@AuthenticationPrincipal AuthenticatedUser principal,
                                     @PathVariable Long id) {
        return UsuarioResponse.desde(usuarioService.suspender(id, principal.getId()));
    }

    @PutMapping("/{id}/reactivar")
    public UsuarioResponse reactivar(@PathVariable Long id) {
        return UsuarioResponse.desde(usuarioService.reactivar(id));
    }

    @PutMapping("/{id}/rol")
    public UsuarioResponse asignarRol(@PathVariable Long id, @RequestParam String rol) {
        return UsuarioResponse.desde(usuarioService.asignarRol(id, rol));
    }

    // ---------- Centro de notificaciones (cualquier usuario autenticado) ----------

    @GetMapping("/notificaciones/mias")
    public List<NotificacionResponse> misNotificaciones(@AuthenticationPrincipal AuthenticatedUser principal) {
        return notificacionService.listar(principal.getId()).stream()
                .map(NotificacionResponse::desde).toList();
    }

    @PutMapping("/notificaciones/{id}/leida")
    public void marcarLeida(@AuthenticationPrincipal AuthenticatedUser principal,
                            @PathVariable Long id) {
        notificacionService.marcarLeida(principal.getId(), id);
    }

    @DeleteMapping("/notificaciones/{id}")
    public List<NotificacionResponse> descartar(@AuthenticationPrincipal AuthenticatedUser principal,
                                                @PathVariable Long id) {
        notificacionService.marcarLeida(principal.getId(), id);
        return notificacionService.listar(principal.getId()).stream()
                .map(NotificacionResponse::desde).toList();
    }
}
