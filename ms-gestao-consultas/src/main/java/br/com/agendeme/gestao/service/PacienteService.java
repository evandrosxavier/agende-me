package br.com.agendeme.gestao.service;

import br.com.agendeme.gestao.dto.paciente.PacienteRequest;
import br.com.agendeme.gestao.dto.paciente.PacienteResponse;
import br.com.agendeme.gestao.dto.paciente.PacienteUpdate;
import br.com.agendeme.gestao.mapper.PacienteMapper;
import br.com.agendeme.gestao.model.domain.Paciente;
import br.com.agendeme.gestao.model.domain.Role;
import br.com.agendeme.gestao.repository.PacienteRepository;
import br.com.agendeme.gestao.repository.UsuarioRepository;
import br.com.agendeme.gestao.excecoes.BusinessException;
import br.com.agendeme.gestao.excecoes.ErrorCode;
import org.springframework.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final PacienteMapper pacienteMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public PacienteResponse cadastrar(PacienteRequest dto) {
        if (pacienteRepository.findByCpf(dto.cpf()).isPresent()) {
            throw new BusinessException(ErrorCode.CPF_JA_CADASTRADO, HttpStatus.CONFLICT);
        }
        if (usuarioRepository.findByLogin(dto.login()).isPresent()) {
            throw new BusinessException(ErrorCode.LOGIN_ALREADY_EXISTS, HttpStatus.CONFLICT);
        }
        if (usuarioRepository.findByEmail(dto.email()).isPresent()) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS, HttpStatus.CONFLICT);
        }

        Paciente paciente = pacienteMapper.toEntity(dto);
        paciente.setSenha(passwordEncoder.encode(dto.senha()));
        paciente.setRole(Role.PACIENTE);
        paciente.setAtivo(true);
        return pacienteMapper.toResponseDTO(pacienteRepository.save(paciente));
    }

    @Transactional(readOnly = true)
    public PacienteResponse buscarPorCpf(String cpf) {
        Paciente paciente = pacienteRepository.findByCpf(cpf)
                .orElseThrow(() -> new BusinessException(ErrorCode.PACIENTE_NAO_ENCONTRADO, HttpStatus.NOT_FOUND));
        return pacienteMapper.toResponseDTO(paciente);
    }

    @Transactional(readOnly = true)
    public Page<PacienteResponse> buscarPorNome(String nome, Pageable pageable) {
        return pacienteRepository.findByNomeContainingIgnoreCase(nome, pageable)
                .map(pacienteMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<PacienteResponse> listarPacientes(Pageable pageable) {
        return pacienteRepository.findAll(pageable)
                .map(pacienteMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<PacienteResponse> listarPacientesAtivos(Pageable pageable) {
        return pacienteRepository.findAllByAtivoTrue(pageable)
                .map(pacienteMapper::toResponseDTO);
    }

    @Transactional
    public void inativar(Long id) {
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PACIENTE_NAO_ENCONTRADO, HttpStatus.NOT_FOUND));
        if (!paciente.getAtivo()) {
            throw new BusinessException(ErrorCode.PACIENTE_JA_INATIVO, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        pacienteRepository.deactivate(id);
    }

    @Transactional
    public PacienteResponse atualizar(Long id, PacienteUpdate dto) {
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PACIENTE_NAO_ENCONTRADO, HttpStatus.NOT_FOUND));

        if (!paciente.getAtivo()) {
            throw new BusinessException(ErrorCode.PACIENTE_INATIVO, HttpStatus.UNPROCESSABLE_ENTITY);
        }


        pacienteMapper.updateEntityFromDTO(dto, paciente);
        return pacienteMapper.toResponseDTO(pacienteRepository.save(paciente));
    }
}



