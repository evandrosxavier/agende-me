package br.com.agendeme.notificacoes.dto;

public record ConsultaNotificacaoDTO(
        Long consultaId,
        String pacienteNome,
        String pacienteCpf,
        String pacienteEmail,
        String medicoNome,
        String medicoCrm,
        String especialidade,
        String dataHora,
        String status,
        String dataEvento
) {}

