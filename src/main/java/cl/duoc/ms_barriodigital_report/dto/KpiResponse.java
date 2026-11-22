package cl.duoc.ms_barriodigital_report.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KpiResponse {
    private String rango;
    private Long totalTramites;
    private List<TramitePorHoraResponse> tramitesPorHora;
    private Double tiempoPromedioResolucionMinutos;
}
