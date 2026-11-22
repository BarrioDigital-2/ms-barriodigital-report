package cl.duoc.ms_barriodigital_report.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;

public enum RangoTiempo {
    LAST_24H("last24h", Duration.ofHours(24)),
    LAST_7D("last7d", Duration.ofDays(7)),
    LAST_30D("last30d", Duration.ofDays(30));

    private final String valor;
    private final Duration duracion;

    RangoTiempo(String valor, Duration duracion) {
        this.valor = valor;
        this.duracion = duracion;
    }

    public String getValor() {
        return valor;
    }

    public static RangoTiempo desde(String valor) {
        return Arrays.stream(values())
                .filter(rango -> rango.valor.equals(valor))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Rango no válido: " + valor));
    }

    public LocalDateTime fechaDesde() {
        return LocalDateTime.now().minus(duracion);
    }
}
