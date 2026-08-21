package com.tallerdae.cotizador.application.port.in;

import com.tallerdae.cotizador.domain.model.Calzado;
import com.tallerdae.cotizador.domain.model.Reparacion;

import java.util.List;

/**
 * Puerto de entrada para la consulta del catálogo de calzados y reparaciones.
 */
public interface ConsultarCatalogoUseCase {

    /**
     * Retorna la lista completa de tipos de calzado disponibles.
     *
     * @return lista de calzados
     */
    List<Calzado> listarCalzados();

    /**
     * Retorna la lista completa de tipos de reparación disponibles.
     *
     * @return lista de reparaciones
     */
    List<Reparacion> listarReparaciones();
}
