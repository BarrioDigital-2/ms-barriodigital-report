package cl.duoc.ms_barriodigital_report.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.ms_barriodigital_report.dto.KpiResponse;
import cl.duoc.ms_barriodigital_report.dto.TopProcedureResponse;
import cl.duoc.ms_barriodigital_report.model.RangoTiempo;
import cl.duoc.ms_barriodigital_report.service.ReportService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/kpis")
    public KpiResponse kpis(@RequestParam(name = "range", defaultValue = "last24h") String range) {
        return reportService.obtenerKpis(RangoTiempo.desde(range));
    }

    @GetMapping("/top-procedures")
    public List<TopProcedureResponse> topProcedures(
            @RequestParam(name = "range", defaultValue = "last7d") String range) {
        return reportService.obtenerTopTramites(RangoTiempo.desde(range));
    }
}
