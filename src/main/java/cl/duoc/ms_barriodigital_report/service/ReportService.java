package cl.duoc.ms_barriodigital_report.service;

import java.util.List;

import cl.duoc.ms_barriodigital_report.dto.KpiResponse;
import cl.duoc.ms_barriodigital_report.dto.TopProcedureResponse;
import cl.duoc.ms_barriodigital_report.model.RangoTiempo;

public interface ReportService {
    KpiResponse obtenerKpis(RangoTiempo rango);
    List<TopProcedureResponse> obtenerTopTramites(RangoTiempo rango);
}
