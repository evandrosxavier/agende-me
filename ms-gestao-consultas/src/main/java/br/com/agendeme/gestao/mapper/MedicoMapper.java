package br.com.agendeme.gestao.mapper;

import br.com.agendeme.gestao.dto.medico.MedicoRequest;
import br.com.agendeme.gestao.dto.medico.MedicoResponse;
import br.com.agendeme.gestao.dto.medico.MedicoUpdate;
import br.com.agendeme.gestao.model.domain.Medico;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = EnderecoMapper.class)
public interface MedicoMapper extends IdadeCalculatorMapper{


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    @Mapping(target = "role", ignore = true)
    Medico toEntity(MedicoRequest dto);

    @Mapping(target = "idade", expression = "java(calcularIdade(medico.getDataNascimento()))")
    MedicoResponse toResponseDTO(Medico medico);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "login", ignore = true)
    @Mapping(target = "especialidade", ignore = true)
    @Mapping(target = "dataNascimento", ignore = true)
    @Mapping(target = "senha", ignore = true)
    void updateEntityFromDTO(MedicoUpdate dto, @MappingTarget Medico medico);
}


