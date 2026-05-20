package br.com.agendeme.gestao.dto.consulta;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados do atendimento médico realizado na consulta")
public record ConsultaAtendimentoUpdate(

        @Schema(description = "Diagnóstico do paciente", example = "Hipertensão arterial leve")
        @Size(max = 255)
        String diagnostico,

        @Schema(description = "Tratamento proposto ao paciente", example = "Medicação anti-hipertensiva por 30 dias")
        @Size(max = 255)
        String tratamentoProposto,

        @Schema(description = "Demais observações clínicas", example = "Retorno em 30 dias para reavaliação")
        @Size(max = 255)
        String demaisObservacoes
) {}
