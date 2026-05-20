package br.com.agendeme.gestao.repository;

import br.com.agendeme.gestao.model.domain.Paciente;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    Optional<Paciente> findByCpf(String cpf);

    Page<Paciente> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    Page<Paciente> findAllByAtivoTrue(Pageable pageable);

    Optional<Paciente> findByLogin(String login);

    @Modifying
    @Transactional
    @Query("UPDATE Paciente p SET p.ativo = false WHERE p.id = :id")
    void deactivate(@Param("id") Long id);
}

