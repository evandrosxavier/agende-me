package br.com.agendeme.gestao.repository;

import br.com.agendeme.gestao.model.domain.Enfermeiro;
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
public interface EnfermeiroRepository extends JpaRepository<Enfermeiro, Long> {

    Optional<Enfermeiro> findByCre(String cre);

    Page<Enfermeiro> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    Page<Enfermeiro> findAllByAtivoTrue(Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE Enfermeiro e SET e.ativo = false WHERE e.id = :id")
    void deactivate(@Param("id") Long id);
}

