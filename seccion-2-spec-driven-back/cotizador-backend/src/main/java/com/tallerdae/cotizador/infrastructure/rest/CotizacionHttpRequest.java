package com.tallerdae.cotizador.infrastructure.rest;

import java.util.List;
import java.util.UUID;

public record CotizacionHttpRequest(UUID tipoCalzadoId, List<UUID> tipoReparacionIds, boolean urgente) {}
