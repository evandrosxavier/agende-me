package br.com.agendeme.gestao.dto.endereco;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados de endereço para cadastro")
public record EnderecoRequest(

        @Schema(description = "Logradouro (rua, avenida, etc.)", example = "Rua das Flores")
        @NotBlank(message = "Logradouro é obrigatório")
        @Size(max = 150, message = "Logradouro deve ter no máximo 150 caracteres")
        String logradouro,

        @Schema(description = "Número do imóvel", example = "123")
        @NotBlank(message = "Número é obrigatório")
        @Size(max = 10, message = "Número deve ter no máximo 10 caracteres")
        String numero,

        @Schema(description = "Complemento do endereço", example = "Apto 42")
        @Size(max = 50, message = "Complemento deve ter no máximo 50 caracteres")
        String complemento,

        @Schema(description = "Bairro", example = "Centro")
        @NotBlank(message = "Bairro é obrigatório")
        @Size(max = 100, message = "Bairro deve ter no máximo 100 caracteres")
        String bairro,

        @Schema(description = "Cidade", example = "São Paulo")
        @NotBlank(message = "Cidade é obrigatória")
        @Size(max = 100, message = "Cidade deve ter no máximo 100 caracteres")
        String cidade,

        @Schema(description = "UF (sigla do estado)", example = "SP")
        @NotBlank(message = "UF é obrigatória")
        @Size(min = 2, max = 2, message = "UF deve ter exatamente 2 caracteres")
        String uf,

        @Schema(description = "CEP com 8 dígitos numéricos sem hífen", example = "01310100")
        @NotBlank(message = "CEP é obrigatório")
        @Pattern(regexp = "\\d{8}", message = "CEP deve conter 8 dígitos numéricos sem hífen")
        String cep
) {}
