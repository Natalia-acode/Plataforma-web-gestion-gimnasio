package com.gimnasio.domain.converter;

import com.gimnasio.domain.enums.EstadoReserva;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EstadoReservaConverter implements AttributeConverter<EstadoReserva, String> {

    @Override
    public String convertToDatabaseColumn(EstadoReserva atributo) {
        return atributo == null ? null : atributo.getValor();
    }

    @Override
    public EstadoReserva convertToEntityAttribute(String datos) {
        return datos == null ? null : EstadoReserva.desdeValor(datos);
    }
}
