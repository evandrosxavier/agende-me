package br.com.agendeme.gestao.dto.medico;

import br.com.agendeme.gestao.dto.endereco.EnderecoResponse;
import br.com.agendeme.gestao.model.domain.Especialidade;
import br.com.agendeme.gestao.model.domain.Role;
import br.com.agendeme.gestao.model.domain.Sexo;
import java.time.LocalDate;

public record MedicoResponse(
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
        String crm,
        String crmUf,
        Especialidade especialidade,
        Boolean ativo
) {}