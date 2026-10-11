package com.gimnasio.domain;

import com.gimnasio.domain.enums.EstadoReserva;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Reservas de clase y lista de espera (CU-18, CU-19, CU-20). */
@Entity
@Table(name = "reserva_clase")
public class ReservaClase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reserva")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_clase", nullable = false)
    private Clase clase;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    /** Fecha de la sesión reservada. */
    @Column(nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoReserva estado;

    /** Posición cuando estado = en_espera (RN-09). */
    @Column(name = "posicion_espera", columnDefinition = "smallint")
    private Integer posicionEspera;

    @Column(name = "fecha_reserva", nullable = false, columnDefinition = "datetime2(0)")
    private LocalDateTime fechaReserva;

    @PrePersist
    void prePersist() {
        if (fechaReserva == null) {
            fechaReserva = LocalDateTime.now();
        }
    }

    public boolean estaVigente() {
        return estado == EstadoReserva.CONFIRMADA || estado == EstadoReserva.EN_ESPERA;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Clase getClase() {
        return clase;
    }

    public void setClase(Clase clase) {
        this.clase = clase;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
    }

    public Integer getPosicionEspera() {
        return posicionEspera;
    }

    public void setPosicionEspera(Integer posicionEspera) {
        this.posicionEspera = posicionEspera;
    }

    public LocalDateTime getFechaReserva() {
        return fechaReserva;
    }

    public void setFechaReserva(LocalDateTime fechaReserva) {
        this.fechaReserva = fechaReserva;
    }
}
