package com.tallerdae.cotizador.infrastructure.rest;

import com.tallerdae.cotizador.domain.model.Calzado;
import com.tallerdae.cotizador.domain.model.Cotizacion;
import com.tallerdae.cotizador.domain.model.Reparacion;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper responsable de convertir entre entidades de dominio y DTOs REST.
 * Es el único lugar con conocimiento de ambos mundos; el dominio nunca
 * importa clases de la capa HTTP.
 */
@Component
public class CotizacionMapper {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * Convierte una {@link Cotizacion} de dominio a {@link CotizacionResponse}.
     *
     * @param cotizacion entidad de dominio
     * @return DTO listo para serializar como respuesta HTTP
     */
    public CotizacionResponse toResponse(Cotizacion cotizacion) {
        List<java.util.UUID> reparacionIds = cotizacion.getReparaciones()
                .stream()
                .map(Reparacion::getId)
                .collect(Collectors.toList());

        return new CotizacionResponse(
                cotizacion.getId(),
                cotizacion.getFechaCreacion().format(FORMATTER),
                cotizacion.getCalzado().getId(),
                cotizacion.getCalzado().getNombre(),
                reparacionIds,
                cotizacion.getNivelUrgencia(),
                cotizacion.getSubtotal().getMonto(),
                cotizacion.getRecargo().getMonto(),
                cotizacion.getTotal().getMonto(),
                cotizacion.getTotal().getMoneda(),
                cotizacion.getTiempoEstimadoDias()
        );
    }

    /**
     * Convierte un {@link Calzado} de dominio a {@link CalzadoResponse}.
     *
     * @param calzado entidad de dominio
     * @return DTO de catálogo de calzados
     */
    public CalzadoResponse toCalzadoResponse(Calzado calzado) {
        return new CalzadoResponse(
                calzado.getId(),
                calzado.getNombre(),
                calzado.getFactorComplejidad()
        );
    }

    /**
     * Convierte una {@link Reparacion} de dominio a {@link ReparacionResponse}.
     *
     * @param reparacion entidad de dominio
     * @return DTO de catálogo de reparaciones
     */
    public ReparacionResponse toReparacionResponse(Reparacion reparacion) {
        return new ReparacionResponse(
                reparacion.getId(),
                reparacion.getNombre(),
                reparacion.getPrecioBase(),
                reparacion.getTiempoEstimadoDias()
        );
    }
}
