package com.gimnasio.domain.converter;

import com.gimnasio.domain.enums.EstadoPago;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EstadoPagoConverter implements AttributeConverter<EstadoPago, String> {

    @Override
    public String convertToDatabaseColumn(EstadoPago atributo) {
        return atributo == null ? null : atributo.getValor();
    }

    @Override
    public EstadoPago convertToEntityAttribute(String datos) {
        return datos == null ? null : EstadoPago.desdeValor(datos);
    }
}
