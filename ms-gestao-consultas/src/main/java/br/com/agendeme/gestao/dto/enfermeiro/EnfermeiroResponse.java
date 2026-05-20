package br.com.agendeme.gestao.dto.enfermeiro;

import br.com.agendeme.gestao.dto.endereco.EnderecoResponse;
import br.com.agendeme.gestao.model.enums.Role;
import br.com.agendeme.gestao.model.enums.Sexo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Dados do enfermeiro retornados na resposta")
public record EnfermeiroResponse(
        @Schema(description = "ID do enfermeiro", example = "3")
        Long id,

        @Schema(description = "Nome completo", example = "Ana Paula Oliveira")
        String nome,

        @Schema(description = "E-mail", example = "ana.oliveira@hospital.com")
        String email,

        @Schema(description = "DDD", example = "31")
        String ddd,

        @Schema(description = "Telefone", example = "995551234")
        String telefone,

        @Schema(description = "Sexo", example = "FEMININO")
        Sexo sexo,

        @Schema(description = "Data de nascimento", example = "1990-03-15")
        LocalDate dataNascimento,

        @Schema(description = "Idade calculada em anos", example = "36")
        Integer idade,

        @Schema(description = "Endereço do enfermeiro")
        EnderecoResponse endereco,

        @Schema(description = "Login de acesso", example = "anaenf")
        String login,

        @Schema(description = "Perfil de acesso", example = "ENFERMEIRO")
        Role role,

        @Schema(description = "Número do CRE", example = "502721")
        String cre,

        @Schema(description = "UF do CRE", example = "MG")
        String creUf,

        @Schema(description = "Indica se o cadastro está ativo", example = "true")
        Boolean ativo
) {}
