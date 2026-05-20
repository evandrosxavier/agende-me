package br.com.agendeme.gestao.dto.consulta;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;

import java.time.LocalDateTime;

@Schema(description = "Dados para atualização de uma consulta agendada (todos os campos são opcionais)")
public record ConsultaAgendamentoUpdate(

        @Schema(description = "Nova data e hora da consulta", example = "2026-07-20T09:00:00")
        @FutureOrPresent(message = "A data da consulta deve ser hoje ou futura")
        LocalDateTime dataHora,

        @Schema(description = "ID do novo médico (a especialidade será atualizada automaticamente)", example = "3")
        Long medicoId
) {}