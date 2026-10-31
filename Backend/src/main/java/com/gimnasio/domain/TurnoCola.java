package com.gimnasio.domain;

import com.gimnasio.domain.enums.EstadoTurno;
import com.gimnasio.domain.enums.TipoTurno;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** Cola virtual (CU-22 a CU-25). */
@Entity
@Table(name = "turno_cola")
public class TurnoCola {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_turno")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 10)
    private TipoTurno tipo;

    @Column(name = "fecha_solicitud", nullable = false, columnDefinition = "datetime2(0)")
    private LocalDateTime fechaSolicitud;

    @Column(nullable = false)
    private Integer posicion;

    @Column(nullable = false, length = 20)
    private EstadoTurno estado;

    @Column(name = "hora_llamado", columnDefinition = "datetime2(0)")
    private LocalDateTime horaLLamado;

    @Column(name = "hora_atencion", columnDefinition = "datetime2(0)")
    private LocalDateTime horaAtencion;

    @PrePersist
    void prePersist() {
        if (fechaSolicitud == null) {
            fechaSolicitud = LocalDateTime.now();
        }
    }

    public boolean estaActivo() {
        return estado == EstadoTurno.ESPERANDO || estado == EstadoTurno.LLAMADO;
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

    public TipoTurno getTipo() {
        return tipo;
    }

    public void setTipo(TipoTurno tipo) {
        this.tipo = tipo;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public Integer getPosicion() {
        return posicion;
    }

    public void setPosicion(Integer posicion) {
        this.posicion = posicion;
    }

    public EstadoTurno getEstado() {
        return estado;
    }

    public void setEstado(EstadoTurno estado) {
        this.estado = estado;
    }

    public LocalDateTime getHoraLLamado() {
        return horaLLamado;
    }

    public void setHoraLLamado(LocalDateTime horaLLamado) {
        this.horaLLamado = horaLLamado;
    }

    public LocalDateTime getHoraAtencion() {
        return horaAtencion;
    }

    public void setHoraAtencion(LocalDateTime horaAtencion) {
        this.horaAtencion = horaAtencion;
    }
}
