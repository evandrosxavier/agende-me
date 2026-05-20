package br.com.agendeme.gestao.dto.consulta;

import br.com.agendeme.gestao.model.enums.Especialidade;
import br.com.agendeme.gestao.model.enums.StatusConsulta;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Dados de uma consulta agendada retornados na resposta")
public record ConsultaAgendamentoResponse(

        @Schema(description = "ID da consulta", example = "10")
        Long id,

        @Schema(description = "ID do paciente", example = "1")
        Long pacienteId,

        @Schema(description = "Nome do paciente", example = "Maria Clara Santos")
        String pacienteNome,

        @Schema(description = "ID do médico", example = "2")
        Long medicoId,

        @Schema(description = "Nome do médico", example = "Dr. Carlos Ferreira")
        String medicoNome,

        @Schema(description = "CRM do médico", example = "123456")
        String medicoCrm,

        @Schema(description = "UF do CRM do médico", example = "SP")
        String medicoCrmUf,

        @Schema(description = "Especialidade médica", example = "CARDIOLOGIA")
        Especialidade especialidade,

        @Schema(description = "Data e hora da consulta", example = "2026-06-15T14:30:00")
        LocalDateTime dataHora,

        @Schema(description = "Status atual da consulta", example = "AGENDADA")
        StatusConsulta status,

        @Schema(description = "Data de criação do registro", example = "2026-05-20T10:00:00")
        LocalDateTime dataCriacao,

        @Schema(description = "Data da última modificação", example = "2026-05-20T10:00:00")
        LocalDateTime dataModificacao
) {}
