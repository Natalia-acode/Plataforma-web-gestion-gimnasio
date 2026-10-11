package com.gimnasio.service;

import com.gimnasio.domain.Usuario;
import com.gimnasio.domain.enums.EstadoUsuario;
import com.gimnasio.domain.enums.Rol;
import com.gimnasio.dto.LoginRequest;
import com.gimnasio.dto.LoginResponse;
import com.gimnasio.dto.RegistroRequest;
import com.gimnasio.dto.StaffRequest;
import com.gimnasio.exception.BusinessException;
import com.gimnasio.exception.NotFoundException;
import com.gimnasio.repository.UsuarioRepository;
import com.gimnasio.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/** CU-01 registro, CU-02 inicio de sesión, CU-03 recuperación de contraseña. */
@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final NotificacionService notificacionService;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                       JwtService jwtService, NotificacionService notificacionService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.notificacionService = notificacionService;
    }

    @Transactional
    public LoginResponse registrarCliente(RegistroRequest solicitud) {
        if (usuarioRepository.existsByEmail(solicitud.email().toLowerCase())) {
            throw new BusinessException("Ya existe una cuenta con el correo " + solicitud.email());
        }
        Usuario usuario = new Usuario();
        usuario.setNombre(solicitud.nombre());
        usuario.setApellidos(solicitud.apellidos());
        usuario.setEmail(solicitud.email().toLowerCase());
        usuario.setPasswordHash(passwordEncoder.encode(solicitud.contrasena()));
        usuario.setTelefono(solicitud.telefono());
        if (solicitud.fechaNacimiento() != null && !solicitud.fechaNacimiento().isBlank()) {
            usuario.setFechaNacimiento(LocalDate.parse(solicitud.fechaNacimiento()));
        }
        usuario.setRol(Rol.CLIENTE);
        usuarioRepository.save(usuario);
        return LoginResponse.crear(jwtService.generarToken(usuario), usuario);
    }

    public LoginResponse login(LoginRequest solicitud) {
        Usuario usuario = usuarioRepository.findByEmail(solicitud.email().toLowerCase())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));
        if (!passwordEncoder.matches(solicitud.contrasena(), usuario.getPasswordHash())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }
        if (usuario.getEstado() == EstadoUsuario.SUSPENDIDO) {
            throw new BusinessException("La cuenta está suspendida; contacte a recepción");
        }
        return LoginResponse.crear(jwtService.generarToken(usuario), usuario);
    }

    /** CU-04: alta de recepcionistas, entrenadores y administradores (solo administrador). */
    @Transactional
    public Usuario crearStaff(StaffRequest solicitud) {
        if (usuarioRepository.existsByEmail(solicitud.email().toLowerCase())) {
            throw new BusinessException("Ya existe una cuenta con el correo " + solicitud.email());
        }
        Rol rol = Rol.desdeValor(solicitud.rol().toLowerCase());
        if (rol == Rol.CLIENTE) {
            throw new BusinessException("Para cuentas de cliente use el registro público");
        }
        Usuario usuario = new Usuario();
        usuario.setNombre(solicitud.nombre());
        usuario.setApellidos(solicitud.apellidos());
        usuario.setEmail(solicitud.email().toLowerCase());
        usuario.setPasswordHash(passwordEncoder.encode(solicitud.contrasena()));
        usuario.setRol(rol);
        usuarioRepository.save(usuario);
        notificacionService.enviar(usuario, com.gimnasio.domain.enums.TipoNotificacion.CUENTA,
                "Bienvenido(a) al equipo: su cuenta de " + rol.getValor() + " fue creada.");
        return usuario;
    }

    /** CU-03: recuperación de contraseña (envía aviso; el flujo completo usaría token por correo). */
    @Transactional
    public void recuperarContrasena(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new NotFoundException("No existe una cuenta con el correo " + email));
        notificacionService.enviar(usuario, com.gimnasio.domain.enums.TipoNotificacion.CUENTA,
                "Solicitud de recuperación de contraseña recibida. Verifique su correo.");
    }
}
