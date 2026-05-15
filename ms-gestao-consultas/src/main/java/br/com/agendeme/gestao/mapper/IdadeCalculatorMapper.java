package br.com.agendeme.gestao.mapper;

import java.time.LocalDate;
import java.time.Period;

public interface IdadeCalculatorMapper {
    default Integer calcularIdade(LocalDate dataNascimento) {
        if (dataNascimento == null) return null;
        return Period.between(dataNascimento, LocalDate.now()).getYears();
    }
}