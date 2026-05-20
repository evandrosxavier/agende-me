package br.com.agendeme.gestao.dto.consulta;

import jakarta.validation.constraints.FutureOrPresent;

import java.time.LocalDateTime;

public record ConsultaAgendamentoUpdate(
        @FutureOrPresent(message = "A data da consulta deve ser hoje ou futura")
        LocalDateTime dataHora,
        Long medicoId
) {}