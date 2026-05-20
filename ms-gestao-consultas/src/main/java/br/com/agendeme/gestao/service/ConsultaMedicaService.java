package br.com.agendeme.gestao.service;

import br.com.agendeme.gestao.dto.consulta.ConsultaAgendamentoResponse;
import br.com.agendeme.gestao.dto.consulta.ConsultaAgendamentoUpdate;
import br.com.agendeme.gestao.dto.consulta.ConsultaAtendimentoUpdate;
import br.com.agendeme.gestao.dto.consulta.ConsultaAtendimentoResponse;
import br.com.agendeme.gestao.dto.consulta.ConsultaRequest;
import br.com.agendeme.gestao.excecoes.BusinessException;
import br.com.agendeme.gestao.excecoes.ErrorCode;
import br.com.agendeme.gestao.mapper.ConsultaMedicaMapper;
import br.com.agendeme.gestao.model.domain.ConsultaMedica;
import br.com.agendeme.gestao.model.enums.Especialidade;
import br.com.agendeme.gestao.model.domain.Medico;
import br.com.agendeme.gestao.model.domain.Paciente;
import br.com.agendeme.gestao.model.enums.StatusConsulta;
import br.com.agendeme.gestao.repository.ConsultaMedicaRepository;
import br.com.agendeme.gestao.repository.MedicoRepository;
import br.com.agendeme.gestao.repository.PacienteRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ConsultaMedicaService {

    private final ConsultaMedicaRepository consultaRepository;
    private final MedicoRepository medicoRepository;
    private final PacienteRepository pacienteRepository;
    private final ConsultaMedicaMapper consultaMedicaMapper;
    private final EntityManager entityManager;
    private final KafkaProducerService kafkaProducerService;

    @Transactional
    public ConsultaAgendamentoResponse cadastrarConsulta(ConsultaRequest dto) {

        Paciente paciente = pacienteRepository.findById(dto.pacienteId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PACIENTE_NAO_ENCONTRADO, HttpStatus.NOT_FOUND));

        if (!paciente.getAtivo()) {
            throw new BusinessException(ErrorCode.PACIENTE_INATIVO_CONSULTA, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        Medico medico = medicoRepository.findById(dto.medicoId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEDICO_NAO_ENCONTRADO, HttpStatus.NOT_FOUND));

        if (!medico.getAtivo()) {
            throw new BusinessException(ErrorCode.MEDICO_INATIVO_CONSULTA, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        ConsultaMedica consulta = consultaMedicaMapper.toEntity(dto);
        consulta.setMedico(medico);
        consulta.setPaciente(paciente);
        consulta.setStatus(StatusConsulta.AGENDADA);
        ConsultaMedica consultaSalva = consultaRepository.save(consulta);

        kafkaProducerService.enviarNotificacao(
                "consulta-agendada",
                consultaMedicaMapper.toNotificacaoDTO(consultaSalva)
        );

        return consultaMedicaMapper.consultaAgendadaToResponseDTO(consultaSalva);

    }

    @Transactional
    public ConsultaAgendamentoResponse atualizarConsultaAgendada(Long id, ConsultaAgendamentoUpdate dto) {

        ConsultaMedica consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONSULTA_NAO_ENCONTRADA, HttpStatus.NOT_FOUND));

        if (!consulta.getPaciente().getAtivo()) {
            throw new BusinessException(ErrorCode.PACIENTE_INATIVO_CONSULTA, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        if (dto.medicoId() != null) {
            Medico medico = medicoRepository.findById(dto.medicoId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.MEDICO_NAO_ENCONTRADO, HttpStatus.NOT_FOUND));
            if (!medico.getAtivo()) {
                throw new BusinessException(ErrorCode.MEDICO_INATIVO_CONSULTA, HttpStatus.UNPROCESSABLE_ENTITY);
            }
            consulta.setMedico(medico);
            consulta.setEspecialidade(medico.getEspecialidade());
        } else if (!consulta.getMedico().getAtivo()) {
            throw new BusinessException(ErrorCode.MEDICO_INATIVO_CONSULTA, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        consultaMedicaMapper.updateEntityFromDTO(dto, consulta);
        consultaRepository.saveAndFlush(consulta);
        entityManager.refresh(consulta);

        kafkaProducerService.enviarNotificacao(
                "consulta-agendamento-atualizado",
                consultaMedicaMapper.toNotificacaoDTO(consulta)
        );
        return consultaMedicaMapper.consultaAgendadaToResponseDTO(consulta);
    }

    @Transactional
    public ConsultaAtendimentoResponse registrarAtendimento(Long id, ConsultaAtendimentoUpdate dto) {

        ConsultaMedica consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONSULTA_NAO_ENCONTRADA, HttpStatus.NOT_FOUND));

        if (consulta.getStatus() != StatusConsulta.AGENDADA) {
            throw new BusinessException(ErrorCode.CONSULTA_NAO_PODE_SER_REGISTRADA, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        consultaMedicaMapper.updateEntityFromDTO(dto, consulta);

        consulta.setStatus(StatusConsulta.REALIZADA);

        consultaRepository.saveAndFlush(consulta);
        entityManager.refresh(consulta);

        kafkaProducerService.enviarNotificacao(
                "consulta-atendimento-registrado",
                consultaMedicaMapper.toNotificacaoDTO(consulta)
        );

        return consultaMedicaMapper.consultaRealizadaToResponseDTO(consulta);
    }



    @Transactional
    public ConsultaAtendimentoResponse atualizarAtendimentoRealizado(Long id, ConsultaAtendimentoUpdate dto) {

        ConsultaMedica consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONSULTA_NAO_ENCONTRADA, HttpStatus.NOT_FOUND));

        if (consulta.getStatus() != StatusConsulta.REALIZADA) {
            throw new BusinessException(ErrorCode.CONSULTA_NAO_ESTA_REALIZADA, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        consultaMedicaMapper.updateEntityFromDTO(dto, consulta);

        consultaRepository.saveAndFlush(consulta);
        entityManager.refresh(consulta);

        kafkaProducerService.enviarNotificacao(
                "consulta-atendimento-atualizado",
                consultaMedicaMapper.toNotificacaoDTO(consulta)
        );
        return consultaMedicaMapper.consultaRealizadaToResponseDTO(consulta);
    }

    @Transactional
    public ConsultaAgendamentoResponse cancelar(Long id) {
        ConsultaMedica consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONSULTA_NAO_ENCONTRADA, HttpStatus.NOT_FOUND));

        if (consulta.getStatus() == StatusConsulta.CANCELADA) {
            throw new BusinessException(ErrorCode.CONSULTA_JA_CANCELADA, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        if (consulta.getStatus() == StatusConsulta.REALIZADA) {
            throw new BusinessException(ErrorCode.CONSULTA_NAO_PODE_SER_CANCELADA, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        consulta.setStatus(StatusConsulta.CANCELADA);
        consultaRepository.saveAndFlush(consulta);
        entityManager.refresh(consulta);
        kafkaProducerService.enviarNotificacao(
                "consulta-cancelada",
                consultaMedicaMapper.toNotificacaoDTO(consulta)
        );

        return consultaMedicaMapper.consultaAgendadaToResponseDTO(consulta);
    }

    @Transactional(readOnly = true)
    public Page<ConsultaAgendamentoResponse> buscarPorCpfPaciente(String cpf, Pageable pageable) {

        if (pacienteRepository.findByCpf(cpf).isEmpty()) {
            throw new BusinessException(ErrorCode.PACIENTE_NAO_ENCONTRADO, HttpStatus.NOT_FOUND);
        }
        return consultaRepository.findByPacienteCpf(cpf, pageable)
                .map(consultaMedicaMapper::consultaAgendadaToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<ConsultaAgendamentoResponse> buscarPorCrmMedico(String crm, Pageable pageable) {
        if (medicoRepository.findByCrm(crm).isEmpty()) {
            throw new BusinessException(ErrorCode.MEDICO_NAO_ENCONTRADO, HttpStatus.NOT_FOUND);
        }
        return consultaRepository.findByMedicoCrm(crm, pageable)
                .map(consultaMedicaMapper::consultaAgendadaToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<ConsultaAgendamentoResponse> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim, Pageable pageable) {
        if (inicio.isAfter(fim)) {
            throw new BusinessException(ErrorCode.PERIODO_INVALIDO, HttpStatus.BAD_REQUEST);
        }
        return consultaRepository.findByDataHoraBetween(inicio, fim, pageable)
                .map(consultaMedicaMapper::consultaAgendadaToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<ConsultaAgendamentoResponse> buscarPorStatus(StatusConsulta status, Pageable pageable) {
        return consultaRepository.findByStatus(status, pageable)
                .map(consultaMedicaMapper::consultaAgendadaToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<ConsultaAgendamentoResponse> buscarPorEspecialidade(Especialidade especialidade, Pageable pageable) {
        return consultaRepository.findByEspecialidade(especialidade, pageable)
                .map(consultaMedicaMapper::consultaAgendadaToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<ConsultaAgendamentoResponse> buscarConsultasDoPaciente(String login, Pageable pageable) {
        Paciente paciente = pacienteRepository.findByLogin(login)
                .orElseThrow(() -> new BusinessException(ErrorCode.PACIENTE_NAO_ENCONTRADO, HttpStatus.NOT_FOUND));
        return consultaRepository.findByPacienteCpf(paciente.getCpf(), pageable)
                .map(consultaMedicaMapper::consultaAgendadaToResponseDTO);
    }

}

