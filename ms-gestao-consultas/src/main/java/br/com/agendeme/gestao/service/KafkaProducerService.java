package br.com.agendeme.gestao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void enviarNotificacao(String topico, Object mensagem) {
        try {
            kafkaTemplate.send(topico, mensagem);
            log.info("Mensagem enviada para o tópico {}", topico);
        } catch (Exception e) {
            log.error("Falha ao enviar mensagem para o tópico {}: {}", topico, e.getMessage());
        }
    }
}