package br.com.agendeme.gestao.dto.notificacao;

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
        String dataEvento,
        String diagnostico,
        String tratamentoProposto,
        String demaisObservacoes


) {}