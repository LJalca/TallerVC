package com.tallerdae.cotizador.domain.model;

import java.math.BigDecimal;

/**
 * Estrategia de precios con recargo del 30 % por urgencia.
 * Retorna {@code subtotal.aplicarPorcentaje(0.30)} como recargo.
 */
public class UrgentPricingStrategy implements UrgencyPricingStrategy {

    private static final BigDecimal RECARGO_URGENCIA_PORCENTAJE = new BigDecimal("0.30");

    @Override
    public Dinero calcularRecargo(Dinero subtotal) {
        return subtotal.aplicarPorcentaje(RECARGO_URGENCIA_PORCENTAJE);
    }
}
