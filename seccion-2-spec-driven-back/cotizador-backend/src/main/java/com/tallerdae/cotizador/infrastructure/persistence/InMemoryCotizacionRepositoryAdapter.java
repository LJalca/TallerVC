package com.tallerdae.cotizador.infrastructure.persistence;

import com.tallerdae.cotizador.application.port.out.CotizacionRepositoryPort;
import com.tallerdae.cotizador.domain.model.Cotizacion;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de persistencia en memoria para {@link Cotizacion}.
 *
 * <p>Implementa {@link CotizacionRepositoryPort}. Las cotizaciones se
 * almacenan en un {@link HashMap} indexado por su UUID, sin datos semilla
 * iniciales.</p>
 */
@Repository
public class InMemoryCotizacionRepositoryAdapter implements CotizacionRepositoryPort {

    private final Map<UUID, Cotizacion> almacenamiento = new HashMap<>();

    /**
     * Persiste la cotización en memoria, indexada por su id.
     *
     * @param cotizacion la cotización a guardar
     */
    @Override
    public void guardar(Cotizacion cotizacion) {
        almacenamiento.put(cotizacion.getId(), cotizacion);
    }

    /**
     * Recupera una cotización por su id.
     *
     * <p>Este método no forma parte del puerto {@link CotizacionRepositoryPort};
     * se expone como utilidad para pruebas de persistencia (Propiedad 6).</p>
     *
     * @param id UUID de la cotización a buscar
     * @return {@link Optional} con la cotización si existe, vacío en caso contrario
     */
    public Optional<Cotizacion> buscarPorId(UUID id) {
        return Optional.ofNullable(almacenamiento.get(id));
    }
}
