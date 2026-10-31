package com.gimnasio.domain.converter;

import com.gimnasio.domain.enums.TipoPago;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TipoPagoConverter implements AttributeConverter<TipoPago, String> {

    @Override
    public String convertToDatabaseColumn(TipoPago atributo) {
        return atributo == null ? null : atributo.getValor();
    }

    @Override
    public TipoPago convertToEntityAttribute(String datos) {
        return datos == null ? null : TipoPago.desdeValor(datos);
    }
}
