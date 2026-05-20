package br.com.agendeme.gestao.dto.paciente;

import br.com.agendeme.gestao.dto.endereco.EnderecoRequest;
import br.com.agendeme.gestao.model.enums.Sexo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

@Schema(description = "Dados para cadastro de um novo paciente")
public record PacienteRequest(

        @Schema(description = "Nome completo do paciente", example = "Maria Clara Santos")
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
        String nome,

        @Schema(description = "E-mail do paciente", example = "maria.santos@email.com")
        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        @Size(max = 50, message = "E-mail deve ter no máximo 50 caracteres")
        String email,

        @Schema(description = "DDD do telefone (2 dígitos)", example = "11")
        @NotBlank(message = "DDD é obrigatório")
        @Pattern(regexp = "\\d{2}", message = "DDD deve ter 2 dígitos")
        String ddd,

        @Schema(description = "Número do telefone (8 ou 9 dígitos)", example = "987654321")
        @NotBlank(message = "Telefone é obrigatório")
        @Pattern(regexp = "\\d{8,9}", message = "Telefone inválido")
        String telefone,

        @Schema(description = "Sexo do paciente", example = "FEMININO")
        @NotNull(message = "Sexo é obrigatório")
        Sexo sexo,

        @Schema(description = "Data de nascimento (formato: yyyy-MM-dd)", example = "1990-05-20")
        @NotNull(message = "Data de nascimento é obrigatória")
        @Past(message = "Data de nascimento deve ser uma data no passado")
        LocalDate dataNascimento,

        @Schema(description = "Endereço do paciente")
        @NotNull(message = "Endereço é obrigatório")
        @Valid
        EnderecoRequest endereco,

        @Schema(description = "Login de acesso ao sistema (4 a 10 caracteres alfanuméricos)", example = "mariaclara")
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

        @Schema(description = "CPF com 11 dígitos numéricos sem pontuação", example = "12345678901")
        @NotBlank(message = "CPF é obrigatório")
        @Pattern(regexp = "\\d{11}", message = "CPF deve conter 11 dígitos numéricos sem pontuação")
        String cpf
) {}
