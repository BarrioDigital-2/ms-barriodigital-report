package cl.duoc.ms_barriodigital_report.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopProcedureResponse {
    private String tipoTramite;
    private Long cantidad;
}
