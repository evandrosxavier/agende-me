package br.com.agendeme.gestao.dto.consulta;
import jakarta.validation.constraints.Size;


public record ConsultaAtendimentoUpdate(
        @Size(max = 255)
        String diagnostico,

        @Size(max = 255)
        String tratamentoProposto,

        @Size(max = 255)
        String demaisObservacoes
) {}
