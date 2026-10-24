package com.gimnasio.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gimnasio.domain.CheckIn;
import com.gimnasio.domain.Suscripcion;
import com.gimnasio.domain.Usuario;
import com.gimnasio.domain.enums.EstadoSuscripcion;
import com.gimnasio.domain.enums.EstadoUsuario;
import com.gimnasio.domain.enums.MetodoCheckIn;
import com.gimnasio.domain.enums.Rol;
import com.gimnasio.exception.BusinessException;
import com.gimnasio.exception.NotFoundException;
import com.gimnasio.repository.CheckInRepository;
import com.gimnasio.repository.SuscripcionRepository;
import com.gimnasio.repository.UsuarioRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CheckInServiceTest {

    @Mock
    private CheckInRepository checkInRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private SuscripcionRepository suscripcionRepository;

    private CheckInService checkInService;

    private Usuario usuario;
    private Suscripcion suscripcion;
    private CheckIn checkInAbierto;

    @BeforeEach
    void setUp() {
        checkInService = new CheckInService(
                checkInRepository, usuarioRepository, suscripcionRepository);

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Juan");
        usuario.setApellidos("Pérez");
        usuario.setEmail("juan@email.com");
        usuario.setEstado(EstadoUsuario.ACTIVO);
        usuario.setRol(Rol.CLIENTE);

        suscripcion = new Suscripcion();
        suscripcion.setId(10L);
        suscripcion.setUsuario(usuario);
        suscripcion.setEstado(EstadoSuscripcion.ACTIVA);
        suscripcion.setFechaFin(LocalDate.now().plusDays(30));

        checkInAbierto = new CheckIn();
        checkInAbierto.setId(100L);
        checkInAbierto.setUsuario(usuario);
        checkInAbierto.setFechaHoraEntrada(LocalDateTime.now().minusHours(1));
        checkInAbierto.setFechaHoraSalida(null);
        checkInAbierto.setMetodo(MetodoCheckIn.QR);
    }

    @Test
    void registrarEntradaQr_exito() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(suscripcionRepository.findFirstByUsuarioIdAndEstado(1L, EstadoSuscripcion.ACTIVA))
                .thenReturn(Optional.of(suscripcion));
        when(checkInRepository.findFirstByUsuarioIdAndFechaHoraSalidaIsNull(1L))
                .thenReturn(Optional.empty());
        when(checkInRepository.save(any(CheckIn.class))).thenAnswer(inv -> inv.getArgument(0));

        CheckIn resultado = checkInService.registrarEntradaQr(1L);

        assertNotNull(resultado);
        assertEquals(MetodoCheckIn.QR, resultado.getMetodo());
        assertEquals(usuario, resultado.getUsuario());
        verify(checkInRepository, times(1)).save(any(CheckIn.class));
    }

    @Test
    void registrarEntradaQr_sinSuscripcionActiva_lanzaExcepcion() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(suscripcionRepository.findFirstByUsuarioIdAndEstado(1L, EstadoSuscripcion.ACTIVA))
                .thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> checkInService.registrarEntradaQr(1L));
    }

    @Test
    void registrarEntradaQr_yaTieneIngresoAbierto_lanzaExcepcion() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(suscripcionRepository.findFirstByUsuarioIdAndEstado(1L, EstadoSuscripcion.ACTIVA))
                .thenReturn(Optional.of(suscripcion));
        when(checkInRepository.findFirstByUsuarioIdAndFechaHoraSalidaIsNull(1L))
                .thenReturn(Optional.of(checkInAbierto));

        assertThrows(BusinessException.class, () -> checkInService.registrarEntradaQr(1L));
    }

    @Test
    void registrarSalida_exito() {
        when(checkInRepository.findFirstByUsuarioIdAndFechaHoraSalidaIsNull(1L))
                .thenReturn(Optional.of(checkInAbierto));
        when(checkInRepository.save(any(CheckIn.class))).thenAnswer(inv -> inv.getArgument(0));

        CheckIn resultado = checkInService.registrarSalida(1L);

        assertNotNull(resultado);
        assertNotNull(resultado.getFechaHoraSalida());
        verify(checkInRepository, times(1)).save(checkInAbierto);
    }

    @Test
    void registrarSalida_sinIngresoAbierto_lanzaExcepcion() {
        when(checkInRepository.findFirstByUsuarioIdAndFechaHoraSalidaIsNull(1L))
                .thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> checkInService.registrarSalida(1L));
    }

    @Test
    void registrarEntradaQr_usuarioNoExiste_lanzaExcepcion() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> checkInService.registrarEntradaQr(99L));
    }
}
