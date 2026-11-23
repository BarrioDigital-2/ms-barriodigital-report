package cl.duoc.ms_barriodigital_report.service.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import cl.duoc.ms_barriodigital_report.dto.SolicitudEvent;
import cl.duoc.ms_barriodigital_report.model.EventoSolicitud;
import cl.duoc.ms_barriodigital_report.repository.EventoSolicitudRepository;
import cl.duoc.ms_barriodigital_report.service.EventoRegistroService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventoRegistroServiceImpl implements EventoRegistroService {

    private final EventoSolicitudRepository repository;

    @Override
    public void registrar(SolicitudEvent evento) {
        try {
            LocalDateTime fechaEvento = LocalDateTime.parse(evento.getTimestamp());

            EventoSolicitud entidad = new EventoSolicitud();
            entidad.setSolicitudId(evento.getId());
            entidad.setTipoEvento(evento.getTipo());
            entidad.setTipoTramite(evento.getTitulo());
            entidad.setEstado(evento.getEstado());
            entidad.setFechaEvento(fechaEvento);
            repository.save(entidad);
        } catch (RuntimeException e) {
            log.warn("Timestamp inválido; se descarta el evento de solicitud id={}: {}", evento.getId(), e.getMessage());
        }
    }
}
