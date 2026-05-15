package br.com.agendeme.gestao.repository;

import br.com.agendeme.gestao.model.domain.Especialidade;
import br.com.agendeme.gestao.model.domain.Medico;
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
public interface MedicoRepository extends JpaRepository<Medico, Long> {

    Optional<Medico> findByCrm(String crm);

    Page<Medico> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    Page<Medico> findByEspecialidade(Especialidade especialidade, Pageable pageable);

    Page<Medico> findAllByAtivoTrue(Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE Medico m SET m.ativo = false WHERE m.id = :id")
    void deactivate(@Param("id") Long id);
}

