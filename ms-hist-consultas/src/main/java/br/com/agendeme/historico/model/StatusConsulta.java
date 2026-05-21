package br.com.agendeme.historico.model;

public enum StatusConsulta {
    AGENDADA, REALIZADA, CANCELADA;

    public static boolean isValid(String value) {
        if (value == null) return false;
        for (StatusConsulta s : values()) {
            if (s.name().equalsIgnoreCase(value)) return true;
        }
        return false;
    }
}

