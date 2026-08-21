package com.tallerdae.cotizador.infrastructure.rest;

import java.math.BigDecimal;
import java.util.UUID;

public record CalzadoResponse(UUID id, String nombre, BigDecimal factorComplejidad) {}
