package br.com.agendeme.gestao.dto.consulta;
import br.com.agendeme.gestao.model.domain.StatusConsulta;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public record ConsultaAtendimentoUpdate(
        @Size(max = 255)
        String diagnostico,

        @Size(max = 255)
        String tratamentoProposto,

        @Size(max = 255)
        String demaisObservacoes
) {}
