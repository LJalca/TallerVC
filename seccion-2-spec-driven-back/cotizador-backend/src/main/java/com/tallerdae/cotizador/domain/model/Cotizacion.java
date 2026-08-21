package com.tallerdae.cotizador.domain.model;

import com.tallerdae.cotizador.domain.exception.ValidacionException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Entidad que representa una cotización de reparación de calzado.
 *
 * <p>Sólo puede ser instanciada a través del método de fábrica
 * {@link #crear(Calzado, List, NivelUrgencia, UrgencyPricingStrategy)},
 * que garantiza que toda instancia nace en estado válido y con todos los
 * campos calculados (subtotal, recargo, total, tiempoEstimadoDias, id y
 * fechaCreacion).</p>
 */
public final class Cotizacion {

    private final UUID id;
    private final LocalDateTime fechaCreacion;
    private final Calzado calzado;
    private final List<Reparacion> reparaciones;
    private final NivelUrgencia nivelUrgencia;
    private final Dinero subtotal;
    private final Dinero recargo;
    private final Dinero total;
    private final int tiempoEstimadoDias;

    /** Constructor privado — usar {@link #crear}. */
    private Cotizacion(
            UUID id,
            LocalDateTime fechaCreacion,
            Calzado calzado,
            List<Reparacion> reparaciones,
            NivelUrgencia nivelUrgencia,
            Dinero subtotal,
            Dinero recargo,
            Dinero total,
            int tiempoEstimadoDias) {
        this.id = id;
        this.fechaCreacion = fechaCreacion;
        this.calzado = calzado;
        this.reparaciones = reparaciones;
        this.nivelUrgencia = nivelUrgencia;
        this.subtotal = subtotal;
        this.recargo = recargo;
        this.total = total;
        this.tiempoEstimadoDias = tiempoEstimadoDias;
    }

    /**
     * Crea una cotización válida a partir de los datos del pedido.
     *
     * @param calzado       tipo de calzado seleccionado
     * @param reparaciones  lista de reparaciones seleccionadas (no vacía)
     * @param nivelUrgencia urgencia del servicio
     * @param strategy      estrategia de cálculo de recargo
     * @return nueva instancia de {@code Cotizacion}
     * @throws ValidacionException si la lista de reparaciones es nula o vacía (RN-01)
     */
    public static Cotizacion crear(
            Calzado calzado,
            List<Reparacion> reparaciones,
            NivelUrgencia nivelUrgencia,
            UrgencyPricingStrategy strategy) {

        // RN-01: la lista de reparaciones no puede estar vacía
        if (reparaciones == null || reparaciones.isEmpty()) {
            throw new ValidacionException("Se requiere al menos una reparación seleccionada");
        }

        // RN-02: subtotal = Σ(precioBase_i × factorComplejidad)
        Dinero subtotal = calcularSubtotal(calzado, reparaciones);

        // Recargo vía Strategy
        Dinero recargo = strategy.calcularRecargo(subtotal);

        // Total = subtotal + recargo
        Dinero total = subtotal.sumar(recargo);

        // RN-03 / RN-04: tiempo estimado
        int tiempoEstimadoDias = calcularTiempoEstimado(reparaciones, nivelUrgencia);

        // RN-05: id y fechaCreacion asignados en el momento de creación
        UUID id = UUID.randomUUID();
        LocalDateTime fechaCreacion = LocalDateTime.now();

        return new Cotizacion(
                id,
                fechaCreacion,
                calzado,
                List.copyOf(reparaciones),
                nivelUrgencia,
                subtotal,
                recargo,
                total,
                tiempoEstimadoDias);
    }

    // -------------------------------------------------------------------------
    // Métodos de cálculo privados
    // -------------------------------------------------------------------------

    /**
     * RN-02: Σ(precioBase_i × factorComplejidad).
     */
    private static Dinero calcularSubtotal(Calzado calzado, List<Reparacion> reparaciones) {
        Dinero acumulador = new Dinero(BigDecimal.ZERO, "USD");
        for (Reparacion reparacion : reparaciones) {
            BigDecimal productoMonto = reparacion.getPrecioBase()
                    .multiply(calzado.getFactorComplejidad());
            acumulador = acumulador.sumar(new Dinero(productoMonto, "USD"));
        }
        return acumulador;
    }

    /**
     * RN-03 (NORMAL): {@code max(tiemposEstimadosDias)}.
     * RN-04 (URGENTE): {@code max(1, ⌈max/2⌉)}.
     */
    private static int calcularTiempoEstimado(List<Reparacion> reparaciones, NivelUrgencia nivelUrgencia) {
        int maxTiempo = reparaciones.stream()
                .mapToInt(Reparacion::getTiempoEstimadoDias)
                .max()
                .getAsInt();

        if (nivelUrgencia == NivelUrgencia.URGENTE) {
            return Math.max(1, (int) Math.ceil(maxTiempo / 2.0));
        }
        return maxTiempo;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public UUID getId() {
        return id;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public Calzado getCalzado() {
        return calzado;
    }

    public List<Reparacion> getReparaciones() {
        return reparaciones;
    }

    public NivelUrgencia getNivelUrgencia() {
        return nivelUrgencia;
    }

    public Dinero getSubtotal() {
        return subtotal;
    }

    public Dinero getRecargo() {
        return recargo;
    }

    public Dinero getTotal() {
        return total;
    }

    public int getTiempoEstimadoDias() {
        return tiempoEstimadoDias;
    }
}
