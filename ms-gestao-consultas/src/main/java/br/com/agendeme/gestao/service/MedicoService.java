package br.com.agendeme.gestao.service;

import br.com.agendeme.gestao.dto.medico.MedicoReativacaoRequest;
import br.com.agendeme.gestao.dto.medico.MedicoRequest;
import br.com.agendeme.gestao.dto.medico.MedicoResponse;
import br.com.agendeme.gestao.dto.medico.MedicoUpdate;
import br.com.agendeme.gestao.mapper.EnderecoMapper;
import br.com.agendeme.gestao.mapper.MedicoMapper;
import br.com.agendeme.gestao.model.enums.Especialidade;
import br.com.agendeme.gestao.model.domain.Medico;
import br.com.agendeme.gestao.model.enums.Role;
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
    private final EnderecoMapper enderecoMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public MedicoResponse cadastrar(MedicoRequest dto) {
        medicoRepository.findByCrm(dto.crm()).ifPresent(m -> {
            if (m.getAtivo()) {
                throw new BusinessException(ErrorCode.CRM_JA_CADASTRADO, HttpStatus.CONFLICT);
            }
            throw new BusinessException(ErrorCode.MEDICO_CADASTRO_INATIVO, HttpStatus.UNPROCESSABLE_ENTITY);
        });
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

    @Transactional
    public MedicoResponse reativar(String crm, MedicoReativacaoRequest dto) {
        Medico medico = medicoRepository.findByCrm(crm)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEDICO_NAO_ENCONTRADO, HttpStatus.NOT_FOUND));

        if (medico.getAtivo()) {
            throw new BusinessException(ErrorCode.MEDICO_JA_ATIVO, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        if (dto.email() != null) {
            usuarioRepository.findByEmail(dto.email())
                    .filter(u -> !u.getId().equals(medico.getId()))
                    .ifPresent(u -> { throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS, HttpStatus.CONFLICT); });
            medico.setEmail(dto.email());
        }
        if (dto.ddd() != null) medico.setDdd(dto.ddd());
        if (dto.telefone() != null) medico.setTelefone(dto.telefone());
        if (dto.endereco() != null) medico.setEndereco(enderecoMapper.toEntity(dto.endereco()));

        medico.setSenha(passwordEncoder.encode(dto.senha()));
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
    public void inativar(String crm) {
        Medico medico = medicoRepository.findByCrm(crm)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEDICO_NAO_ENCONTRADO, HttpStatus.NOT_FOUND));
        if (!medico.getAtivo()) {
            throw new BusinessException(ErrorCode.MEDICO_JA_INATIVO, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        medicoRepository.deactivate(medico.getId());
    }

    @Transactional
    public MedicoResponse atualizar(String crm, MedicoUpdate dto) {
        Medico medico = medicoRepository.findByCrm(crm)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEDICO_NAO_ENCONTRADO, HttpStatus.NOT_FOUND));
        if (!medico.getAtivo()) {
            throw new BusinessException(ErrorCode.MEDICO_INATIVO, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        medicoMapper.updateEntityFromDTO(dto, medico);
        return medicoMapper.toResponseDTO(medicoRepository.save(medico));
    }
}
