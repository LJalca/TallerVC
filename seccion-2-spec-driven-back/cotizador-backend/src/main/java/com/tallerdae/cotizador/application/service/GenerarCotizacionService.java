package com.tallerdae.cotizador.application.service;

import com.tallerdae.cotizador.application.port.in.CotizacionRequest;
import com.tallerdae.cotizador.application.port.in.GenerarCotizacionUseCase;
import com.tallerdae.cotizador.application.port.out.CalzadoRepositoryPort;
import com.tallerdae.cotizador.application.port.out.CotizacionRepositoryPort;
import com.tallerdae.cotizador.application.port.out.ReparacionRepositoryPort;
import com.tallerdae.cotizador.domain.exception.RecursoNoEncontradoException;
import com.tallerdae.cotizador.domain.exception.ValidacionException;
import com.tallerdae.cotizador.domain.model.Calzado;
import com.tallerdae.cotizador.domain.model.Cotizacion;
import com.tallerdae.cotizador.domain.model.NivelUrgencia;
import com.tallerdae.cotizador.domain.model.NormalPricingStrategy;
import com.tallerdae.cotizador.domain.model.Reparacion;
import com.tallerdae.cotizador.domain.model.UrgencyPricingStrategy;
import com.tallerdae.cotizador.domain.model.UrgentPricingStrategy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Servicio de aplicación que orquesta la generación de cotizaciones.
 *
 * <p>Implementa {@link GenerarCotizacionUseCase}: valida la entrada, resuelve
 * las entidades de dominio a través de los puertos de salida, selecciona la
 * estrategia de precios correspondiente y delega la lógica de negocio a
 * {@link Cotizacion#crear(Calzado, List, NivelUrgencia, UrgencyPricingStrategy)}.</p>
 */
@Service
public class GenerarCotizacionService implements GenerarCotizacionUseCase {

    private final CalzadoRepositoryPort calzadoRepository;
    private final ReparacionRepositoryPort reparacionRepository;
    private final CotizacionRepositoryPort cotizacionRepository;

    public GenerarCotizacionService(
            CalzadoRepositoryPort calzadoRepository,
            ReparacionRepositoryPort reparacionRepository,
            CotizacionRepositoryPort cotizacionRepository) {
        this.calzadoRepository = calzadoRepository;
        this.reparacionRepository = reparacionRepository;
        this.cotizacionRepository = cotizacionRepository;
    }

    /**
     * Genera una cotización a partir del request proporcionado.
     *
     * <p>Flujo de orquestación:</p>
     * <ol>
     *   <li>Valida que {@code reparacionIds} no sea nulo ni vacío (RN-01).</li>
     *   <li>Resuelve el {@link Calzado} por id; lanza {@link RecursoNoEncontradoException} si no existe.</li>
     *   <li>Resuelve cada {@link Reparacion} por id; acumula los ids faltantes y lanza
     *       {@link RecursoNoEncontradoException} con todos ellos si alguno no existe.</li>
     *   <li>Selecciona la {@link UrgencyPricingStrategy} según el nivel de urgencia.</li>
     *   <li>Crea la {@link Cotizacion} mediante el factory method del dominio.</li>
     *   <li>Persiste la cotización.</li>
     *   <li>Retorna la cotización generada.</li>
     * </ol>
     *
     * @param request datos de entrada con calzado, reparaciones y nivel de urgencia
     * @return la cotización generada y persistida
     * @throws ValidacionException          si la lista de reparaciones es nula o vacía
     * @throws RecursoNoEncontradoException si el calzado o alguna reparación no existe en el catálogo
     */
    @Override
    public Cotizacion generarCotizacion(CotizacionRequest request) {

        // 1. Validar que reparacionIds no esté vacío (RN-01)
        if (request.reparacionIds() == null || request.reparacionIds().isEmpty()) {
            throw new ValidacionException("Se requiere al menos una reparación seleccionada");
        }

        // 2. Resolver Calzado
        Calzado calzado = calzadoRepository.buscarPorId(request.calzadoId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Calzado con id '" + request.calzadoId() + "' no fue encontrado"));

        // 3. Resolver cada Reparacion; acumular los ids no encontrados
        List<UUID> idsNoEncontrados = new ArrayList<>();
        List<Reparacion> reparaciones = new ArrayList<>();

        for (UUID id : request.reparacionIds()) {
            reparacionRepository.buscarPorId(id)
                    .ifPresentOrElse(
                            reparaciones::add,
                            () -> idsNoEncontrados.add(id));
        }

        if (!idsNoEncontrados.isEmpty()) {
            throw new RecursoNoEncontradoException(
                    "Reparaciones no encontradas: " + idsNoEncontrados);
        }

        // 4. Seleccionar estrategia de precios según nivel de urgencia
        UrgencyPricingStrategy strategy = request.nivelUrgencia() == NivelUrgencia.URGENTE
                ? new UrgentPricingStrategy()
                : new NormalPricingStrategy();

        // 5. Crear la cotización mediante el factory method del dominio
        Cotizacion cotizacion = Cotizacion.crear(calzado, reparaciones, request.nivelUrgencia(), strategy);

        // 6. Persistir
        cotizacionRepository.guardar(cotizacion);

        // 7. Retornar
        return cotizacion;
    }
}
