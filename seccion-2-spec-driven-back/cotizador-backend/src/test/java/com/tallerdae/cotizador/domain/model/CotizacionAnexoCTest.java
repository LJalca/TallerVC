package com.tallerdae.cotizador.domain.model;

import com.tallerdae.cotizador.domain.exception.ValidacionException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pruebas equivalentes a los escenarios Gherkin del Anexo C (sección 3.7).
 */
class CotizacionAnexoCTest {

    @Test
    void cotizacionSimpleSinUrgencia_calculaTotalCorrecto() {
        Calzado zapatoFormal = new Calzado(UUID.randomUUID(), "Zapato formal", new BigDecimal("1.2"));
        Reparacion cambioTacon = new Reparacion(UUID.randomUUID(), "Cambio de tacón", new BigDecimal("12.00"), 2);

        Cotizacion cotizacion = Cotizacion.crear(
                zapatoFormal, List.of(cambioTacon), NivelUrgencia.NORMAL, new NormalPricingStrategy());

        assertEquals(0, cotizacion.getTotal().getMonto().compareTo(new BigDecimal("14.40")));
        assertEquals(2, cotizacion.getTiempoEstimadoDias());
    }

    @Test
    void cotizacionUrgente_aplicaRecargoYReduceTiempo() {
        Calzado botaDeCuero = new Calzado(UUID.randomUUID(), "Bota de cuero", new BigDecimal("1.5"));
        Reparacion cambioSuela = new Reparacion(UUID.randomUUID(), "Cambio de suela", new BigDecimal("20.00"), 4);

        Cotizacion cotizacion = Cotizacion.crear(
                botaDeCuero, List.of(cambioSuela), NivelUrgencia.URGENTE, new UrgentPricingStrategy());

        assertEquals(0, cotizacion.getSubtotal().getMonto().compareTo(new BigDecimal("30.00")));
        assertEquals(0, cotizacion.getTotal().getMonto().compareTo(new BigDecimal("39.00")));
        assertEquals(2, cotizacion.getTiempoEstimadoDias());
    }

    @Test
    void cotizacionSinReparaciones_lanzaValidacionException() {
        Calzado calzado = new Calzado(UUID.randomUUID(), "Zapatilla deportiva", new BigDecimal("1.0"));

        assertThrows(ValidacionException.class, () ->
                Cotizacion.crear(calzado, List.of(), NivelUrgencia.NORMAL, new NormalPricingStrategy()));
    }
}
