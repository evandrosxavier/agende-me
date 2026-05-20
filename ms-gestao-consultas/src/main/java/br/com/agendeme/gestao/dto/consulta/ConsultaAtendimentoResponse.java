package br.com.agendeme.gestao.dto.consulta;

import br.com.agendeme.gestao.model.enums.Especialidade;
import br.com.agendeme.gestao.model.enums.StatusConsulta;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Dados completos de uma consulta realizada, incluindo informações do atendimento")
public record ConsultaAtendimentoResponse(

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

        @Schema(description = "Status da consulta", example = "REALIZADA")
        StatusConsulta status,

        @Schema(description = "Data de criação do registro", example = "2026-05-20T10:00:00")
        LocalDateTime dataCriacao,

        @Schema(description = "Data da última modificação", example = "2026-06-15T15:00:00")
        LocalDateTime dataModificacao,

        @Schema(description = "Diagnóstico registrado pelo médico", example = "Hipertensão arterial leve")
        String diagnostico,

        @Schema(description = "Tratamento proposto pelo médico", example = "Medicação anti-hipertensiva por 30 dias")
        String tratamentoProposto,

        @Schema(description = "Demais observações clínicas", example = "Retorno em 30 dias para reavaliação")
        String demaisObservacoes
) {}
