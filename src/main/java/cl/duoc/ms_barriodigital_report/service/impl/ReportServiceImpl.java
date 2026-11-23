package cl.duoc.ms_barriodigital_report.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import cl.duoc.ms_barriodigital_report.dto.KpiResponse;
import cl.duoc.ms_barriodigital_report.dto.TopProcedureResponse;
import cl.duoc.ms_barriodigital_report.dto.TramitePorHoraResponse;
import cl.duoc.ms_barriodigital_report.model.RangoTiempo;
import cl.duoc.ms_barriodigital_report.repository.EventoSolicitudRepository;
import cl.duoc.ms_barriodigital_report.repository.EventoSolicitudRepository.PromedioResolucion;
import cl.duoc.ms_barriodigital_report.service.ReportService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final EventoSolicitudRepository repository;

    @Override
    public KpiResponse obtenerKpis(RangoTiempo rango) {
        LocalDateTime fechaDesde = rango.fechaDesde();
        long total = repository.countByTipoEventoAndFechaEventoAfter("SOLICITUD_CREADA", fechaDesde);

        List<TramitePorHoraResponse> porHora = repository.contarPorHora(fechaDesde).stream()
                .map(fila -> new TramitePorHoraResponse(fila.getHora(), fila.getCantidad()))
                .toList();

        PromedioResolucion promedio = repository.promedioResolucion(fechaDesde);
        Double minutos = promedio == null ? null : promedio.getPromedioMinutos();

        return new KpiResponse(rango.getValor(), total, porHora, minutos);
    }

    @Override
    public List<TopProcedureResponse> obtenerTopTramites(RangoTiempo rango) {
        return repository.contarPorTipo(rango.fechaDesde()).stream()
                .map(fila -> new TopProcedureResponse(fila.getTipoTramite(), fila.getCantidad()))
                .toList();
    }
}
