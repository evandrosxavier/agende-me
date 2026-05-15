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
        @Size(max = 50, message = "E-mail deve ter no máximo 50 caracteres")
        String email,

        @NotBlank(message = "DDD é obrigatório")
        @Pattern(regexp = "\\d{2}", message = "DDD deve ter 2 dígitos")
        String ddd,

        @NotBlank(message = "Telefone é obrigatório")
        @Pattern(regexp = "\\d{8,9}", message = "Telefone inválido")
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
        @Size(min = 4, max = 10, message = "Login deve ter entre 4 e 10 caracteres")
        @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Login deve conter apenas letras e números")
        String login,

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 8, max = 72, message = "Senha deve ter entre 8 e 72 caracteres")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@#$!%*?&]).+$",
                message = "Senha deve conter letra, número e caractere especial"
        )
        String senha,

        @NotBlank(message = "CRM é obrigatório")
        @Pattern(regexp = "\\d{4,10}", message = "CRM deve ter no máximo 10 caracteres")
        String crm,

        @NotBlank(message = "UF do CRM é obrigatória")
        @Pattern(regexp = "[A-Z]{2}", message = "UF deve ter 2 letras maiúsculas")
        String crmUf,

        @NotNull(message = "Especialidade é obrigatória")
        Especialidade especialidade
) {}
