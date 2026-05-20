package br.com.agendeme.gestao.excecoes;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {


    // Autenticação / Credenciais
    EMAIL_ALREADY_EXISTS("O e-mail informado já está cadastrado."),
    LOGIN_ALREADY_EXISTS("O login informado já está em uso."),
    USER_NOT_FOUND("Usuário não encontrado com o identificador informado."),
    LOGIN_NOT_FOUND("O login informado não foi encontrado na base de dados."),
    INVALID_PASSWORD("A senha atual informada está incorreta."),
    INVALID_LOGIN_PASSWORD("Login ou senha inválidos. Tente novamente!"),

    // CPF
    CPF_JA_CADASTRADO("Já existe um paciente cadastrado com o CPF informado."),

    //Médico
    CRM_JA_CADASTRADO("Já existe um médico cadastrado com o CRM informado."),
    MEDICO_NAO_ENCONTRADO("Médico não encontrado com o identificador informado."),
    MEDICO_JA_INATIVO("O médico informado já está inativo."),
    MEDICO_INATIVO("O médico informado está inativo e não pode ser alterado."),

    // Paciente
    PACIENTE_NAO_ENCONTRADO("Paciente não encontrado com o identificador informado."),
    PACIENTE_JA_INATIVO("O paciente informado já está inativo."),
    PACIENTE_INATIVO("O paciente informado está inativo e não pode ser alterado."),

    // Enfermeiro
    CRE_JA_CADASTRADO("Já existe um enfermeiro cadastrado com o CRE informado."),
    ENFERMEIRO_NAO_ENCONTRADO("Enfermeiro não encontrado com o identificador informado."),
    ENFERMEIRO_JA_INATIVO("O enfermeiro informado já está inativo."),
    ENFERMEIRO_INATIVO("O enfermeiro informado está inativo e não pode ser alterado."),

    // Consulta
    PACIENTE_INATIVO_CONSULTA("O paciente informado está inativo. Não é possível seguir com o agendamento ou alteração da consulta."),
    MEDICO_INATIVO_CONSULTA("O médico informado está inativo. Não é possível seguir com o agendamento ou alteração da consulta."),
    CONSULTA_NAO_ENCONTRADA("Consulta não encontrada com o identificador informado."),
    CONSULTA_JA_CANCELADA("A consulta informada já está cancelada."),
    CONSULTA_NAO_PODE_SER_CANCELADA("A consulta não pode ser cancelada pois já foi realizada."),
    PERIODO_INVALIDO("A data de início não pode ser posterior à data de fim."),
    CONSULTA_NAO_PODE_SER_REGISTRADA("A consulta não pode ser registrada pois já foi cancelada ou realizada."),
    CONSULTA_NAO_ESTA_REALIZADA("A consulta não foi realizada e não pode ser atualizada.");

    private final String message;
}
