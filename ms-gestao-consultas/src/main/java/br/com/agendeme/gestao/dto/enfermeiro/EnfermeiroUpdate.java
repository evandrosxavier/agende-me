package br.com.agendeme.gestao.dto.enfermeiro;

import br.com.agendeme.gestao.dto.endereco.EnderecoUpdate;
import br.com.agendeme.gestao.model.enums.Sexo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EnfermeiroUpdate(

        @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
        String nome,

        @Size(min = 2, max = 2, message = "DDD deve ter 2 dígitos")
        @Pattern(regexp = "\\d{2}", message = "DDD deve ter 2 dígitos")
        String ddd,

        @Size(min = 8, max = 9, message = "Telefone deve ter entre 8 e 9 dígitos")
        @Pattern(regexp = "\\d{8,9}", message = "Telefone inválido")
        String telefone,

        Sexo sexo,

        @Valid
        EnderecoUpdate endereco,

        @Pattern(regexp = "\\d{4,10}", message = "CRE deve ter entre 4 e 10 dígitos")
        String cre,

        @Pattern(regexp = "[A-Z]{2}", message = "UF deve ter 2 letras maiúsculas")
        String creUf
) {}

