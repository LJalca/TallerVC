package com.tallerdae.cotizador.infrastructure.persistence;

import com.tallerdae.cotizador.application.port.out.ReparacionRepositoryPort;
import com.tallerdae.cotizador.domain.model.Reparacion;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class InMemoryReparacionRepositoryAdapter implements ReparacionRepositoryPort {

    public static final UUID ID_CAMBIO_SUELA     = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    public static final UUID ID_LIMPIEZA_PROFUNDA = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    public static final UUID ID_COSTURA_REFUERZO  = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
    public static final UUID ID_TINTADO           = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");

    private final Map<UUID, Reparacion> almacenamiento;

    public InMemoryReparacionRepositoryAdapter() {
        almacenamiento = new HashMap<>();
        almacenamiento.put(ID_CAMBIO_SUELA,
                new Reparacion(ID_CAMBIO_SUELA, "Cambio de suela", new BigDecimal("35000"), 5));
        almacenamiento.put(ID_LIMPIEZA_PROFUNDA,
                new Reparacion(ID_LIMPIEZA_PROFUNDA, "Limpieza profunda", new BigDecimal("15000"), 2));
        almacenamiento.put(ID_COSTURA_REFUERZO,
                new Reparacion(ID_COSTURA_REFUERZO, "Costura de refuerzo", new BigDecimal("20000"), 3));
        almacenamiento.put(ID_TINTADO,
                new Reparacion(ID_TINTADO, "Tintado", new BigDecimal("25000"), 4));
    }

    @Override
    public List<Reparacion> listarTodos() {
        return new ArrayList<>(almacenamiento.values());
    }

    @Override
    public Optional<Reparacion> buscarPorId(UUID id) {
        return Optional.ofNullable(almacenamiento.get(id));
    }
}
