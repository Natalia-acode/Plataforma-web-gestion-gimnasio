package com.gimnasio.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalTime;

/** Parametros del sistema (CU-17). Tabla de fila unica. */
@Entity
@Table(name = "configuracion")
public class Configuracion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_config")
    private Long id;

    @Column(name = "capacidad_maxima", nullable = false)
    private Integer capacidadMaxima;

    /** % de ocupacion para el semaforo amarillo. */
    @Column(name = "umbral_medio", nullable = false, columnDefinition = "tinyint")
    private byte umbralMedio;

    /** % de ocupacion para el semaforo rojo. */
    @Column(name = "umbral_alto", nullable = false, columnDefinition = "tinyint")
    private byte umbralAlto;

    /** Minutos para presentarse al turno llamado (RN-08). */
    @Column(name = "ventana_turno_min", nullable = false)
    private Integer ventanaTurnoMin;

    /** Estimacion de espera por persona delante (CU-23). */
    @Column(name = "tiempo_rotacion_min", nullable = false)
    private Integer tiempoRotacionMin;

    /** RN-04: numero de reintentos de cobro antes de vencer la suscripcion. */
    @Column(name = "reintentos_cobro", nullable = false)
    private Integer reintentosCobro;

    @Column(name = "horario_apertura", nullable = false, columnDefinition = "time(0)")
    private LocalTime horarioApertura;

    @Column(name = "horario_cierre", nullable = false, columnDefinition = "time(0)")
    private LocalTime horarioCierre;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getCapacidadMaxima() {
        return capacidadMaxima;
    }

    public void setCapacidadMaxima(Integer capacidadMaxima) {
        this.capacidadMaxima = capacidadMaxima;
    }

    public byte getUmbralMedio() {
        return umbralMedio;
    }

    public void setUmbralMedio(byte umbralMedio) {
        this.umbralMedio = umbralMedio;
    }

    public byte getUmbralAlto() {
        return umbralAlto;
    }

    public void setUmbralAlto(byte umbralAlto) {
        this.umbralAlto = umbralAlto;
    }

    public Integer getVentanaTurnoMin() {
        return ventanaTurnoMin;
    }

    public void setVentanaTurnoMin(Integer ventanaTurnoMin) {
        this.ventanaTurnoMin = ventanaTurnoMin;
    }

    public Integer getTiempoRotacionMin() {
        return tiempoRotacionMin;
    }

    public void setTiempoRotacionMin(Integer tiempoRotacionMin) {
        this.tiempoRotacionMin = tiempoRotacionMin;
    }

    public Integer getReintentosCobro() {
        return reintentosCobro;
    }

    public void setReintentosCobro(Integer reintentosCobro) {
        this.reintentosCobro = reintentosCobro;
    }

    public LocalTime getHorarioApertura() {
        return horarioApertura;
    }

    public void setHorarioApertura(LocalTime horarioApertura) {
        this.horarioApertura = horarioApertura;
    }

    public LocalTime getHorarioCierre() {
        return horarioCierre;
    }

    public void setHorarioCierre(LocalTime horarioCierre) {
        this.horarioCierre = horarioCierre;
    }
}
