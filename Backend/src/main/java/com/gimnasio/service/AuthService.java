package com.gimnasio.service;

import com.gimnasio.domain.Usuario;
import com.gimnasio.domain.enums.EstadoUsuario;
import com.gimnasio.domain.enums.Rol;
import com.gimnasio.domain.enums.TipoNotificacion;
import com.gimnasio.dto.ArranqueResponse;
import com.gimnasio.dto.LoginRequest;
import com.gimnasio.dto.LoginResponse;
import com.gimnasio.dto.PerfilRequest;
import com.gimnasio.dto.RegistroRequest;
import com.gimnasio.dto.RegistroResponse;
import com.gimnasio.dto.StaffRequest;
import com.gimnasio.dto.RestablecerContrasenaRequest;
import com.gimnasio.exception.BusinessException;
import com.gimnasio.exception.NotFoundException;
import com.gimnasio.repository.UsuarioRepository;
import com.gimnasio.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/** CU-01 registro, CU-02 inicio de sesion, CU-03 recuperacion de contraseña. */
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

    /**
     * CU-01: cualquier visitante se registra con el rol que elija. La cuenta de cliente nace activa;
     * la de personal nace activa solo durante el arranque (no hay administrador que la revise)
     * y, si no, queda pendiente hasta que un administrador la apruebe o rechace.
     */
    @Transactional
    public RegistroResponse registrar(RegistroRequest solicitud) {
        String email = solicitud.email().toLowerCase();
        if (usuarioRepository.existsByEmail(email)) {
            throw new BusinessException("Ya existe una cuenta con el correo " + solicitud.email());
        }
        Rol rol = Rol.desdeValor(solicitud.rol() == null || solicitud.rol().isBlank()
                ? Rol.CLIENTE.getValor()
                : solicitud.rol().toLowerCase());
        Usuario usuario = construir(solicitud, rol);
        if (rol == Rol.CLIENTE || faltaAdministrador()) {
            usuario.setEstado(EstadoUsuario.ACTIVO);
            usuarioRepository.save(usuario);
            return RegistroResponse.cuentaActiva(jwtService.generarToken(usuario), usuario);
        }
        usuario.setEstado(EstadoUsuario.PENDIENTE);
        usuarioRepository.save(usuario);
        notificarAdministradores(usuario, rol);
        return RegistroResponse.cuentaPendiente(usuario);
    }

    /** Estado de arranque: mientras no haya administrador activo el personal se autoactiva al registrarse. */
    public ArranqueResponse estadoArranque() {
        boolean falta = faltaAdministrador();
        List<String> roles = Arrays.stream(Rol.values()).map(Rol::getValor).toList();
        return new ArranqueResponse(falta, roles);
    }

    private boolean faltaAdministrador() {
        return usuarioRepository.findByRolAndEstado(Rol.ADMINISTRADOR, EstadoUsuario.ACTIVO).isEmpty();
    }

    public LoginResponse login(LoginRequest solicitud) {
        Usuario usuario = usuarioRepository.findByEmail(solicitud.email().toLowerCase())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));
        if (!passwordEncoder.matches(solicitud.contrasena(), usuario.getPasswordHash())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }
        if (usuario.getEstado() == EstadoUsuario.PENDIENTE) {
            throw new BusinessException("Su solicitud de cuenta de " + usuario.getRol().getValor()
                    + " aún está pendiente de aprobación por un administrador");
        }
        if (usuario.getEstado() == EstadoUsuario.RECHAZADO) {
            throw new BusinessException("Su solicitud de cuenta fue rechazada; contacte a la administración");
        }
        if (usuario.getEstado() == EstadoUsuario.SUSPENDIDO) {
            throw new BusinessException("La cuenta está suspendida; contacte a recepción");
        }
        return LoginResponse.crear(jwtService.generarToken(usuario), usuario);
    }

    /** CU-04: alta de recepcionistas, entrenadores y administradores por un administrador (nace aprobada). */
    @Transactional
    public Usuario crearStaff(StaffRequest solicitud) {
        String email = solicitud.email().toLowerCase();
        if (usuarioRepository.existsByEmail(email)) {
            throw new BusinessException("Ya existe una cuenta con el correo " + solicitud.email());
        }
        Rol rol = Rol.desdeValor(solicitud.rol().toLowerCase());
        if (rol == Rol.CLIENTE) {
            throw new BusinessException("Para cuentas de cliente use el registro público");
        }
        Usuario usuario = construir(new RegistroRequest(solicitud.nombre(), solicitud.apellidos(),
                solicitud.email(), solicitud.contrasena(), null, null, solicitud.rol()), rol);
        usuario.setEstado(EstadoUsuario.ACTIVO);
        usuarioRepository.save(usuario);
        notificacionService.enviar(usuario, TipoNotificacion.CUENTA,
                "Bienvenido(a) al equipo: su cuenta de " + rol.getValor() + " fue creada.");
        return usuario;
    }

    /** CU-03: recuperación de contraseña con token temporal y vencimiento de 15 minutos. */
    @Transactional
    public void recuperarContrasena(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email.toLowerCase()).orElse(null);
        if (usuario == null) {
            return;
        }

        String token = UUID.randomUUID().toString();
        usuario.setPasswordResetToken(token);
        usuario.setPasswordResetExpira(LocalDateTime.now().plusMinutes(15));
        usuarioRepository.save(usuario);

        String enlace = "http://localhost:5173/restablecer?token=" + token;
        notificacionService.enviar(usuario, TipoNotificacion.CUENTA,
                "Solicitud de recuperación de contraseña recibida. Use este enlace para restablecerla: " + enlace
                        + " (válido 15 minutos)."
        );
    }

    @Transactional
    public void restablecerContrasena(RestablecerContrasenaRequest solicitud) {
        if (solicitud == null || solicitud.token() == null || solicitud.token().isBlank()) {
            throw new BusinessException("El token de recuperación es obligatorio");
        }
        if (solicitud.nuevaContrasena() == null || solicitud.nuevaContrasena().length() < 8
                || solicitud.nuevaContrasena().length() > 72) {
            throw new BusinessException("La contraseña debe tener entre 8 y 72 caracteres");
        }

        Usuario usuario = usuarioRepository.findByPasswordResetToken(solicitud.token())
                .orElseThrow(() -> new BusinessException("El token de recuperación es inválido o ya fue usado"));
        if (usuario.getPasswordResetExpira() == null || usuario.getPasswordResetExpira().isBefore(LocalDateTime.now())) {
            usuario.setPasswordResetToken(null);
            usuario.setPasswordResetExpira(null);
            usuarioRepository.save(usuario);
            throw new BusinessException("El enlace de recuperación expiró. Solicite uno nuevo");
        }

        usuario.setPasswordHash(passwordEncoder.encode(solicitud.nuevaContrasena()));
        usuario.setPasswordResetToken(null);
        usuario.setPasswordResetExpira(null);
        usuarioRepository.save(usuario);
        notificacionService.enviar(usuario, TipoNotificacion.CUENTA,
                "Su contraseña fue actualizada correctamente.");
    }

    /** CU-04: edición del perfil del usuario autenticado. */
    @Transactional
    public Usuario actualizarPerfil(Long usuarioId, PerfilRequest solicitud) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado: " + usuarioId));

        if (solicitud.nombre() != null) {
            String nombre = solicitud.nombre().trim();
            validarNombre(nombre, "nombre");
            usuario.setNombre(nombre);
        }
        if (solicitud.apellidos() != null) {
            String apellidos = solicitud.apellidos().trim();
            validarNombre(apellidos, "apellidos");
            usuario.setApellidos(apellidos);
        }
        if (solicitud.telefono() != null) {
            usuario.setTelefono(solicitud.telefono().trim());
        }
        if (solicitud.fechaNacimiento() != null && !solicitud.fechaNacimiento().isBlank()) {
            usuario.setFechaNacimiento(LocalDate.parse(solicitud.fechaNacimiento()));
        }

        if (usuario.getNombre() == null || usuario.getNombre().isBlank()
                || usuario.getApellidos() == null || usuario.getApellidos().isBlank()) {
            throw new BusinessException("Nombre y apellidos son obligatorios");
        }

        return usuarioRepository.save(usuario);
    }

    private Usuario construir(RegistroRequest solicitud, Rol rol) {
        validarNombre(solicitud.nombre(), "nombre");
        validarNombre(solicitud.apellidos(), "apellidos");

        Usuario usuario = new Usuario();
        usuario.setNombre(solicitud.nombre().trim());
        usuario.setApellidos(solicitud.apellidos().trim());
        usuario.setEmail(solicitud.email().toLowerCase());
        usuario.setPasswordHash(passwordEncoder.encode(solicitud.contrasena()));
        usuario.setTelefono(solicitud.telefono());
        if (solicitud.fechaNacimiento() != null && !solicitud.fechaNacimiento().isBlank()) {
            usuario.setFechaNacimiento(LocalDate.parse(solicitud.fechaNacimiento()));
        }
        usuario.setRol(rol);
        return usuario;
    }

    private void notificarAdministradores(Usuario solicitante, Rol rol) {
        String mensaje = "Nueva solicitud de cuenta de " + rol.getValor() + " pendiente de aprobación: "
                + solicitante.getNombreCompleto() + " (" + solicitante.getEmail() + ").";
        for (Usuario administrador : usuarioRepository.findByRolAndEstado(Rol.ADMINISTRADOR, EstadoUsuario.ACTIVO)) {
            notificacionService.enviar(administrador, TipoNotificacion.CUENTA, mensaje);
        }
    }

    private void validarNombre(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new BusinessException("El " + campo + " es obligatorio");
        }
        if (!valor.matches("^[\\p{L}\\p{M}ñÑáéíóúÁÉÍÓÚüÜ' .-]+$")) {
            throw new BusinessException("El " + campo + " solo puede contener letras, espacios, acentos, comillas simples, puntos y guiones");
        }
    }
}
