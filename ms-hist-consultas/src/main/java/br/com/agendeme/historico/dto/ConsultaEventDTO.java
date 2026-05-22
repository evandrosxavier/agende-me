package br.com.agendeme.historico.dto;

public record ConsultaEventDTO(
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

