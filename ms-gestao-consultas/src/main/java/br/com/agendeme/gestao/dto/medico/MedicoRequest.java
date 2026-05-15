package br.com.agendeme.gestao.dto.medico;

import br.com.agendeme.gestao.dto.endereco.EnderecoRequest;
import br.com.agendeme.gestao.model.domain.Especialidade;
import br.com.agendeme.gestao.model.domain.Sexo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record MedicoRequest(

        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
        String nome,

        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        @Size(max = 100, message = "E-mail deve ter no máximo 100 caracteres")
        String email,

        @Pattern(regexp = "\\d{2}", message = "DDD deve ter 2 dígitos")
        String ddd,

        @Size(min= 8, max = 20, message = "Telefone deve ter no mínimo 8 e máximo 20 caracteres")
        String telefone,

        @NotNull(message = "Sexo é obrigatório")
        Sexo sexo,

        @NotNull(message = "Data de nascimento é obrigatória")
        @Past(message = "Data de nascimento deve ser uma data no passado")
        LocalDate dataNascimento,

        @NotNull(message = "Endereço é obrigatório")
        @Valid
        EnderecoRequest endereco,

        @NotBlank(message = "Login é obrigatório")
        String login,

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 8, max = 255, message = "Senha deve ter entre 8 e 255 caracteres")
        String senha,

        @NotBlank(message = "CRM é obrigatório")
        @Size(max = 15, message = "CRM deve ter no máximo 15 caracteres")
        String crm,

        @NotBlank(message = "UF do CRM é obrigatória")
        @Size(min = 2, max = 2, message = "UF do CRM deve ter exatamente 2 caracteres")
        String crmUf,

        @NotNull(message = "Especialidade é obrigatória")
        Especialidade especialidade
) {}
