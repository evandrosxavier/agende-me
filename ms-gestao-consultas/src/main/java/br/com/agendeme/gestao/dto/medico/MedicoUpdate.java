package br.com.agendeme.gestao.dto.medico;

import br.com.agendeme.gestao.dto.endereco.EnderecoRequest;
import br.com.agendeme.gestao.model.domain.Especialidade;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MedicoUpdate(

        @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
        String nome,

        @Pattern(regexp = "\\d{2}", message = "DDD deve ter 2 dígitos")
        String ddd,

        @Size(min= 8, max = 20, message = "Telefone deve ter no máximo 20 caracteres")
        String telefone,

        @Valid
        EnderecoRequest endereco,

        @Size(min=4, max = 15, message = "CRM deve ter no máximo 15 caracteres")
        String crm,

        @Size(min = 2, max = 2, message = "UF do CRM deve ter exatamente 2 caracteres")
        String crmUf,

        Especialidade especialidade
) {}
