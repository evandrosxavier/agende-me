package br.com.agendeme.gestao.dto.endereco;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EnderecoUpdate(

        @Size(max = 150, message = "Logradouro deve ter no máximo 150 caracteres")
        String logradouro,

        @Size(max = 10, message = "Número deve ter no máximo 10 caracteres")
        String numero,

        @Size(max = 50, message = "Complemento deve ter no máximo 50 caracteres")
        String complemento,

        @Size(max = 100, message = "Bairro deve ter no máximo 100 caracteres")
        String bairro,

        @Size(max = 100, message = "Cidade deve ter no máximo 100 caracteres")
        String cidade,

        @Size(min = 2, max = 2, message = "UF deve ter exatamente 2 caracteres")
        String uf,

        @Pattern(regexp = "\\d{8}", message = "CEP deve conter 8 dígitos numéricos sem hífen")
        String cep
) {}

