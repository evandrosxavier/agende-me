package br.com.agendeme.notificacoes.dto;

import br.com.agendeme.notificacoes.model.NotificacaoLog;
import br.com.agendeme.notificacoes.model.StatusEnvio;

import java.time.LocalDateTime;

public record NotificacaoLogResponseDTO(
        Long id,
        Long consultaId,
        String tipoEvento,
        String statusConsulta,
        String destinatario,
        String pacienteNome,
        String assunto,
        StatusEnvio status,
        String mensagemErro,
        LocalDateTime dataEnvio
) {
    public static NotificacaoLogResponseDTO fromEntity(NotificacaoLog entity) {
        return new NotificacaoLogResponseDTO(
                entity.getId(),
                entity.getConsultaId(),
                entity.getTipoEvento(),
                entity.getStatusConsulta(),
                entity.getDestinatario(),
                entity.getPacienteNome(),
                entity.getAssunto(),
                entity.getStatus(),
                entity.getMensagemErro(),
                entity.getDataEnvio()
        );
    }
}

