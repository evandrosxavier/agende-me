package br.com.agendeme.gestao.mapper;

import br.com.agendeme.gestao.dto.enfermeiro.EnfermeiroRequest;
import br.com.agendeme.gestao.dto.enfermeiro.EnfermeiroResponse;
import br.com.agendeme.gestao.dto.enfermeiro.EnfermeiroUpdate;
import br.com.agendeme.gestao.model.domain.Enfermeiro;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = EnderecoMapper.class)
public interface EnfermeiroMapper extends IdadeCalculatorMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    @Mapping(target = "role", ignore = true)
    Enfermeiro toEntity(EnfermeiroRequest dto);

    @Mapping(target = "idade", expression = "java(calcularIdade(enfermeiro.getDataNascimento()))")
    EnfermeiroResponse toResponseDTO(Enfermeiro enfermeiro);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "login", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "senha", ignore = true)
    @Mapping(target = "dataNascimento", ignore = true)
    void updateEntityFromDTO(EnfermeiroUpdate dto, @MappingTarget Enfermeiro enfermeiro);
}

