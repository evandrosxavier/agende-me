package br.com.agendeme.historico.repository;

import br.com.agendeme.historico.model.HistoricoConsulta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface HistoricoConsultaRepository extends JpaRepository<HistoricoConsulta, Long> {

        @Query("SELECT h FROM HistoricoConsulta h WHERE REPLACE(REPLACE(h.pacienteCpf, '.', ''), '-', '') = :cpf")
        Page<HistoricoConsulta> findByPacienteCpf(@Param("cpf") String cpf, Pageable pageable);

        @Query("SELECT h FROM HistoricoConsulta h WHERE REPLACE(REPLACE(h.pacienteCpf, '.', ''), '-', '') = :cpf AND h.dataHora > :dataHora")
        Page<HistoricoConsulta> findByPacienteCpfAndDataHoraAfter(@Param("cpf") String cpf, @Param("dataHora") LocalDateTime dataHora, Pageable pageable);

        Page<HistoricoConsulta> findByPacienteNomeContainingIgnoreCase(String nome, Pageable pageable);
        Page<HistoricoConsulta> findByMedicoCrm(String crm, Pageable pageable);
        Page<HistoricoConsulta> findByMedicoNomeContainingIgnoreCase(String nome, Pageable pageable);
        Page<HistoricoConsulta> findByStatus(String status, Pageable pageable);

        Optional<HistoricoConsulta> findByConsultaIdAndDataEvento(Long consultaId, LocalDateTime dataEvento);
}
