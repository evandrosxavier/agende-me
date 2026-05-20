package br.com.agendeme.gestao.dto.enfermeiro;

import br.com.agendeme.gestao.dto.endereco.EnderecoResponse;
import br.com.agendeme.gestao.model.enums.Role;
import br.com.agendeme.gestao.model.enums.Sexo;

import java.time.LocalDate;

public record EnfermeiroResponse(
        Long id,
        String nome,
        String email,
        String ddd,
        String telefone,
        Sexo sexo,
        LocalDate dataNascimento,
        Integer idade,
        EnderecoResponse endereco,
        String login,
        Role role,
        String cre,
        String creUf,
        Boolean ativo
) {}
