package br.com.agendeme.gestao.dto.medico;

import br.com.agendeme.gestao.dto.endereco.EnderecoRequest;
import br.com.agendeme.gestao.model.enums.Especialidade;
import br.com.agendeme.gestao.model.enums.Sexo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

@Schema(description = "Dados para cadastro de um novo médico")
public record MedicoRequest(

        @Schema(description = "Nome completo do médico", example = "Dr. Carlos Eduardo Ferreira")
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
        String nome,

        @Schema(description = "E-mail do médico", example = "carlos.ferreira@hospital.com")
        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        @Size(max = 50, message = "E-mail deve ter no máximo 50 caracteres")
        String email,

        @Schema(description = "DDD do telefone (2 dígitos)", example = "11")
        @NotBlank(message = "DDD é obrigatório")
        @Pattern(regexp = "\\d{2}", message = "DDD deve ter 2 dígitos")
        String ddd,

        @Schema(description = "Número do telefone (8 ou 9 dígitos)", example = "991234567")
        @NotBlank(message = "Telefone é obrigatório")
        @Pattern(regexp = "\\d{8,9}", message = "Telefone inválido")
        String telefone,

        @Schema(description = "Sexo do médico", example = "MASCULINO")
        @NotNull(message = "Sexo é obrigatório")
        Sexo sexo,

        @Schema(description = "Data de nascimento (formato: yyyy-MM-dd)", example = "1980-03-10")
        @NotNull(message = "Data de nascimento é obrigatória")
        @Past(message = "Data de nascimento deve ser uma data no passado")
        LocalDate dataNascimento,

        @Schema(description = "Endereço do médico")
        @NotNull(message = "Endereço é obrigatório")
        @Valid
        EnderecoRequest endereco,

        @Schema(description = "Login de acesso ao sistema (4 a 10 caracteres alfanuméricos)", example = "carlosmed")
        @NotBlank(message = "Login é obrigatório")
        @Size(min = 4, max = 10, message = "Login deve ter entre 4 e 10 caracteres")
        @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Login deve conter apenas letras e números")
        String login,

        @Schema(description = "Senha de acesso (mínimo 8 caracteres, com letra, número e caractere especial)", example = "Senha@123")
        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 8, max = 72, message = "Senha deve ter entre 8 e 72 caracteres")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@#$!%*?&]).+$",
                message = "Senha deve conter letra, número e caractere especial"
        )
        String senha,

        @Schema(description = "Número do CRM (4 a 10 dígitos)", example = "123456")
        @NotBlank(message = "CRM é obrigatório")
        @Pattern(regexp = "\\d{4,10}", message = "CRM deve ter no máximo 10 caracteres")
        String crm,

        @Schema(description = "UF do CRM (2 letras maiúsculas)", example = "SP")
        @NotBlank(message = "UF do CRM é obrigatória")
        @Pattern(regexp = "[A-Z]{2}", message = "UF deve ter 2 letras maiúsculas")
        String crmUf,

        @Schema(description = "Especialidade médica", example = "CARDIOLOGIA")
        @NotNull(message = "Especialidade é obrigatória")
        Especialidade especialidade
) {}
