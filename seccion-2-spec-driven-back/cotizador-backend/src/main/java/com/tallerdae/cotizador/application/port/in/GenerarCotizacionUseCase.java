package com.tallerdae.cotizador.application.port.in;

import com.tallerdae.cotizador.domain.model.Cotizacion;

/**
 * Puerto de entrada para la generación de cotizaciones.
 */
public interface GenerarCotizacionUseCase {

    /**
     * Genera una cotización a partir del request proporcionado.
     *
     * @param request datos de entrada con calzado, reparaciones y nivel de urgencia
     * @return la cotización generada y persistida
     */
    Cotizacion generarCotizacion(CotizacionRequest request);
}
