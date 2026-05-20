package br.com.agendeme.gestao.service;

import br.com.agendeme.gestao.dto.enfermeiro.EnfermeiroRequest;
import br.com.agendeme.gestao.dto.enfermeiro.EnfermeiroResponse;
import br.com.agendeme.gestao.dto.enfermeiro.EnfermeiroUpdate;
import br.com.agendeme.gestao.excecoes.BusinessException;
import br.com.agendeme.gestao.excecoes.ErrorCode;
import br.com.agendeme.gestao.mapper.EnfermeiroMapper;
import br.com.agendeme.gestao.model.domain.Enfermeiro;
import br.com.agendeme.gestao.model.enums.Role;
import br.com.agendeme.gestao.repository.EnfermeiroRepository;
import br.com.agendeme.gestao.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EnfermeiroService {

    private final EnfermeiroRepository enfermeiroRepository;
    private final UsuarioRepository usuarioRepository;
    private final EnfermeiroMapper enfermeiroMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public EnfermeiroResponse cadastrar(EnfermeiroRequest dto) {
        if (enfermeiroRepository.findByCre(dto.cre()).isPresent()) {
            throw new BusinessException(ErrorCode.CRE_JA_CADASTRADO, HttpStatus.CONFLICT);
        }
        if (usuarioRepository.findByLogin(dto.login()).isPresent()) {
            throw new BusinessException(ErrorCode.LOGIN_ALREADY_EXISTS, HttpStatus.CONFLICT);
        }
        if (usuarioRepository.findByEmail(dto.email()).isPresent()) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS, HttpStatus.CONFLICT);
        }

        Enfermeiro enfermeiro = enfermeiroMapper.toEntity(dto);
        enfermeiro.setSenha(passwordEncoder.encode(dto.senha()));
        enfermeiro.setRole(Role.ENFERMEIRO);
        enfermeiro.setAtivo(true);

        return enfermeiroMapper.toResponseDTO(enfermeiroRepository.save(enfermeiro));
    }

    @Transactional(readOnly = true)
    public EnfermeiroResponse buscarPorCre(String cre) {
        Enfermeiro enfermeiro = enfermeiroRepository.findByCre(cre)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENFERMEIRO_NAO_ENCONTRADO, HttpStatus.NOT_FOUND));
        return enfermeiroMapper.toResponseDTO(enfermeiro);
    }

    @Transactional(readOnly = true)
    public Page<EnfermeiroResponse> buscarPorNome(String nome, Pageable pageable) {
        return enfermeiroRepository.findByNomeContainingIgnoreCase(nome, pageable)
                .map(enfermeiroMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<EnfermeiroResponse> listarEnfermeiros(Pageable pageable) {
        return enfermeiroRepository.findAll(pageable)
                .map(enfermeiroMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<EnfermeiroResponse> listarEnfermeirosAtivos(Pageable pageable) {
        return enfermeiroRepository.findAllByAtivoTrue(pageable)
                .map(enfermeiroMapper::toResponseDTO);
    }

    @Transactional
    public void inativar(String cre) {
        Enfermeiro enfermeiro = enfermeiroRepository.findByCre(cre)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENFERMEIRO_NAO_ENCONTRADO, HttpStatus.NOT_FOUND));
        if (!enfermeiro.getAtivo()) {
            throw new BusinessException(ErrorCode.ENFERMEIRO_JA_INATIVO, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        enfermeiroRepository.deactivate(enfermeiro.getId());
    }

    @Transactional
    public EnfermeiroResponse atualizar(Long id, EnfermeiroUpdate dto) {
        Enfermeiro enfermeiro = enfermeiroRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENFERMEIRO_NAO_ENCONTRADO, HttpStatus.NOT_FOUND));
        if (!enfermeiro.getAtivo()) {
            throw new BusinessException(ErrorCode.ENFERMEIRO_INATIVO, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        enfermeiroMapper.updateEntityFromDTO(dto, enfermeiro);
        return enfermeiroMapper.toResponseDTO(enfermeiroRepository.save(enfermeiro));
    }
}
