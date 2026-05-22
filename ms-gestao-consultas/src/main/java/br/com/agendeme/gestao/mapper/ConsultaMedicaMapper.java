package br.com.agendeme.gestao.mapper;

import br.com.agendeme.gestao.dto.consulta.*;
import br.com.agendeme.gestao.dto.notificacao.ConsultaNotificacaoDTO;
import br.com.agendeme.gestao.model.domain.ConsultaMedica;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ConsultaMedicaMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "paciente", ignore = true)
    @Mapping(target = "medico", ignore = true)
    @Mapping(target = "especialidade", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataModificacao", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target= "diagnostico", ignore = true)
    @Mapping(target = "tratamentoProposto", ignore = true)
    @Mapping(target = "demaisObservacoes", ignore = true)
    ConsultaMedica toEntity(ConsultaRequest dto);

    @Mapping(source = "paciente.id",   target = "pacienteId")
    @Mapping(source = "paciente.nome", target = "pacienteNome")
    @Mapping(source = "medico.id",     target = "medicoId")
    @Mapping(source = "medico.nome",   target = "medicoNome")
    @Mapping(source = "medico.crm",    target = "medicoCrm")
    @Mapping(source = "medico.crmUf",  target = "medicoCrmUf")
    ConsultaAgendamentoResponse consultaAgendadaToResponseDTO(ConsultaMedica consulta);

    @Mapping(source = "paciente.id",   target = "pacienteId")
    @Mapping(source = "paciente.nome", target = "pacienteNome")
    @Mapping(source = "medico.id",     target = "medicoId")
    @Mapping(source = "medico.nome",   target = "medicoNome")
    @Mapping(source = "medico.crm",    target = "medicoCrm")
    @Mapping(source = "medico.crmUf",  target = "medicoCrmUf")
    ConsultaAtendimentoResponse consultaRealizadaToResponseDTO(ConsultaMedica consulta);


    @Mapping(source = "paciente.nome", target = "pacienteNome")
    @Mapping(source = "paciente.cpf", target = "pacienteCpf")
    @Mapping(source = "paciente.email", target = "pacienteEmail")
    @Mapping(source = "medico.nome", target = "medicoNome")
    @Mapping(source = "medico.crm", target = "medicoCrm")
    @Mapping(target = "consultaId", source = "id")
    @Mapping(target = "dataEvento", expression = "java(java.time.LocalDateTime.now().toString())")
    ConsultaNotificacaoDTO toNotificacaoDTO(ConsultaMedica consulta);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "paciente", ignore = true)
    @Mapping(target = "medico", ignore = true)
    @Mapping(target = "especialidade", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataModificacao", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target= "diagnostico", ignore = true)
    @Mapping(target = "tratamentoProposto", ignore = true)
    @Mapping(target = "demaisObservacoes", ignore = true)
    void updateEntityFromDTO(ConsultaAgendamentoUpdate dto, @MappingTarget ConsultaMedica consulta);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "paciente", ignore = true)
    @Mapping(target = "medico", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataModificacao", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "especialidade", ignore = true)
    @Mapping(target = "dataHora", ignore = true)

    void updateEntityFromDTO(ConsultaAtendimentoUpdate dto, @MappingTarget ConsultaMedica consulta);


}
