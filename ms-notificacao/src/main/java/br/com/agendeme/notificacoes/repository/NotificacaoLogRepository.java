package br.com.agendeme.notificacoes.repository;

import br.com.agendeme.notificacoes.model.NotificacaoLog;
import br.com.agendeme.notificacoes.model.StatusEnvio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacaoLogRepository extends JpaRepository<NotificacaoLog, Long> {

    List<NotificacaoLog> findByConsultaId(Long consultaId);

    List<NotificacaoLog> findByStatus(StatusEnvio status);

    List<NotificacaoLog> findByDestinatario(String destinatario);
}

