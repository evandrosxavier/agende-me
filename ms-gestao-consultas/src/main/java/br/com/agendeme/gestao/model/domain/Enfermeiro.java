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
@Table(name = "enfermeiros", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"cre", "creUf"})
})
@DiscriminatorValue("ENFERMEIRO")
public class Enfermeiro extends Usuario {

    @Column(nullable = false, length = 15)
    private String cre;

    @Column(nullable = false, length = 2)
    private String creUf;
}
