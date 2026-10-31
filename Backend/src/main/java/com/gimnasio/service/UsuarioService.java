package com.gimnasio.service;

import com.gimnasio.domain.Usuario;
import com.gimnasio.domain.enums.EstadoUsuario;
import com.gimnasio.domain.enums.Rol;
import com.gimnasio.domain.enums.TipoNotificacion;
import com.gimnasio.exception.BusinessException;
import com.gimnasio.exception.NotFoundException;
import com.gimnasio.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** CU-26 administración de usuarios del personal (rol, suspensión). */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final NotificacionService notificacionService;

    public UsuarioService(UsuarioRepository usuarioRepository, NotificacionService notificacionService) {
        this.usuarioRepository = usuarioRepository;
        this.notificacionService = notificacionService;
    }

    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    public List<Usuario> listarPorRol(Rol rol) {
        return usuarioRepository.findByRol(rol);
    }

    /** Bandeja de solicitudes de personal creadas por el registro público. */
    public List<Usuario> listarSolicitudes() {
        return usuarioRepository.findByEstadoOrderByFechaRegistroAsc(EstadoUsuario.PENDIENTE);
    }

    public Usuario obtener(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado: " + id));
    }

    @Transactional
    public Usuario aprobar(Long id) {
        Usuario usuario = obtener(id);
        if (usuario.getEstado() != EstadoUsuario.PENDIENTE) {
            throw new BusinessException("Solo se pueden aprobar solicitudes pendientes");
        }
        usuario.setEstado(EstadoUsuario.ACTIVO);
        usuarioRepository.save(usuario);
        notificacionService.enviar(usuario, TipoNotificacion.CUENTA,
                "Su cuenta de " + usuario.getRol().getValor() + " fue aprobada; ya puede iniciar sesión.");
        return usuario;
    }

    @Transactional
    public Usuario rechazar(Long id) {
        Usuario usuario = obtener(id);
        if (usuario.getEstado() != EstadoUsuario.PENDIENTE) {
            throw new BusinessException("Solo se pueden rechazar solicitudes pendientes");
        }
        usuario.setEstado(EstadoUsuario.RECHAZADO);
        usuarioRepository.save(usuario);
        notificacionService.enviar(usuario, TipoNotificacion.CUENTA,
                "Su solicitud de cuenta de " + usuario.getRol().getValor()
                        + " fue rechazada. Contacte a la administración del gimnasio.");
        return usuario;
    }

    @Transactional
    public Usuario suspender(Long id, Long administradorId) {
        if (id.equals(administradorId)) {
            throw new BusinessException("No puede suspender su propia cuenta");
        }
        Usuario usuario = obtener(id);
        if (usuario.getEstado() != EstadoUsuario.ACTIVO) {
            throw new BusinessException("Solo se pueden suspender cuentas activas");
        }
        usuario.setEstado(EstadoUsuario.SUSPENDIDO);
        usuarioRepository.save(usuario);
        notificacionService.enviar(usuario, TipoNotificacion.CUENTA,
                "Su cuenta fue suspendida. Contacte a la administración del gimnasio.");
        return usuario;
    }

    @Transactional
    public Usuario reactivar(Long id) {
        Usuario usuario = obtener(id);
        if (usuario.getEstado() != EstadoUsuario.SUSPENDIDO && usuario.getEstado() != EstadoUsuario.RECHAZADO) {
            throw new BusinessException("La cuenta no está suspendida ni rechazada");
        }
        usuario.setEstado(EstadoUsuario.ACTIVO);
        usuarioRepository.save(usuario);
        return usuario;
    }

    @Transactional
    public Usuario asignarRol(Long id, String rol) {
        Usuario usuario = obtener(id);
        Rol nuevoRol = Rol.desdeValor(rol.toLowerCase());
        if (nuevoRol != Rol.ADMINISTRADOR && esUltimoAdministradorActivo(usuario)) {
            throw new BusinessException("No puede quitar el rol al último administrador activo");
        }
        usuario.setRol(nuevoRol);
        usuarioRepository.save(usuario);
        notificacionService.enviar(usuario, TipoNotificacion.CUENTA,
                "Su rol en el sistema fue actualizado a " + usuario.getRol().getValor() + ".");
        return usuario;
    }

    private boolean esUltimoAdministradorActivo(Usuario usuario) {
        if (usuario.getRol() != Rol.ADMINISTRADOR || usuario.getEstado() != EstadoUsuario.ACTIVO) {
            return false;
        }
        return usuarioRepository.findByRolAndEstado(Rol.ADMINISTRADOR, EstadoUsuario.ACTIVO).size() <= 1;
    }
}
