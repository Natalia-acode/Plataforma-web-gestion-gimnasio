package com.gimnasio.domain;

import com.gimnasio.domain.enums.MetodoCheckIn;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** Entradas y salidas (CU-13, CU-14). Base del calculo de aforo RN-11. */
@Entity
@Table(name = "check_in")
public class CheckIn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_checkin")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    /** NULL cuando el ingreso fue con QR. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_recepcionista")
    private Usuario recepcionista;

    @Column(name = "fecha_hora_entrada", nullable = false, columnDefinition = "datetime2(0)")
    private LocalDateTime fechaHoraEntrada;

    /** NULL = la persona sigue dentro del gimnasio. */
    @Column(name = "fecha_hora_salida", columnDefinition = "datetime2(0)")
    private LocalDateTime fechaHoraSalida;

    @Column(nullable = false, length = 10)
    private MetodoCheckIn metodo;

    public boolean estaDentro() {
        return fechaHoraSalida == null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Usuario getRecepcionista() {
        return recepcionista;
    }

    public void setRecepcionista(Usuario recepcionista) {
        this.recepcionista = recepcionista;
    }

    public LocalDateTime getFechaHoraEntrada() {
        return fechaHoraEntrada;
    }

    public void setFechaHoraEntrada(LocalDateTime fechaHoraEntrada) {
        this.fechaHoraEntrada = fechaHoraEntrada;
    }

    public LocalDateTime getFechaHoraSalida() {
        return fechaHoraSalida;
    }

    public void setFechaHoraSalida(LocalDateTime fechaHoraSalida) {
        this.fechaHoraSalida = fechaHoraSalida;
    }

    public MetodoCheckIn getMetodo() {
        return metodo;
    }

    public void setMetodo(MetodoCheckIn metodo) {
        this.metodo = metodo;
    }
}
