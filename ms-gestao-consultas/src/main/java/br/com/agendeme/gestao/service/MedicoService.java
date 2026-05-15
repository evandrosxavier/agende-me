package br.com.agendeme.gestao.service;

import br.com.agendeme.gestao.dto.medico.MedicoRequest;
import br.com.agendeme.gestao.dto.medico.MedicoResponse;
import br.com.agendeme.gestao.dto.medico.MedicoUpdate;
import br.com.agendeme.gestao.mapper.MedicoMapper;
import br.com.agendeme.gestao.model.domain.Especialidade;
import br.com.agendeme.gestao.model.domain.Medico;
import br.com.agendeme.gestao.model.domain.Role;
import br.com.agendeme.gestao.repository.MedicoRepository;
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
public class MedicoService {

    private final MedicoRepository medicoRepository;
    private final UsuarioRepository usuarioRepository;
    private final MedicoMapper medicoMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public MedicoResponse cadastrar(MedicoRequest dto) {
        if (medicoRepository.findByCrm(dto.crm()).isPresent()) {
            throw new BusinessException(ErrorCode.CRM_JA_CADASTRADO, HttpStatus.CONFLICT);
        }
        if (usuarioRepository.findByLogin(dto.login()).isPresent()) {
            throw new BusinessException(ErrorCode.LOGIN_ALREADY_EXISTS, HttpStatus.CONFLICT);
        }
        if (usuarioRepository.findByEmail(dto.email()).isPresent()) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS, HttpStatus.CONFLICT);
        }

        Medico medico = medicoMapper.toEntity(dto);
        medico.setSenha(passwordEncoder.encode(dto.senha()));
        medico.setRole(Role.MEDICO);
        medico.setAtivo(true);
        return medicoMapper.toResponseDTO(medicoRepository.save(medico));
    }

    @Transactional(readOnly = true)
    public MedicoResponse buscarPorCrm(String crm) {
        return medicoMapper.toResponseDTO(
                medicoRepository.findByCrm(crm)
                        .orElseThrow(() -> new BusinessException(ErrorCode.MEDICO_NAO_ENCONTRADO, HttpStatus.NOT_FOUND))
        );
    }

    @Transactional(readOnly = true)
    public Page<MedicoResponse> buscarPorNome(String nome, Pageable pageable) {
        return medicoRepository.findByNomeContainingIgnoreCase(nome, pageable)
                .map(medicoMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<MedicoResponse> buscarPorEspecialidade(Especialidade especialidade, Pageable pageable) {
        return medicoRepository.findByEspecialidade(especialidade, pageable)
                .map(medicoMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<MedicoResponse> listarMedicos(Pageable pageable) {
        return medicoRepository.findAll(pageable)
                .map(medicoMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<MedicoResponse> listarMedicosAtivos(Pageable pageable) {
        return medicoRepository.findAllByAtivoTrue(pageable)
                .map(medicoMapper::toResponseDTO);
    }

    @Transactional
    public void inativar(Long id) {
        Medico medico = medicoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEDICO_NAO_ENCONTRADO, HttpStatus.NOT_FOUND));
        if (!medico.getAtivo()) {
            throw new BusinessException(ErrorCode.MEDICO_JA_INATIVO, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        medicoRepository.deactivate(id);
    }

    @Transactional
    public MedicoResponse atualizar(Long id, MedicoUpdate dto) {
        Medico medico = medicoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEDICO_NAO_ENCONTRADO, HttpStatus.NOT_FOUND));
        if (!medico.getAtivo()) {
            throw new BusinessException(ErrorCode.MEDICO_INATIVO, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        medicoMapper.updateEntityFromDTO(dto, medico);
        return medicoMapper.toResponseDTO(medicoRepository.save(medico));
    }
}
