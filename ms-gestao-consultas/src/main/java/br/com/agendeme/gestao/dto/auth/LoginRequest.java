package br.com.agendeme.gestao.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados de autenticação do usuário")
public record LoginRequest(
        @Schema(description = "Login do usuário", example = "joaomedico")
        @NotBlank String login,

        @Schema(description = "Senha do usuário", example = "Senha@123")
        @NotBlank String senha
) {}