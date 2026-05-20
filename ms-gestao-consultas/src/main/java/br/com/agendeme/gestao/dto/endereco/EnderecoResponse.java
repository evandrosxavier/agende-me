package br.com.agendeme.gestao.dto.endereco;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados de endereço retornados na resposta")
public record EnderecoResponse(
        @Schema(description = "Logradouro", example = "Rua das Flores")
        String logradouro,

        @Schema(description = "Número do imóvel", example = "123")
        String numero,

        @Schema(description = "Complemento", example = "Apto 42")
        String complemento,

        @Schema(description = "Bairro", example = "Centro")
        String bairro,

        @Schema(description = "Cidade", example = "São Paulo")
        String cidade,

        @Schema(description = "UF", example = "SP")
        String uf,

        @Schema(description = "CEP", example = "01310100")
        String cep
) {}
