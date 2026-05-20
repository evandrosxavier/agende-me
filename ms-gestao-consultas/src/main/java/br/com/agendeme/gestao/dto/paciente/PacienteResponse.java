package br.com.agendeme.gestao.dto.paciente;

import br.com.agendeme.gestao.dto.endereco.EnderecoResponse;
import br.com.agendeme.gestao.model.enums.Role;
import br.com.agendeme.gestao.model.enums.Sexo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Dados do paciente retornados na resposta")
public record PacienteResponse(
        @Schema(description = "ID do paciente", example = "1")
        Long id,

        @Schema(description = "Nome completo", example = "Maria Clara Santos")
        String nome,

        @Schema(description = "E-mail", example = "maria.santos@email.com")
        String email,

        @Schema(description = "DDD", example = "11")
        String ddd,

        @Schema(description = "Telefone", example = "987654321")
        String telefone,

        @Schema(description = "Sexo", example = "FEMININO")
        Sexo sexo,

        @Schema(description = "Data de nascimento", example = "1990-05-20")
        LocalDate dataNascimento,

        @Schema(description = "Idade calculada em anos", example = "36")
        Integer idade,

        @Schema(description = "Endereço do paciente")
        EnderecoResponse endereco,

        @Schema(description = "Login de acesso", example = "mariaclara")
        String login,

        @Schema(description = "Perfil de acesso", example = "PACIENTE")
        Role role,

        @Schema(description = "CPF do paciente", example = "12345678901")
        String cpf,

        @Schema(description = "Indica se o cadastro está ativo", example = "true")
        Boolean ativo
) {}
