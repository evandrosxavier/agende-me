package br.com.agendeme.gestao.dto.medico;

import br.com.agendeme.gestao.dto.endereco.EnderecoResponse;
import br.com.agendeme.gestao.model.enums.Especialidade;
import br.com.agendeme.gestao.model.enums.Role;
import br.com.agendeme.gestao.model.enums.Sexo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Dados do médico retornados na resposta")
public record MedicoResponse(
        @Schema(description = "ID do médico", example = "2")
        Long id,

        @Schema(description = "Nome completo", example = "Dr. Carlos Eduardo Ferreira")
        String nome,

        @Schema(description = "E-mail", example = "carlos.ferreira@hospital.com")
        String email,

        @Schema(description = "DDD", example = "11")
        String ddd,

        @Schema(description = "Telefone", example = "991234567")
        String telefone,

        @Schema(description = "Sexo", example = "MASCULINO")
        Sexo sexo,

        @Schema(description = "Data de nascimento", example = "1980-03-10")
        LocalDate dataNascimento,

        @Schema(description = "Idade calculada em anos", example = "46")
        Integer idade,

        @Schema(description = "Endereço do médico")
        EnderecoResponse endereco,

        @Schema(description = "Login de acesso", example = "carlosmed")
        String login,

        @Schema(description = "Perfil de acesso", example = "MEDICO")
        Role role,

        @Schema(description = "Número do CRM", example = "123456")
        String crm,

        @Schema(description = "UF do CRM", example = "SP")
        String crmUf,

        @Schema(description = "Especialidade médica", example = "CARDIOLOGIA")
        Especialidade especialidade,

        @Schema(description = "Indica se o cadastro está ativo", example = "true")
        Boolean ativo
) {}