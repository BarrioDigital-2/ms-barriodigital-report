package cl.duoc.ms_barriodigital_report.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cl.duoc.ms_barriodigital_report.model.EventoSolicitud;

public interface EventoSolicitudRepository extends JpaRepository<EventoSolicitud, Long> {

    long countByTipoEventoAndFechaEventoAfter(String tipoEvento, LocalDateTime fechaDesde);

    interface ConteoPorHora {
        String getHora();
        Long getCantidad();
    }

    interface ConteoPorTipo {
        String getTipoTramite();
        Long getCantidad();
    }

    interface PromedioResolucion {
        Double getPromedioMinutos();
    }

    @Query(value = """
            SELECT DATE_FORMAT(fecha_evento, '%Y-%m-%dT%H:00:00') AS hora,
                   COUNT(*) AS cantidad
            FROM evento_solicitud
            WHERE tipo_evento = 'SOLICITUD_CREADA'
              AND fecha_evento >= :fechaDesde
            GROUP BY DATE_FORMAT(fecha_evento, '%Y-%m-%dT%H:00:00')
            ORDER BY hora
            """, nativeQuery = true)
    List<ConteoPorHora> contarPorHora(@Param("fechaDesde") LocalDateTime fechaDesde);

    @Query(value = """
            SELECT tipo_tramite AS tipoTramite, COUNT(*) AS cantidad
            FROM evento_solicitud
            WHERE tipo_evento = 'SOLICITUD_CREADA'
              AND fecha_evento >= :fechaDesde
            GROUP BY tipo_tramite
            ORDER BY cantidad DESC
            """, nativeQuery = true)
    List<ConteoPorTipo> contarPorTipo(@Param("fechaDesde") LocalDateTime fechaDesde);

    @Query(value = """
            SELECT AVG(TIMESTAMPDIFF(MINUTE, c.fecha_evento, r.fecha_evento)) AS promedioMinutos
            FROM evento_solicitud c
            JOIN evento_solicitud r
              ON c.solicitud_id = r.solicitud_id
             AND r.estado = 'RESUELTO'
             AND r.fecha_evento >= c.fecha_evento
            WHERE c.tipo_evento = 'SOLICITUD_CREADA'
              AND c.fecha_evento >= :fechaDesde
            """, nativeQuery = true)
    PromedioResolucion promedioResolucion(@Param("fechaDesde") LocalDateTime fechaDesde);
}
