package com.tallerdae.cotizador.application.port.out;

import com.tallerdae.cotizador.domain.model.Cotizacion;

public interface CotizacionRepositoryPort {

    void guardar(Cotizacion cotizacion);
}
