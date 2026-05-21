package br.com.agendeme.historico.listener;

import br.com.agendeme.historico.dto.ConsultaEventDTO;
import br.com.agendeme.historico.service.HistoricoConsultaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HistoricoConsultaListener {

    private final HistoricoConsultaService historicoConsultaService;

    @KafkaListener(topics = "consulta-agendada", groupId = "${spring.kafka.consumer.group-id}")
    public void onConsultaAgendada(ConsultaEventDTO dto) {
        log.info("Evento recebido [consulta-agendada] | consultaId={}", dto.consultaId());
        historicoConsultaService.salvarHistorico(dto);
    }

    @KafkaListener(topics = "consulta-agendamento-atualizado", groupId = "${spring.kafka.consumer.group-id}")
    public void onConsultaAgendadaAtualizada(ConsultaEventDTO dto) {
        log.info("Evento recebido [consulta-atualizada] | consultaId={}", dto.consultaId());
        historicoConsultaService.salvarHistorico(dto);
    }

    @KafkaListener(topics = "consulta-atendimento-registrado", groupId = "${spring.kafka.consumer.group-id}")
    public void onConsultaRealizada(ConsultaEventDTO dto) {
        log.info("Evento recebido [consulta-realizada] | consultaId={}", dto.consultaId());
        historicoConsultaService.salvarHistorico(dto);
    }

    @KafkaListener(topics = "consulta-atendimento-atualizado", groupId = "${spring.kafka.consumer.group-id}")
    public void onConsultaMedicaAtualizada(ConsultaEventDTO dto) {
        log.info("Evento recebido [consulta-medica-atualizada] | consultaId={}", dto.consultaId());
        historicoConsultaService.salvarHistorico(dto);
    }

    @KafkaListener(topics = "consulta-cancelada", groupId = "${spring.kafka.consumer.group-id}")
    public void onConsultaCancelada(ConsultaEventDTO dto) {
        log.info("Evento recebido [consulta-cancelada] | consultaId={}", dto.consultaId());
        historicoConsultaService.salvarHistorico(dto);
    }
}
