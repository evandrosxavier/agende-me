package br.com.agendeme.notificacoes.service;

import br.com.agendeme.notificacoes.dto.ConsultaNotificacaoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificacaoEmailService {

    private final JavaMailSender mailSender;
    private final NotificacaoLogService logService;

    @Value("${spring.mail.username}")
    private String remetente;

    private static final DateTimeFormatter FORMATTER_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATTER_HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FORMATTER_ISO  = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public void enviarConsultaAgendada(ConsultaNotificacaoDTO dto) {
        String assunto = "Consulta Agendada - Agende-me";
        enviar(dto, "AGENDADA", assunto, montarCorpoAgendada(dto));
    }

    public void enviarConsultaAgendadaAtualizada(ConsultaNotificacaoDTO dto) {
        String assunto = "Consulta Atualizada - Agende-me";
        enviar(dto, "ATUALIZADA", assunto, montarCorpoAtualizada(dto));
    }

    public void enviarConsultaRealizada(ConsultaNotificacaoDTO dto) {
        String assunto = "Consulta Realizada - Agende-me";
        enviar(dto, "REALIZADA", assunto, montarCorpoRealizada(dto));
    }

    public void enviarConsultaCancelada(ConsultaNotificacaoDTO dto) {
        String assunto = "Consulta Cancelada - Agende-me";
        enviar(dto, "CANCELADA", assunto, montarCorpoCancelada(dto));
    }


    private String montarCorpoAgendada(ConsultaNotificacaoDTO dto) {
        LocalDateTime dataHora = parseDataHora(dto.dataHora());
        return String.format("""
                Olá %s,

                Confirmamos sua consulta para o dia %s às %s
                com Dr(a). %s - %s.

                Caso precise reagendar ou cancelar, por favor entre em contato conosco.

                Atenciosamente,
                Equipe Agende-me
                """,
                dto.pacienteNome(),
                dataHora.format(FORMATTER_DATA),
                dataHora.format(FORMATTER_HORA),
                dto.medicoNome(),
                formatarEspecialidade(dto.especialidade()));
    }

    private String montarCorpoAtualizada(ConsultaNotificacaoDTO dto) {
        LocalDateTime dataHora = parseDataHora(dto.dataHora());
        return String.format("""
                Olá %s,

                Sua consulta foi atualizada. Confira os novos dados:

                Data:    %s às %s
                Médico:  Dr(a). %s
                Área:    %s

                Em caso de dúvidas, entre em contato conosco.

                Atenciosamente,
                Equipe Agende-me
                """,
                dto.pacienteNome(),
                dataHora.format(FORMATTER_DATA),
                dataHora.format(FORMATTER_HORA),
                dto.medicoNome(),
                formatarEspecialidade(dto.especialidade()));
    }

    private String montarCorpoRealizada(ConsultaNotificacaoDTO dto) {
        LocalDateTime dataHora = parseDataHora(dto.dataHora());
        return String.format("""
                Olá %s,

                Ficamos felizes em informar que sua consulta com Dr(a). %s - %s
                que estava agendada para o dia %s às %s foi realizada com sucesso.

                Que tal avaliar sua experiência? Sua opinião é muito importante para nós!

                Atenciosamente,
                Equipe Agende-me
                """,
                dto.pacienteNome(),
                dto.medicoNome(),
                formatarEspecialidade(dto.especialidade()),
                dataHora.format(FORMATTER_DATA),
                dataHora.format(FORMATTER_HORA));
    }




    private String montarCorpoCancelada(ConsultaNotificacaoDTO dto) {
        LocalDateTime dataHora = parseDataHora(dto.dataHora());
        return String.format("""
                Olá %s,

                Infelizmente, sua consulta com Dr(a). %s - %s
                que estava agendada para o dia %s às %s foi cancelada.

                Em caso de dúvidas, entre em contato conosco.

                Atenciosamente,
                Equipe Agende-me
                """,
                dto.pacienteNome(),
                dto.medicoNome(),
                formatarEspecialidade(dto.especialidade()),
                dataHora.format(FORMATTER_DATA),
                dataHora.format(FORMATTER_HORA));
    }


    private void enviar(ConsultaNotificacaoDTO dto, String tipoEvento, String assunto, String corpo) {
        try {
            SimpleMailMessage mensagem = new SimpleMailMessage();
            mensagem.setFrom(remetente);
            mensagem.setTo(dto.pacienteEmail());
            mensagem.setSubject(assunto);
            mensagem.setText(corpo);
            mailSender.send(mensagem);

            log.info("E-mail enviado | Tipo: {} | Para: {} | Consulta ID: {}",
                    tipoEvento, dto.pacienteEmail(), dto.consultaId());

            logService.registrarSucesso(dto.consultaId(), tipoEvento,
                    dto.pacienteEmail(), dto.pacienteNome(), assunto);

        } catch (Exception e) {
            Throwable causa = e.getCause() != null ? e.getCause() : e;
            String mensagemErro = causa.getMessage() != null ? causa.getMessage() : e.getMessage();

            log.error("Falha ao enviar e-mail | Tipo: {} | Para: {} | Consulta ID: {} | Erro: {}",
                    tipoEvento, dto.pacienteEmail(), dto.consultaId(), mensagemErro);
            log.debug("Stack trace completo:", e);

            logService.registrarFalha(dto.consultaId(), tipoEvento,
                    dto.pacienteEmail(), dto.pacienteNome(), assunto, mensagemErro);
        }
    }

    private LocalDateTime parseDataHora(String dataHora) {
        try {
            return LocalDateTime.parse(dataHora, FORMATTER_ISO);
        } catch (Exception e) {
            return LocalDateTime.parse(dataHora);
        }
    }

    private String formatarEspecialidade(String especialidade) {
        if (especialidade == null || especialidade.isBlank()) return "";
        return Arrays.stream(especialidade.split("_"))
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }

}
