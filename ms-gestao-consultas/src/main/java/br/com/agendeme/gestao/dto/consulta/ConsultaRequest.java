package br.com.agendeme.gestao.dto.consulta;

import br.com.agendeme.gestao.model.domain.Especialidade;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ConsultaRequest(

        @NotNull(message = "ID do paciente é obrigatório")
        Long pacienteId,

        @NotNull(message = "ID do médico é obrigatório")
        Long medicoId,

        @NotNull(message = "Especialidade é obrigatória")
        Especialidade especialidade,

        @NotNull(message = "Data e hora da consulta são obrigatórias")
        @FutureOrPresent(message = "A data e hora da consulta deve ser uma data futura")
        LocalDateTime dataHora
) {}

