package br.com.agendeme.historico.excecoes;

public enum ErrorCode {

    HISTORICO_NAO_ENCONTRADO("Histórico não encontrado com o identificador informado."),
    CPF_NAO_ENCONTRADO("Nenhum histórico encontrado para o CPF informado."),
    CRM_NAO_ENCONTRADO("Nenhum histórico encontrado para o CRM informado."),
    STATUS_INVALIDO("Status informado é inválido. Valores aceitos: AGENDADA, REALIZADA, CANCELADA.");

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
