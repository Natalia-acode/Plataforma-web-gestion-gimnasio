package com.gimnasio.domain;

import com.gimnasio.domain.enums.TipoMetodoPago;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Tarjeta tokenizada. RNF-02 (PCI-DSS): en esta tabla NUNCA se almacenan
 * los datos completos de la tarjeta; solo el token emitido por la pasarela.
 */
@Entity
@Table(name = "metodo_pago")
public class MetodoPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_metodo")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 20)
    private TipoMetodoPago tipo;

    @Column(name = "ultimos_cuatro", nullable = false, columnDefinition = "nchar(4)")
    private String ultimosCuatro;

    @Column(length = 120)
    private String titular;

    @Column(name = "token_pasarela", nullable = false, length = 255)
    private String tokenPasarela;

    @Column(nullable = false, columnDefinition = "bit")
    private boolean predeterminado = false;

    @Column(nullable = false, columnDefinition = "bit")
    private boolean activo = true;

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

    public TipoMetodoPago getTipo() {
        return tipo;
    }

    public void setTipo(TipoMetodoPago tipo) {
        this.tipo = tipo;
    }

    public String getUltimosCuatro() {
        return ultimosCuatro;
    }

    public void setUltimosCuatro(String ultimosCuatro) {
        this.ultimosCuatro = ultimosCuatro;
    }

    public String getTitular() {
        return titular;
    }

    public void setTitular(String titular) {
        this.titular = titular;
    }

    public String getTokenPasarela() {
        return tokenPasarela;
    }

    public void setTokenPasarela(String tokenPasarela) {
        this.tokenPasarela = tokenPasarela;
    }

    public boolean isPredeterminado() {
        return predeterminado;
    }

    public void setPredeterminado(boolean predeterminado) {
        this.predeterminado = predeterminado;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
