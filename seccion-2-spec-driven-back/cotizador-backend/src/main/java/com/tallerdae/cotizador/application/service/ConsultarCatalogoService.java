package com.tallerdae.cotizador.application.service;

import com.tallerdae.cotizador.application.port.in.ConsultarCatalogoUseCase;
import com.tallerdae.cotizador.application.port.out.CalzadoRepositoryPort;
import com.tallerdae.cotizador.application.port.out.ReparacionRepositoryPort;
import com.tallerdae.cotizador.domain.model.Calzado;
import com.tallerdae.cotizador.domain.model.Reparacion;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementación del caso de uso de consulta del catálogo.
 * Delega directamente a los repositorios correspondientes sin lógica de negocio adicional.
 */
@Service
public class ConsultarCatalogoService implements ConsultarCatalogoUseCase {

    private final CalzadoRepositoryPort calzadoRepository;
    private final ReparacionRepositoryPort reparacionRepository;

    public ConsultarCatalogoService(
            CalzadoRepositoryPort calzadoRepository,
            ReparacionRepositoryPort reparacionRepository) {
        this.calzadoRepository = calzadoRepository;
        this.reparacionRepository = reparacionRepository;
    }

    @Override
    public List<Calzado> listarCalzados() {
        return calzadoRepository.listarTodos();
    }

    @Override
    public List<Reparacion> listarReparaciones() {
        return reparacionRepository.listarTodos();
    }
}
