package br.com.agendeme.notificacoes.service;

import br.com.agendeme.notificacoes.dto.NotificacaoLogResponseDTO;
import br.com.agendeme.notificacoes.model.NotificacaoLog;
import br.com.agendeme.notificacoes.model.StatusEnvio;
import br.com.agendeme.notificacoes.repository.NotificacaoLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificacaoLogService {

    private final NotificacaoLogRepository repository;

    public void registrarSucesso(Long consultaId, String tipoEvento,
                                  String destinatario, String pacienteNome, String assunto) {
        salvar(consultaId, tipoEvento, destinatario, pacienteNome, assunto, StatusEnvio.ENVIADO, null);
    }

    public void registrarFalha(Long consultaId, String tipoEvento,
                                String destinatario, String pacienteNome, String assunto,
                                String mensagemErro) {
        salvar(consultaId, tipoEvento, destinatario, pacienteNome, assunto, StatusEnvio.FALHA, mensagemErro);
    }

    public List<NotificacaoLogResponseDTO> listarTodos() {
        return repository.findAll().stream()
                .map(NotificacaoLogResponseDTO::fromEntity)
                .toList();
    }

    public List<NotificacaoLogResponseDTO> buscarPorConsultaId(Long consultaId) {
        return repository.findByConsultaId(consultaId).stream()
                .map(NotificacaoLogResponseDTO::fromEntity)
                .toList();
    }

    public List<NotificacaoLogResponseDTO> buscarPorStatus(StatusEnvio status) {
        return repository.findByStatus(status).stream()
                .map(NotificacaoLogResponseDTO::fromEntity)
                .toList();
    }

    public List<NotificacaoLogResponseDTO> buscarPorDestinatario(String destinatario) {
        return repository.findByDestinatario(destinatario).stream()
                .map(NotificacaoLogResponseDTO::fromEntity)
                .toList();
    }

    private void salvar(Long consultaId, String tipoEvento, String destinatario,
                        String pacienteNome, String assunto, StatusEnvio status, String mensagemErro) {
        try {
            NotificacaoLog notificacaoLog = NotificacaoLog.builder()
                    .consultaId(consultaId)
                    .tipoEvento(tipoEvento)
                    .destinatario(destinatario)
                    .pacienteNome(pacienteNome)
                    .assunto(assunto)
                    .status(status)
                    .mensagemErro(mensagemErro)
                    .build();
            repository.save(notificacaoLog);
        } catch (Exception ex) {
            log.error("Erro ao persistir log de notificacao: {}", ex.getMessage());
        }
    }
}

