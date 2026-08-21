package com.tallerdae.cotizador.application.port.out;

import com.tallerdae.cotizador.domain.model.Calzado;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CalzadoRepositoryPort {

    List<Calzado> listarTodos();

    Optional<Calzado> buscarPorId(UUID id);
}
