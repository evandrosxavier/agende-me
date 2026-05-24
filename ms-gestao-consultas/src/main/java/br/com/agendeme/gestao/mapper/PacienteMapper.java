package br.com.agendeme.gestao.mapper;

import br.com.agendeme.gestao.dto.paciente.PacienteRequest;
import br.com.agendeme.gestao.dto.paciente.PacienteResponse;
import br.com.agendeme.gestao.dto.paciente.PacienteUpdate;
import br.com.agendeme.gestao.model.domain.Paciente;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = EnderecoMapper.class)
public interface PacienteMapper extends IdadeCalculatorMapper{

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    @Mapping(target = "role", ignore = true)
    Paciente toEntity(PacienteRequest dto);


    @Mapping(target = "idade", expression = "java(calcularIdade(paciente.getDataNascimento()))")
    PacienteResponse toResponseDTO(Paciente paciente);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "login", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "senha", ignore = true)
    @Mapping(target = "dataNascimento", ignore = true)
    @Mapping(target = "cpf", ignore = true)
    @Mapping(target = "sexo", ignore = true)
    @Mapping(target = "nome", ignore = true)
    void updateEntityFromDTO(PacienteUpdate dto, @MappingTarget Paciente paciente);

}


