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
@Table(name = "medicos", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"crm", "crmUf"})
})
@DiscriminatorValue("MEDICO")
public class Medico extends Usuario {

    @Column(nullable = false, length = 15)
    private String crm;

    @Column(nullable = false, length = 2)
    private String crmUf;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private Especialidade especialidade;
}
