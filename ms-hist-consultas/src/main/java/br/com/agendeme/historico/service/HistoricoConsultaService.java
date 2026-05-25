package br.com.agendeme.historico.service;

import br.com.agendeme.historico.dto.ConsultaEventDTO;
import br.com.agendeme.historico.dto.HistoricoConsultaDTO;
import br.com.agendeme.historico.dto.HistoricoConsultaResumo;
import br.com.agendeme.historico.excecoes.BusinessException;
import br.com.agendeme.historico.excecoes.ErrorCode;
import br.com.agendeme.historico.model.HistoricoConsulta;
import br.com.agendeme.historico.model.StatusConsulta;
import br.com.agendeme.historico.repository.HistoricoConsultaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class HistoricoConsultaService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final HistoricoConsultaRepository historicoRepository;


    @Transactional(readOnly = true)
    public Page<HistoricoConsultaDTO> buscarPorPacienteCpf(String cpf, Pageable pageable) {
        return historicoRepository.findUltimoEstadoPorPacienteCpf(normalizarCpf(cpf), pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<HistoricoConsultaDTO> buscarPorPacienteCpfAposData(String cpf, String dataHora, Pageable pageable) {
        LocalDateTime data = parseDataHora(dataHora);
        return historicoRepository.findUltimoEstadoPorPacienteCpfAposData(normalizarCpf(cpf), data, pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<HistoricoConsultaDTO> buscarPorPacienteNome(String nome, Pageable pageable) {
        return historicoRepository.findUltimoEstadoPorPacienteNome(nome, pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<HistoricoConsultaDTO> buscarPorMedicoCrm(String crm, Pageable pageable) {
        return historicoRepository.findUltimoEstadoPorMedicoCrm(crm, pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<HistoricoConsultaDTO> buscarPorMedicoNome(String nome, Pageable pageable) {
        return historicoRepository.findUltimoEstadoPorMedicoNome(nome, pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<HistoricoConsultaDTO> buscarPorStatus(String status, Pageable pageable) {
        if (!StatusConsulta.isValid(status)) {
            throw new BusinessException(ErrorCode.STATUS_INVALIDO, HttpStatus.BAD_REQUEST);
        }
        return historicoRepository.findUltimoEstadoPorStatus(status.toUpperCase(), pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<HistoricoConsultaDTO> buscarPorPeriodo(String inicio, String fim, Pageable pageable) {
        LocalDateTime dtInicio = parseDataHora(inicio);
        LocalDateTime dtFim = parseDataHora(fim);
        return historicoRepository.findUltimoEstadoPorPeriodo(dtInicio, dtFim, pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<HistoricoConsultaResumo> buscarMinhasConsultas(String cpf, Pageable pageable) {
        if (cpf == null || cpf.isBlank()) {
            throw new BusinessException(ErrorCode.STATUS_INVALIDO, HttpStatus.FORBIDDEN);
        }
        return historicoRepository.findUltimoEstadoPorPacienteCpf(normalizarCpf(cpf), pageable).map(this::toDTOResumo);
    }


    @Transactional(readOnly = true)
    public Page<HistoricoConsultaDTO> auditoriaBuscarPorCpf(String cpf, Pageable pageable) {
        return historicoRepository.findAuditoriaPorCpf(normalizarCpf(cpf), pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<HistoricoConsultaDTO> auditoriaBuscarPorPeriodo(String inicio, String fim, Pageable pageable) {
        LocalDateTime dtInicio = parseDataHora(inicio);
        LocalDateTime dtFim = parseDataHora(fim);
        return historicoRepository.findAuditoriaPorPeriodo(dtInicio, dtFim, pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<HistoricoConsultaDTO> auditoriaBuscarPorConsultaId(Long consultaId, Pageable pageable) {
        return historicoRepository.findAuditoriaPorConsultaId(consultaId, pageable).map(this::toDTO);
    }

    @Transactional
    public void salvarHistorico(ConsultaEventDTO dto, String tipoEvento) {
        LocalDateTime dataEvento = LocalDateTime.parse(dto.dataEvento());

        if (historicoRepository.findByConsultaIdAndDataEvento(dto.consultaId(), dataEvento).isPresent()) {
            log.warn("Evento duplicado ignorado | consultaId={} | dataEvento={}", dto.consultaId(), dataEvento);
            return;
        }

        try {
            HistoricoConsulta historico = HistoricoConsulta.builder()
                    .consultaId(dto.consultaId())
                    .pacienteNome(dto.pacienteNome())
                    .pacienteCpf(dto.pacienteCpf())
                    .pacienteEmail(dto.pacienteEmail())
                    .medicoNome(dto.medicoNome())
                    .medicoCrm(dto.medicoCrm())
                    .especialidade(dto.especialidade())
                    .dataHora(parseDataHora(dto.dataHora()))
                    .diagnostico(dto.diagnostico())
                    .tratamentoProposto(dto.tratamentoProposto())
                    .demaisObservacoes(dto.demaisObservacoes())
                    .status(dto.status())
                    .tipoEvento(tipoEvento)
                    .dataEvento(dataEvento)
                    .build();
            historicoRepository.save(historico);
            log.info("Histórico salvo | consultaId={} | tipoEvento {} | status={}", dto.consultaId(), tipoEvento, dto.status());
        } catch (Exception e) {
            log.error("Erro ao salvar histórico | consultaId={} | erro={}", dto.consultaId(), e.getMessage());
            throw e;
        }
    }

    private LocalDateTime parseDataHora(String dataHora) {
        if (dataHora == null || dataHora.isBlank()) return null;
        try {
            return LocalDateTime.parse(dataHora, FORMATTER);
        } catch (Exception e) {
            return LocalDateTime.parse(dataHora);
        }
    }

    private String normalizarCpf(String cpf) {
        if (cpf == null) return null;
        return cpf.replaceAll("[^0-9]", "");
    }

    private HistoricoConsultaDTO toDTO(HistoricoConsulta entity) {
        return new HistoricoConsultaDTO(
                entity.getId(),
                entity.getConsultaId(),
                entity.getPacienteNome(),
                entity.getPacienteCpf(),
                entity.getPacienteEmail(),
                entity.getMedicoNome(),
                entity.getMedicoCrm(),
                entity.getEspecialidade(),
                entity.getDataHora() != null ? entity.getDataHora().format(FORMATTER) : null,
                entity.getStatus(),
                entity.getTipoEvento(),
                entity.getDataEvento() != null ? entity.getDataEvento().format(FORMATTER) : null,
                entity.getDataDoRegistro() != null ? entity.getDataDoRegistro().format(FORMATTER) : null,
                entity.getDiagnostico(),
                entity.getTratamentoProposto(),
                entity.getDemaisObservacoes()
        );
    }

    private HistoricoConsultaResumo toDTOResumo(HistoricoConsulta entity) {
        return new HistoricoConsultaResumo(
                entity.getId(),
                entity.getConsultaId(),
                entity.getPacienteNome(),
                entity.getPacienteCpf(),
                entity.getPacienteEmail(),
                entity.getMedicoNome(),
                entity.getMedicoCrm(),
                entity.getEspecialidade(),
                entity.getDataHora() != null ? entity.getDataHora().format(FORMATTER) : null,
                entity.getStatus(),
                entity.getDataEvento() != null ? entity.getDataEvento().format(FORMATTER) : null,
                entity.getDataDoRegistro() != null ? entity.getDataDoRegistro().format(FORMATTER) : null
        );
    }
}
