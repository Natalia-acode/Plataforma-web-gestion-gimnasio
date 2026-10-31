package com.gimnasio.domain;

import com.gimnasio.domain.enums.EstadoPago;
import com.gimnasio.domain.enums.TipoPago;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pago")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_suscripcion", nullable = false)
    private Suscripcion suscripcion;

    /** NULL cuando el pago fue en efectivo (CU-30). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_metodo")
    private MetodoPago metodo;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(name = "fecha_pago", nullable = false, columnDefinition = "datetime2(0)")
    private LocalDateTime fechaPago;

    @Column(nullable = false, length = 20)
    private TipoPago tipo;

    @Column(nullable = false, length = 20)
    private EstadoPago estado;

    @Column(name = "numero_factura", length = 30)
    private String numeroFactura;

    @Column(name = "id_transaccion_pasarela", length = 100)
    private String idTransaccionPasarela;

    @PrePersist
    void prePersist() {
        if (fechaPago == null) {
            fechaPago = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Suscripcion getSuscripcion() {
        return suscripcion;
    }

    public void setSuscripcion(Suscripcion suscripcion) {
        this.suscripcion = suscripcion;
    }

    public MetodoPago getMetodo() {
        return metodo;
    }

    public void setMetodo(MetodoPago metodo) {
        this.metodo = metodo;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }

    public TipoPago getTipo() {
        return tipo;
    }

    public void setTipo(TipoPago tipo) {
        this.tipo = tipo;
    }

    public EstadoPago getEstado() {
        return estado;
    }

    public void setEstado(EstadoPago estado) {
        this.estado = estado;
    }

    public String getNumeroFactura() {
        return numeroFactura;
    }

    public void setNumeroFactura(String numeroFactura) {
        this.numeroFactura = numeroFactura;
    }

    public String getIdTransaccionPasarela() {
        return idTransaccionPasarela;
    }

    public void setIdTransaccionPasarela(String idTransaccionPasarela) {
        this.idTransaccionPasarela = idTransaccionPasarela;
    }
}
