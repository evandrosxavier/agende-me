package br.com.agendeme.gestao.model.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Entity
@Table(name = "pacientes")
@DiscriminatorValue("PACIENTE")
public class Paciente extends Usuario {

    @Column(nullable = false, unique = true, length = 14)
    private String cpf;
}
