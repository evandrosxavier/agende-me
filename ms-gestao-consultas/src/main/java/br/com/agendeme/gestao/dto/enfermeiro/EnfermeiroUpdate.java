package br.com.agendeme.gestao.dto.enfermeiro;

import br.com.agendeme.gestao.dto.endereco.EnderecoUpdate;
import br.com.agendeme.gestao.model.enums.Sexo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para atualização parcial do enfermeiro (todos os campos são opcionais)")
public record EnfermeiroUpdate(

        @Schema(description = "Nome completo", example = "Ana Paula Oliveira")
        @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
        String nome,

        @Schema(description = "DDD do telefone", example = "31")
        @Size(min = 2, max = 2, message = "DDD deve ter 2 dígitos")
        @Pattern(regexp = "\\d{2}", message = "DDD deve ter 2 dígitos")
        String ddd,

        @Schema(description = "Número do telefone", example = "995551234")
        @Size(min = 8, max = 9, message = "Telefone deve ter entre 8 e 9 dígitos")
        @Pattern(regexp = "\\d{8,9}", message = "Telefone inválido")
        String telefone,

        @Schema(description = "Sexo do enfermeiro", example = "FEMININO")
        Sexo sexo,

        @Schema(description = "Dados de endereço para atualização parcial")
        @Valid
        EnderecoUpdate endereco,

        @Schema(description = "Número do CRE", example = "502721")
        @Pattern(regexp = "\\d{4,10}", message = "CRE deve ter entre 4 e 10 dígitos")
        String cre,

        @Schema(description = "UF do CRE", example = "MG")
        @Pattern(regexp = "[A-Z]{2}", message = "UF deve ter 2 letras maiúsculas")
        String creUf
) {}
