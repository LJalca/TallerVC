package com.tallerdae.cotizador.domain.model;

/**
 * Estrategia de cálculo de recargo según el nivel de urgencia.
 * Implementaciones: {@link NormalPricingStrategy}, {@link UrgentPricingStrategy}.
 */
public interface UrgencyPricingStrategy {

    /**
     * Calcula el recargo a aplicar sobre el subtotal de una cotización.
     *
     * @param subtotal el subtotal de la cotización antes de aplicar recargo
     * @return el monto del recargo (puede ser cero)
     */
    Dinero calcularRecargo(Dinero subtotal);
}
