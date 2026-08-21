package com.tallerdae.cotizador.domain.model;

/**
 * Estrategia de precios sin recargo por urgencia.
 * Siempre retorna {@link Dinero#ZERO} como recargo.
 */
public class NormalPricingStrategy implements UrgencyPricingStrategy {

    @Override
    public Dinero calcularRecargo(Dinero subtotal) {
        return Dinero.ZERO;
    }
}
