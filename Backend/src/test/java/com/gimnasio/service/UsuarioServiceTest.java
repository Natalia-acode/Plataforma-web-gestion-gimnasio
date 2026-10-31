package com.gimnasio.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gimnasio.domain.Usuario;
import com.gimnasio.domain.enums.EstadoUsuario;
import com.gimnasio.domain.enums.Rol;
import com.gimnasio.domain.enums.TipoNotificacion;
import com.gimnasio.exception.BusinessException;
import com.gimnasio.repository.UsuarioRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private NotificacionService notificacionService;

    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioService(usuarioRepository, notificacionService);
    }

    private Usuario solicitud(EstadoUsuario estado) {
        Usuario usuario = new Usuario();
        usuario.setId(7L);
        usuario.setNombre("Pedro");
        usuario.setApellidos("Ramírez");
        usuario.setEmail("pedro@email.com");
        usuario.setRol(Rol.ENTRENADOR);
        usuario.setEstado(estado);
        return usuario;
    }

    @Test
    void aprobar_pendiente_laDejaActivaYAvisaAlSolicitante() {
        Usuario pendiente = solicitud(EstadoUsuario.PENDIENTE);
        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(pendiente));

        Usuario resultado = usuarioService.aprobar(7L);

        assertEquals(EstadoUsuario.ACTIVO, resultado.getEstado());
        verify(usuarioRepository).save(pendiente);
        verify(notificacionService).enviar(any(Usuario.class), eq(TipoNotificacion.CUENTA), anyString());
    }

    @Test
    void aprobar_cuentaActiva_lanzaExcepcion() {
        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(solicitud(EstadoUsuario.ACTIVO)));

        assertThrows(BusinessException.class, () -> usuarioService.aprobar(7L));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void rechazar_pendiente_laDejaRechazada() {
        Usuario pendiente = solicitud(EstadoUsuario.PENDIENTE);
        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(pendiente));

        Usuario resultado = usuarioService.rechazar(7L);

        assertEquals(EstadoUsuario.RECHAZADO, resultado.getEstado());
        verify(usuarioRepository).save(pendiente);
    }

    @Test
    void suspender_pendiente_lanzaExcepcion() {
        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(solicitud(EstadoUsuario.PENDIENTE)));

        assertThrows(BusinessException.class, () -> usuarioService.suspender(7L, 1L));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void reactivar_rechazada_laDejaActiva() {
        Usuario rechazada = solicitud(EstadoUsuario.RECHAZADO);
        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(rechazada));

        Usuario resultado = usuarioService.reactivar(7L);

        assertEquals(EstadoUsuario.ACTIVO, resultado.getEstado());
    }

    @Test
    void reactivar_pendiente_lanzaExcepcion() {
        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(solicitud(EstadoUsuario.PENDIENTE)));

        assertThrows(BusinessException.class, () -> usuarioService.reactivar(7L));
    }

    @Test
    void asignarRol_ultimoAdministradorActivo_lanzaExcepcion() {
        Usuario admin = solicitud(EstadoUsuario.ACTIVO);
        admin.setRol(Rol.ADMINISTRADOR);
        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(admin));
        when(usuarioRepository.findByRolAndEstado(Rol.ADMINISTRADOR, EstadoUsuario.ACTIVO))
                .thenReturn(List.of(admin));

        assertThrows(BusinessException.class, () -> usuarioService.asignarRol(7L, "entrenador"));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }
}
