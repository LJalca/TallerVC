package com.tallerdae.cotizador.infrastructure.rest;

import com.tallerdae.cotizador.domain.model.NivelUrgencia;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CotizacionResponse(
    UUID id,
    String fechaCreacion,
    UUID calzadoId,
    String nombreCalzado,
    List<UUID> reparacionIds,
    NivelUrgencia nivelUrgencia,
    BigDecimal subtotal,
    BigDecimal recargoUrgencia,
    BigDecimal total,
    String moneda,
    int tiempoEstimadoDias
) {}
