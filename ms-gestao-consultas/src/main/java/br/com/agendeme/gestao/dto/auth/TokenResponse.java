package br.com.agendeme.gestao.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Token JWT gerado após autenticação bem-sucedida")
public record TokenResponse(
        @Schema(description = "Token JWT de acesso", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String token
) {}
