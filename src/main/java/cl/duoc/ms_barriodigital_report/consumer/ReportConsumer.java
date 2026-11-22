package cl.duoc.ms_barriodigital_report.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import cl.duoc.ms_barriodigital_report.dto.SolicitudEvent;
import cl.duoc.ms_barriodigital_report.service.EventoRegistroService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReportConsumer {

    private final EventoRegistroService eventoRegistroService;

    @KafkaListener(topics = "${report.kafka.topic}")
    public void recibir(@Payload SolicitudEvent evento) {
        log.info("Evento recibido: tipo={}, solicitudId={}, estado={}",
                evento.getTipo(), evento.getId(), evento.getEstado());
        eventoRegistroService.registrar(evento);
    }
}
