package br.com.agendeme.historico.controller;

import br.com.agendeme.historico.dto.HistoricoConsultaDTO;
import br.com.agendeme.historico.dto.HistoricoConsultaResumo;
import br.com.agendeme.historico.service.HistoricoConsultaService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HistoricoConsultaController {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;

    private final HistoricoConsultaService historicoService;

    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO', 'ADMIN')")
    @QueryMapping
    public List<HistoricoConsultaDTO> buscarPorPacienteCpf(
            @Argument String cpf,
            @Argument Integer page,
            @Argument Integer size) {
        return historicoService.buscarPorPacienteCpf(cpf, pageable(page, size)).getContent();
    }
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO', 'ADMIN')")
    @QueryMapping
    public List<HistoricoConsultaDTO> buscarPorPacienteCpfAposData(
            @Argument String cpf,
            @Argument String dataHora,
            @Argument Integer page,
            @Argument Integer size) {
        return historicoService.buscarPorPacienteCpfAposData(cpf, dataHora, pageable(page, size)).getContent();
    }
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO', 'ADMIN')")
    @QueryMapping
    public List<HistoricoConsultaDTO> buscarPorPacienteNome(
            @Argument String nome,
            @Argument Integer page,
            @Argument Integer size) {
        return historicoService.buscarPorPacienteNome(nome, pageable(page, size)).getContent();
    }

    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO', 'ADMIN')")
    @QueryMapping
    public List<HistoricoConsultaDTO> buscarPorMedicoCrm(
            @Argument String crm,
            @Argument Integer page,
            @Argument Integer size) {
        return historicoService.buscarPorMedicoCrm(crm, pageable(page, size)).getContent();
    }
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO', 'ADMIN')")
    @QueryMapping
    public List<HistoricoConsultaDTO> buscarPorMedicoNome(
            @Argument String nome,
            @Argument Integer page,
            @Argument Integer size) {
        return historicoService.buscarPorMedicoNome(nome, pageable(page, size)).getContent();
    }
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO', 'ADMIN')")
    @QueryMapping
    public List<HistoricoConsultaDTO> buscarPorStatus(
            @Argument String status,
            @Argument Integer page,
            @Argument Integer size) {
        return historicoService.buscarPorStatus(status, pageable(page, size)).getContent();
    }

    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO', 'ADMIN')")
    @QueryMapping
    public List<HistoricoConsultaDTO> buscarPorPeriodo(
            @Argument String inicio,
            @Argument String fim,
            @Argument Integer page,
            @Argument Integer size) {
        return historicoService.buscarPorPeriodo(inicio, fim, pageable(page, size)).getContent();
    }

    @PreAuthorize("hasRole('PACIENTE')")
    @QueryMapping
    public List<HistoricoConsultaResumo> minhasConsultas(
            @Argument Integer page,
            @Argument Integer size) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String cpf = (String) auth.getCredentials();
        return historicoService.buscarMinhasConsultas(cpf, pageable(page, size)).getContent();
    }


    @PreAuthorize("hasRole('ADMIN')")
    @QueryMapping
    public List<HistoricoConsultaDTO> auditoriaBuscarPorCpf(
            @Argument String cpf,
            @Argument Integer page,
            @Argument Integer size) {
        return historicoService.auditoriaBuscarPorCpf(cpf, pageable(page, size)).getContent();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @QueryMapping
    public List<HistoricoConsultaDTO> auditoriaBuscarPorPeriodo(
            @Argument String inicio,
            @Argument String fim,
            @Argument Integer page,
            @Argument Integer size) {
        return historicoService.auditoriaBuscarPorPeriodo(inicio, fim, pageable(page, size)).getContent();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @QueryMapping
    public List<HistoricoConsultaDTO> auditoriaBuscarPorConsultaId(
            @Argument Long consultaId,
            @Argument Integer page,
            @Argument Integer size) {
        return historicoService.auditoriaBuscarPorConsultaId(consultaId, pageable(page, size)).getContent();
    }

    private Pageable pageable(Integer page, Integer size) {
        return PageRequest.of(
                page != null ? page : DEFAULT_PAGE,
                size != null ? size : DEFAULT_SIZE
        );
    }
}
