package com.gimnasio.domain;

import com.gimnasio.domain.enums.EstadoSuscripcion;
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
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "suscripcion")
public class Suscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_suscripcion")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_plan", nullable = false)
    private Plan plan;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSuscripcion estado;

    /** RN-01: la renovación automática es opt-in (por defecto desactivada). */
    @Column(name = "renovacion_automatica", nullable = false, columnDefinition = "bit")
    private boolean renovacionAutomatica = false;

    /** RN-04: reintentos de cobro de renovación ya utilizados (+1 / +3 / +7 días). */
    @Column(name = "intentos_cobro", nullable = false)
    private Integer intentosCobro = 0;

    @Column(name = "fecha_cancelacion", columnDefinition = "datetime2(0)")
    private LocalDateTime fechaCancelacion;

    @Column(name = "motivo_cancelacion", length = 255)
    private String motivoCancelacion;

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

    public Plan getPlan() {
        return plan;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public EstadoSuscripcion getEstado() {
        return estado;
    }

    public void setEstado(EstadoSuscripcion estado) {
        this.estado = estado;
    }

    public boolean isRenovacionAutomatica() {
        return renovacionAutomatica;
    }

    public void setRenovacionAutomatica(boolean renovacionAutomatica) {
        this.renovacionAutomatica = renovacionAutomatica;
    }

    public Integer getIntentosCobro() {
        return intentosCobro;
    }

    public void setIntentosCobro(Integer intentosCobro) {
        this.intentosCobro = intentosCobro;
    }

    public LocalDateTime getFechaCancelacion() {
        return fechaCancelacion;
    }

    public void setFechaCancelacion(LocalDateTime fechaCancelacion) {
        this.fechaCancelacion = fechaCancelacion;
    }

    public String getMotivoCancelacion() {
        return motivoCancelacion;
    }

    public void setMotivoCancelacion(String motivoCancelacion) {
        this.motivoCancelacion = motivoCancelacion;
    }
}
