package br.com.agendeme.gestao.dto.paciente;

import br.com.agendeme.gestao.dto.endereco.EnderecoRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Todos os campos são opcionais — enviar apenas o que deseja alterar
public record PacienteUpdate(

        @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
        String nome,

        @Pattern(regexp = "\\d{2}", message = "DDD deve ter 2 dígitos")
        String ddd,

        @Size(min = 8, max = 20, message = "Telefone deve ter no mínimo 8 e máximo 20 caracteres")
        String telefone,

        @Valid
        EnderecoRequest endereco,

        @Pattern(regexp = "\\d{11}", message = "CPF deve conter 11 dígitos numéricos sem pontuação")
        String cpf
) {}


