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
import br.com.agendeme.gestao.model.domain.Especialidade;
import br.com.agendeme.gestao.model.domain.Medico;
import br.com.agendeme.gestao.model.domain.Paciente;
import br.com.agendeme.gestao.model.domain.StatusConsulta;
import br.com.agendeme.gestao.repository.ConsultaMedicaRepository;
import br.com.agendeme.gestao.repository.MedicoRepository;
import br.com.agendeme.gestao.repository.PacienteRepository;
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

    @Transactional
    public ConsultaAgendamentoResponse cadastrarConsulta(ConsultaRequest dto) {

        Paciente paciente = pacienteRepository.findById(dto.pacienteId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PACIENTE_NAO_ENCONTRADO, HttpStatus.NOT_FOUND));

        Medico medico = medicoRepository.findById(dto.medicoId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEDICO_NAO_ENCONTRADO, HttpStatus.NOT_FOUND));

        ConsultaMedica consulta = consultaMedicaMapper.toEntity(dto);
        consulta.setMedico(medico);
        consulta.setPaciente(paciente);
        consulta.setStatus(StatusConsulta.AGENDADA);

        ConsultaMedica consultaSalva = consultaRepository.save(consulta);
        return consultaMedicaMapper.consultaAgendadaToResponseDTO(consultaSalva);

    }

    @Transactional
    public ConsultaAgendamentoResponse atualizarConsultaAgendada(Long id, ConsultaAgendamentoUpdate dto) {

        ConsultaMedica consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONSULTA_NAO_ENCONTRADA, HttpStatus.NOT_FOUND));

        if (dto.medicoId() != null) {
            Medico medico = medicoRepository.findById(dto.medicoId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.MEDICO_NAO_ENCONTRADO, HttpStatus.NOT_FOUND));
            consulta.setMedico(medico);
        }
        consultaMedicaMapper.updateEntityFromDTO(dto, consulta);
        ConsultaMedica atualizada = consultaRepository.save(consulta);
        return consultaMedicaMapper.consultaAgendadaToResponseDTO(atualizada);
    }


    @Transactional
    public ConsultaAtendimentoResponse atualizarConsultaRealizada(Long id, ConsultaAtendimentoUpdate dto) {

        ConsultaMedica consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONSULTA_NAO_ENCONTRADA, HttpStatus.NOT_FOUND));

        consultaMedicaMapper.updateEntityFromDTO(dto, consulta);

        consulta.setStatus(StatusConsulta.REALIZADA);

        ConsultaMedica atualizada = consultaRepository.save(consulta);
        return consultaMedicaMapper.consultaRealizadaToResponseDTO(atualizada);
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
        return consultaMedicaMapper.consultaAgendadaToResponseDTO(consultaRepository.save(consulta));
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

}

