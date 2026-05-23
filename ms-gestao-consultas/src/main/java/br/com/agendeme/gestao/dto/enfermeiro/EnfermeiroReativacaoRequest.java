package br.com.agendeme.gestao.dto.enfermeiro;

import br.com.agendeme.gestao.dto.endereco.EnderecoRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

@Schema(description = "Dados para reativação de um enfermeiro inativo. Nome, sexo e data de nascimento são imutáveis e não podem ser alterados. Apenas os campos informados serão atualizados.")
public record EnfermeiroReativacaoRequest(

        @Schema(description = "E-mail do enfermeiro", example = "ana.oliveira@hospital.com")
        @Email(message = "E-mail inválido")
        @Size(max = 50, message = "E-mail deve ter no máximo 50 caracteres")
        String email,

        @Schema(description = "DDD do telefone (2 dígitos)", example = "31")
        @Pattern(regexp = "\\d{2}", message = "DDD deve ter 2 dígitos")
        String ddd,

        @Schema(description = "Número do telefone (8 ou 9 dígitos)", example = "995551234")
        @Pattern(regexp = "\\d{8,9}", message = "Telefone inválido")
        String telefone,

        @Schema(description = "Endereço do enfermeiro. Se informado, substitui o endereço atual por completo.")
        @Valid
        EnderecoRequest endereco,

        @Schema(description = "Nova senha de acesso (mínimo 8 caracteres, com letra, número e caractere especial)", example = "Senha@123")
        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 8, max = 72, message = "Senha deve ter entre 8 e 72 caracteres")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@#$!%*?&]).+$",
                message = "Senha deve conter letra, número e caractere especial"
        )
        String senha
) {}
