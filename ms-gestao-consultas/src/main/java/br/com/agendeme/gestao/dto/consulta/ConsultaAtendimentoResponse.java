package br.com.agendeme.gestao.dto.consulta;

import br.com.agendeme.gestao.model.domain.Especialidade;
import br.com.agendeme.gestao.model.domain.StatusConsulta;

import java.time.LocalDateTime;

public record ConsultaAtendimentoResponse(
        Long id,
        Long pacienteId,
        String pacienteNome,
        Long medicoId,
        String medicoNome,
        String medicoCrm,
        String medicoCrmUf,
        Especialidade especialidade,
        LocalDateTime dataHora,
        StatusConsulta status,
        LocalDateTime dataCriacao,
        LocalDateTime dataModificacao,
        String diagnostico,
        String tratamentoProposto,
        String demaisObservacoes
) {}
