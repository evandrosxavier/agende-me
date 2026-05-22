package br.com.agendeme.notificacoes.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificacoes_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificacaoLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long consultaId;

    @Column(nullable = false)
    private String tipoEvento;

    @Column(nullable = false)
    private String destinatario;

    @Column(nullable = false)
    private String pacienteNome;

    @Column(nullable = false)
    private String assunto;

    @Column(length = 30)
    private String statusConsulta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StatusEnvio status;

    @Column(columnDefinition = "TEXT")
    private String mensagemErro;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime dataEnvio;
}

