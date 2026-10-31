package com.gimnasio.domain.converter;

import com.gimnasio.domain.enums.Rol;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Convierte {@link Rol} entre el nombre de la constante (Java) y su valor en minúsculas (BD).
 * El DDL inserta valores como 'administrador', mientras que las constantes Java son ADMINISTRADOR.
 */
@Converter(autoApply = true)
public class RolConverter implements AttributeConverter<Rol, String> {

    @Override
    public String convertToDatabaseColumn(Rol atributo) {
        return atributo == null ? null : atributo.getValor();
    }

    @Override
    public Rol convertToEntityAttribute(String datos) {
        return datos == null ? null : Rol.desdeValor(datos);
    }
}
