package com.tallerdae.cotizador.infrastructure.rest;

import com.tallerdae.cotizador.application.port.in.ConsultarCatalogoUseCase;
import com.tallerdae.cotizador.application.port.in.CotizacionRequest;
import com.tallerdae.cotizador.application.port.in.GenerarCotizacionUseCase;
import com.tallerdae.cotizador.domain.model.Cotizacion;
import com.tallerdae.cotizador.domain.model.NivelUrgencia;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Adaptador REST de entrada. Expone los tres endpoints del cotizador de calzado.
 * No contiene lógica de negocio: delega en los casos de uso y usa
 * {@link CotizacionMapper} para traducir entre dominio y DTOs HTTP.
 */
@RestController
public class CotizacionController {

    private final GenerarCotizacionUseCase generarCotizacionUseCase;
    private final ConsultarCatalogoUseCase consultarCatalogoUseCase;
    private final CotizacionMapper cotizacionMapper;

    public CotizacionController(
            GenerarCotizacionUseCase generarCotizacionUseCase,
            ConsultarCatalogoUseCase consultarCatalogoUseCase,
            CotizacionMapper cotizacionMapper) {
        this.generarCotizacionUseCase = generarCotizacionUseCase;
        this.consultarCatalogoUseCase = consultarCatalogoUseCase;
        this.cotizacionMapper = cotizacionMapper;
    }

    /**
     * Lista todos los tipos de calzado disponibles en el catálogo.
     *
     * @return HTTP 200 con la lista de {@link CalzadoResponse}
     */
    @GetMapping("/api/tipos-calzado")
    public ResponseEntity<List<CalzadoResponse>> listarCalzados() {
        List<CalzadoResponse> result = consultarCatalogoUseCase.listarCalzados()
                .stream()
                .map(cotizacionMapper::toCalzadoResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /**
     * Lista todos los tipos de reparación disponibles en el catálogo.
     *
     * @return HTTP 200 con la lista de {@link ReparacionResponse}
     */
    @GetMapping("/api/tipos-reparacion")
    public ResponseEntity<List<ReparacionResponse>> listarReparaciones() {
        List<ReparacionResponse> result = consultarCatalogoUseCase.listarReparaciones()
                .stream()
                .map(cotizacionMapper::toReparacionResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /**
     * Genera una cotización de reparación de calzado.
     *
     * @param httpRequest DTO HTTP con tipoCalzadoId, tipoReparacionIds y urgente
     * @return HTTP 201 con la {@link CotizacionResponse} generada
     */
    @PostMapping("/api/cotizaciones")
    public ResponseEntity<CotizacionResponse> generarCotizacion(
            @RequestBody CotizacionHttpRequest httpRequest) {
        // Mapear HTTP DTO → use case DTO (traduce urgente:boolean a NivelUrgencia)
        CotizacionRequest useCaseRequest = new CotizacionRequest(
                httpRequest.tipoCalzadoId(),
                httpRequest.tipoReparacionIds(),
                httpRequest.urgente() ? NivelUrgencia.URGENTE : NivelUrgencia.NORMAL
        );
        Cotizacion cotizacion = generarCotizacionUseCase.generarCotizacion(useCaseRequest);
        CotizacionResponse response = cotizacionMapper.toResponse(cotizacion);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
