package br.com.agendeme.gestao.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public NewTopic consultaAgendadaTopic() {
        return TopicBuilder.name("consulta-agendada")
                .partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic consultaAgendamentoAtualizadoTopic() {
        return TopicBuilder.name("consulta-agendamento-atualizado")
                .partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic consultaCanceladaTopic() {
        return TopicBuilder.name("consulta-cancelada")
                .partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic consultaAtendimentoRegistradoTopic() {
        return TopicBuilder.name("consulta-atendimento-registrado")
                .partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic consultaAtendimentoAtualizadoTopic() {
        return TopicBuilder.name("consulta-atendimento-atualizado")
                .partitions(1).replicas(1).build();
    }
}
