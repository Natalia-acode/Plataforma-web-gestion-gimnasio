package com.gimnasio.web;

import com.gimnasio.domain.Usuario;
import com.gimnasio.dto.LoginRequest;
import com.gimnasio.dto.LoginResponse;
import com.gimnasio.dto.RecuperarContrasenaRequest;
import com.gimnasio.dto.RegistroRequest;
import com.gimnasio.dto.StaffRequest;
import com.gimnasio.dto.UsuarioResponse;
import com.gimnasio.security.AuthenticatedUser;
import com.gimnasio.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** CU-01 registro, CU-02 login, CU-03 recuperación, CU-04 alta de personal. */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public LoginResponse registro(@Valid @RequestBody RegistroRequest solicitud) {
        return authService.registrarCliente(solicitud);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest solicitud) {
        return authService.login(solicitud);
    }

    @PostMapping("/recuperar-contrasena")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void recuperarContrasena(@Valid @RequestBody RecuperarContrasenaRequest solicitud) {
        authService.recuperarContrasena(solicitud.email());
    }

    @PostMapping("/staff")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse crearStaff(@Valid @RequestBody StaffRequest solicitud) {
        Usuario usuario = authService.crearStaff(solicitud);
        return UsuarioResponse.desde(usuario);
    }

    @GetMapping("/perfil")
    public UsuarioResponse perfil(@AuthenticationPrincipal AuthenticatedUser principal) {
        return UsuarioResponse.desde(principal.getUsuario());
    }
}
