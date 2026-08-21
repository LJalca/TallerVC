package com.tallerdae.cotizador.infrastructure.rest;

import java.math.BigDecimal;
import java.util.UUID;

public record ReparacionResponse(UUID id, String nombre, BigDecimal precioBase, int tiempoEstimadoDias) {}
