package br.com.agendeme.gestao.repository;

import br.com.agendeme.gestao.model.domain.ConsultaMedica;
import br.com.agendeme.gestao.model.domain.Especialidade;
import br.com.agendeme.gestao.model.domain.StatusConsulta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ConsultaMedicaRepository extends JpaRepository<ConsultaMedica, Long> {

    Page<ConsultaMedica> findByPacienteCpf(String cpf, Pageable pageable);

    Page<ConsultaMedica> findByMedicoCrm(String crm, Pageable pageable);

    Page<ConsultaMedica> findByDataHoraBetween(LocalDateTime inicio, LocalDateTime fim, Pageable pageable);

    Page<ConsultaMedica> findByStatus(StatusConsulta status, Pageable pageable);

    Page<ConsultaMedica> findByEspecialidade(Especialidade especialidade, Pageable pageable);
}

