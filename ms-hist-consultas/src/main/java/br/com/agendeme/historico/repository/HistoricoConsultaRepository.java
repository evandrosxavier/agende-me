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


        @Query("""
               SELECT h FROM HistoricoConsulta h
               WHERE REPLACE(REPLACE(h.pacienteCpf, '.', ''), '-', '') = :cpf
               AND h.dataEvento = (
                   SELECT MAX(h2.dataEvento) FROM HistoricoConsulta h2
                   WHERE h2.consultaId = h.consultaId
               )
               """)
        Page<HistoricoConsulta> findUltimoEstadoPorPacienteCpf(@Param("cpf") String cpf, Pageable pageable);

        @Query("""
               SELECT h FROM HistoricoConsulta h
               WHERE REPLACE(REPLACE(h.pacienteCpf, '.', ''), '-', '') = :cpf
               AND h.dataHora > :dataHora
               AND h.dataEvento = (
                   SELECT MAX(h2.dataEvento) FROM HistoricoConsulta h2
                   WHERE h2.consultaId = h.consultaId
               )
               """)
        Page<HistoricoConsulta> findUltimoEstadoPorPacienteCpfAposData(@Param("cpf") String cpf, @Param("dataHora") LocalDateTime dataHora, Pageable pageable);

        @Query("""
               SELECT h FROM HistoricoConsulta h
               WHERE LOWER(h.pacienteNome) LIKE LOWER(CONCAT('%', :nome, '%'))
               AND h.dataEvento = (
                   SELECT MAX(h2.dataEvento) FROM HistoricoConsulta h2
                   WHERE h2.consultaId = h.consultaId
               )
               """)
        Page<HistoricoConsulta> findUltimoEstadoPorPacienteNome(@Param("nome") String nome, Pageable pageable);

        @Query("""
               SELECT h FROM HistoricoConsulta h
               WHERE h.medicoCrm = :crm
               AND h.dataEvento = (
                   SELECT MAX(h2.dataEvento) FROM HistoricoConsulta h2
                   WHERE h2.consultaId = h.consultaId
               )
               """)
        Page<HistoricoConsulta> findUltimoEstadoPorMedicoCrm(@Param("crm") String crm, Pageable pageable);

        @Query("""
               SELECT h FROM HistoricoConsulta h
               WHERE LOWER(h.medicoNome) LIKE LOWER(CONCAT('%', :nome, '%'))
               AND h.dataEvento = (
                   SELECT MAX(h2.dataEvento) FROM HistoricoConsulta h2
                   WHERE h2.consultaId = h.consultaId
               )
               """)
        Page<HistoricoConsulta> findUltimoEstadoPorMedicoNome(@Param("nome") String nome, Pageable pageable);

        @Query("""
               SELECT h FROM HistoricoConsulta h
               WHERE h.status = :status
               AND h.dataEvento = (
                   SELECT MAX(h2.dataEvento) FROM HistoricoConsulta h2
                   WHERE h2.consultaId = h.consultaId
               )
               """)
        Page<HistoricoConsulta> findUltimoEstadoPorStatus(@Param("status") String status, Pageable pageable);

        @Query("""
               SELECT h FROM HistoricoConsulta h
               WHERE h.dataEvento BETWEEN :inicio AND :fim
               AND h.dataEvento = (
                   SELECT MAX(h2.dataEvento) FROM HistoricoConsulta h2
                   WHERE h2.consultaId = h.consultaId
               )
               ORDER BY h.dataEvento DESC
               """)
        Page<HistoricoConsulta> findUltimoEstadoPorPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim, Pageable pageable);

        @Query("SELECT h FROM HistoricoConsulta h WHERE REPLACE(REPLACE(h.pacienteCpf, '.', ''), '-', '') = :cpf ORDER BY h.consultaId, h.dataEvento ASC")
        Page<HistoricoConsulta> findAuditoriaPorCpf(@Param("cpf") String cpf, Pageable pageable);

        @Query("SELECT h FROM HistoricoConsulta h WHERE h.dataEvento BETWEEN :inicio AND :fim ORDER BY h.consultaId, h.dataEvento ASC")
        Page<HistoricoConsulta> findAuditoriaPorPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim, Pageable pageable);

        @Query("SELECT h FROM HistoricoConsulta h WHERE h.consultaId = :consultaId ORDER BY h.dataEvento ASC")
        Page<HistoricoConsulta> findAuditoriaPorConsultaId(@Param("consultaId") Long consultaId, Pageable pageable);


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
