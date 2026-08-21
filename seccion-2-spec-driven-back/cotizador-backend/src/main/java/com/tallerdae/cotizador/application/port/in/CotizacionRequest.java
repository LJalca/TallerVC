package com.tallerdae.cotizador.application.port.in;

import com.tallerdae.cotizador.domain.model.NivelUrgencia;

import java.util.List;
import java.util.UUID;

/**
 * DTO de entrada para el caso de uso de generación de cotizaciones.
 */
public record CotizacionRequest(
        UUID calzadoId,
        List<UUID> reparacionIds,
        NivelUrgencia nivelUrgencia
) {}
