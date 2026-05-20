package br.com.agendeme.notificacoes.listener;

import br.com.agendeme.notificacoes.dto.ConsultaNotificacaoDTO;
import br.com.agendeme.notificacoes.service.NotificacaoEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultaNotificacaoListener {

    private final NotificacaoEmailService emailService;

    @KafkaListener(topics = "consulta-agendada", groupId = "${spring.kafka.consumer.group-id}")
    public void consumirConsultaAgendada(ConsultaNotificacaoDTO mensagem) {
        log.info("[AGENDADA] Consulta ID: {} | Paciente: {} | Médico: {} | Data: {} | Status: {}",
                mensagem.consultaId(), mensagem.pacienteNome(), mensagem.medicoNome(),
                mensagem.dataHora(), mensagem.status());
        emailService.enviarConsultaAgendada(mensagem);
    }

    @KafkaListener(topics = "consulta-agendamento-atualizado", groupId = "${spring.kafka.consumer.group-id}")
    public void consumirConsultaAtualizada(ConsultaNotificacaoDTO mensagem) {
        log.info("[ATUALIZADA] Consulta ID: {} | Paciente: {} | Médico: {} | Data: {} | Status: {}",
                mensagem.consultaId(), mensagem.pacienteNome(), mensagem.medicoNome(),
                mensagem.dataHora(), mensagem.status());
        emailService.enviarConsultaAgendadaAtualizada(mensagem);
    }


    @KafkaListener(topics = "consulta-atendimento-registrado", groupId = "${spring.kafka.consumer.group-id}")
    public void consumirConsultaRealizada(ConsultaNotificacaoDTO mensagem) {
        log.info("[REALIZADA] Consulta ID: {} | Paciente: {} | Médico: {} | Data: {} | Status: {}",
                mensagem.consultaId(), mensagem.pacienteNome(), mensagem.medicoNome(),
                mensagem.dataHora(), mensagem.status());
        emailService.enviarConsultaRealizada(mensagem);
    }


    @KafkaListener(topics = "consulta-cancelada", groupId = "${spring.kafka.consumer.group-id}")
    public void consumirConsultaCancelada(ConsultaNotificacaoDTO mensagem) {
        log.info("[CANCELADA] Consulta ID: {} | Paciente: {} | Email: {} | Status: {}",
                mensagem.consultaId(), mensagem.pacienteNome(), mensagem.pacienteEmail(),
                mensagem.status());
        emailService.enviarConsultaCancelada(mensagem);
    }
}

