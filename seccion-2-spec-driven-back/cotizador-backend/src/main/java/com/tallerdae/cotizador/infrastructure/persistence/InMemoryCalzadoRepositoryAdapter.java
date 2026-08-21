package com.tallerdae.cotizador.infrastructure.persistence;

import com.tallerdae.cotizador.application.port.out.CalzadoRepositoryPort;
import com.tallerdae.cotizador.domain.model.Calzado;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class InMemoryCalzadoRepositoryAdapter implements CalzadoRepositoryPort {

    public static final UUID ID_BOTA_CUERO         = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID ID_ZAPATILLA_DEPORTIVA = UUID.fromString("22222222-2222-2222-2222-222222222222");
    public static final UUID ID_ZAPATO_FORMAL       = UUID.fromString("33333333-3333-3333-3333-333333333333");
    public static final UUID ID_SANDALIA            = UUID.fromString("44444444-4444-4444-4444-444444444444");

    private final Map<UUID, Calzado> almacenamiento;

    public InMemoryCalzadoRepositoryAdapter() {
        this.almacenamiento = new HashMap<>();
        almacenamiento.put(ID_BOTA_CUERO,         new Calzado(ID_BOTA_CUERO,         "Bota de cuero",       new BigDecimal("1.50")));
        almacenamiento.put(ID_ZAPATILLA_DEPORTIVA, new Calzado(ID_ZAPATILLA_DEPORTIVA, "Zapatilla deportiva", new BigDecimal("1.10")));
        almacenamiento.put(ID_ZAPATO_FORMAL,       new Calzado(ID_ZAPATO_FORMAL,       "Zapato formal",       new BigDecimal("1.25")));
        almacenamiento.put(ID_SANDALIA,            new Calzado(ID_SANDALIA,            "Sandalia",            new BigDecimal("0.90")));
    }

    @Override
    public List<Calzado> listarTodos() {
        return new ArrayList<>(almacenamiento.values());
    }

    @Override
    public Optional<Calzado> buscarPorId(UUID id) {
        return Optional.ofNullable(almacenamiento.get(id));
    }
}
