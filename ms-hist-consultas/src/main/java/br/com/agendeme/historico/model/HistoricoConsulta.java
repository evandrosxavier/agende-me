package br.com.agendeme.historico.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "historico_consultas", uniqueConstraints = {
        @UniqueConstraint(name = "uk_consulta_evento", columnNames = {"consulta_id", "data_evento"})
})
public class HistoricoConsulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long consultaId;

    @Column(nullable = false, length = 100)
    private String pacienteNome;

    @Column(nullable = false, length = 14)
    private String pacienteCpf;

    @Column(nullable = false, length = 100)
    private String pacienteEmail;

    @Column(nullable = false, length = 100)
    private String medicoNome;

    @Column(nullable = false, length = 15)
    private String medicoCrm;

    @Column(nullable = false, length = 40)
    private String especialidade;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false)
    private LocalDateTime dataEvento;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime dataDoRegistro ;
}
