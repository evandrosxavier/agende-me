package br.com.agendeme.gestao.dto.medico;

import br.com.agendeme.gestao.dto.endereco.EnderecoUpdate;
import br.com.agendeme.gestao.model.enums.Especialidade;
import br.com.agendeme.gestao.model.enums.Sexo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para atualização parcial do médico (todos os campos são opcionais)")
public record MedicoUpdate(

        @Schema(description = "Nome completo", example = "Dr. Carlos Eduardo Ferreira")
        @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
        String nome,

        @Schema(description = "DDD do telefone", example = "11")
        @Size(min = 2, max = 2, message = "DDD deve ter 2 dígitos")
        @Pattern(regexp = "\\d{2}", message = "DDD deve ter 2 dígitos")
        String ddd,

        @Schema(description = "Número do telefone", example = "991234567")
        @Size(min = 8, max = 9, message = "Telefone deve ter entre 8 e 9 dígitos")
        @Pattern(regexp = "\\d{8,9}", message = "Telefone inválido")
        String telefone,

        @Schema(description = "Sexo do médico", example = "MASCULINO")
        Sexo sexo,

        @Schema(description = "Dados de endereço para atualização parcial")
        @Valid
        EnderecoUpdate endereco,

        @Schema(description = "Número do CRM", example = "123456")
        @Pattern(regexp = "\\d{4,10}", message = "CRM deve ter no máximo 10 caracteres")
        String crm,

        @Schema(description = "UF do CRM", example = "SP")
        @Pattern(regexp = "[A-Z]{2}", message = "UF deve ter 2 letras maiúsculas")
        String crmUf,

        @Schema(description = "Especialidade médica", example = "CARDIOLOGIA")
        Especialidade especialidade
) {}
