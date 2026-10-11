package com.gimnasio.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

/** Analítica horaria de ocupación; alimenta el mapa de calor (CU-16). */
@Entity
@Table(name = "registro_aforo")
public class RegistroAforo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_registro")
    private Long id;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false, columnDefinition = "tinyint")
    private byte hora;

    @Column(name = "ocupacion_promedio", nullable = false)
    private Integer ocupacionPromedio;

    @Column(name = "ocupacion_maxima", nullable = false)
    private Integer ocupacionMaxima;

    @Column(name = "total_ingresos", nullable = false)
    private Integer totalIngresos;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public byte getHora() {
        return hora;
    }

    public void setHora(byte hora) {
        this.hora = hora;
    }

    public Integer getOcupacionPromedio() {
        return ocupacionPromedio;
    }

    public void setOcupacionPromedio(Integer ocupacionPromedio) {
        this.ocupacionPromedio = ocupacionPromedio;
    }

    public Integer getOcupacionMaxima() {
        return ocupacionMaxima;
    }

    public void setOcupacionMaxima(Integer ocupacionMaxima) {
        this.ocupacionMaxima = ocupacionMaxima;
    }

    public Integer getTotalIngresos() {
        return totalIngresos;
    }

    public void setTotalIngresos(Integer totalIngresos) {
        this.totalIngresos = totalIngresos;
    }
}
