package com.tallerdae.cotizador.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value Object que encapsula un valor monetario (monto + moneda).
 * Inmutable: todas las operaciones retornan nuevas instancias.
 */
public final class Dinero {

    public static final Dinero ZERO = new Dinero(BigDecimal.ZERO, "USD");

    private final BigDecimal monto;
    private final String moneda;

    public Dinero(BigDecimal monto, String moneda) {
        this.monto = monto;
        this.moneda = moneda;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public String getMoneda() {
        return moneda;
    }

    /**
     * Suma este Dinero con otro. Ambos deben tener la misma moneda.
     *
     * @param otro el otro valor monetario a sumar
     * @return nueva instancia de Dinero con el monto sumado
     * @throws IllegalArgumentException si las monedas difieren
     */
    public Dinero sumar(Dinero otro) {
        if (!this.moneda.equals(otro.moneda)) {
            throw new IllegalArgumentException(
                "No se pueden sumar monedas distintas: " + this.moneda + " y " + otro.moneda);
        }
        return new Dinero(this.monto.add(otro.monto), this.moneda);
    }

    /**
     * Aplica un porcentaje sobre el monto y retorna el resultado redondeado a 2 decimales (HALF_UP).
     *
     * @param porcentaje fracción a aplicar (p. ej. 0.30 para el 30 %)
     * @return nueva instancia de Dinero con el monto resultante
     */
    public Dinero aplicarPorcentaje(BigDecimal porcentaje) {
        BigDecimal resultado = this.monto.multiply(porcentaje).setScale(2, RoundingMode.HALF_UP);
        return new Dinero(resultado, this.moneda);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Dinero)) return false;
        Dinero dinero = (Dinero) o;
        return Objects.equals(monto, dinero.monto) &&
               Objects.equals(moneda, dinero.moneda);
    }

    @Override
    public int hashCode() {
        return Objects.hash(monto, moneda);
    }

    @Override
    public String toString() {
        return monto + " " + moneda;
    }
}
