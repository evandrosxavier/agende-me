package br.com.agendeme.gestao.dto.consulta;

import br.com.agendeme.gestao.model.enums.Especialidade;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Schema(description = "Dados para agendamento de uma nova consulta médica")
public record ConsultaRequest(

        @Schema(description = "ID do paciente", example = "1")
        @NotNull(message = "ID do paciente é obrigatório")
        Long pacienteId,

        @Schema(description = "ID do médico", example = "2")
        @NotNull(message = "ID do médico é obrigatório")
        Long medicoId,

        @Schema(description = "Especialidade médica da consulta", example = "CARDIOLOGIA")
        @NotNull(message = "Especialidade é obrigatória")
        Especialidade especialidade,

        @Schema(description = "Data e hora da consulta (formato: yyyy-MM-dd'T'HH:mm:ss)", example = "2026-06-15T14:30:00")
        @NotNull(message = "Data e hora da consulta são obrigatórias")
        @FutureOrPresent(message = "A data e hora da consulta deve ser uma data futura")
        LocalDateTime dataHora
) {}
