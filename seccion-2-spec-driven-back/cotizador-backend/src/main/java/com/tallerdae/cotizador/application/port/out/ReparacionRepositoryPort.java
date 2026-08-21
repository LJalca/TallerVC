package com.tallerdae.cotizador.application.port.out;

import com.tallerdae.cotizador.domain.model.Reparacion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReparacionRepositoryPort {

    List<Reparacion> listarTodos();

    Optional<Reparacion> buscarPorId(UUID id);
}
