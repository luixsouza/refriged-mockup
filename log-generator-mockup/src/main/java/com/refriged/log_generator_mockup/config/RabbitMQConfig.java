package com.refriged.log_generator_mockup.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RabbitMQConfig {

    public static final String FILA_DADOS_REFRIGERACAO = "refrigeracao.dados";
    public static final String FILA_LOGS_SISTEMA = "refrigeracao.logs";
    public static final String FILA_ALERTAS = "refrigeracao.alertas";
    public static final String FILA_DLQ = "refrigeracao.dlq";

    public static final String EXCHANGE_REFRIGERACAO = "refrigeracao.exchange";

    public static final String ROUTING_KEY_DADOS = "refrigeracao.dados";
    public static final String ROUTING_KEY_LOGS = "refrigeracao.logs";
    public static final String ROUTING_KEY_ALERTAS = "refrigeracao.alertas";

    @Value("${spring.rabbitmq.host:localhost}")
    private String rabbitmqHost;

    @Value("${spring.rabbitmq.port:5672}")
    private int rabbitmqPort;

    @Value("${spring.rabbitmq.username:guest}")
    private String rabbitmqUsername;

    @Value("${spring.rabbitmq.password:guest}")
    private String rabbitmqPassword;

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    public TopicExchange refrigeracaoExchange() {
        return ExchangeBuilder
                .topicExchange(EXCHANGE_REFRIGERACAO)
                .durable(true)
                .build();
    }

    @Bean
    public Queue filaDadosRefrigeracao() {
        return QueueBuilder
                .durable(FILA_DADOS_REFRIGERACAO)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", FILA_DLQ)
                .withArgument("x-message-ttl", 3600000)
                .build();
    }

    @Bean
    public Queue filaLogsSistema() {
        return QueueBuilder
                .durable(FILA_LOGS_SISTEMA)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", FILA_DLQ)
                .withArgument("x-message-ttl", 7200000)
                .build();
    }

    @Bean
    public Queue filaAlertas() {
        return QueueBuilder
                .durable(FILA_ALERTAS)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", FILA_DLQ)
                .withArgument("x-message-ttl", 1800000)
                .build();
    }

    @Bean
    public Queue filaDLQ() {
        return QueueBuilder
                .durable(FILA_DLQ)
                .build();
    }

    @Bean
    public Binding bindingDados() {
        return BindingBuilder
                .bind(filaDadosRefrigeracao())
                .to(refrigeracaoExchange())
                .with(ROUTING_KEY_DADOS);
    }

    @Bean
    public Binding bindingLogs() {
        return BindingBuilder
                .bind(filaLogsSistema())
                .to(refrigeracaoExchange())
                .with(ROUTING_KEY_LOGS);
    }

    @Bean
    public Binding bindingAlertas() {
        return BindingBuilder
                .bind(filaAlertas())
                .to(refrigeracaoExchange())
                .with(ROUTING_KEY_ALERTAS);
    }

    @Bean
    public String logRabbitMQConfig() {
        log.info("Configuração RabbitMQ inicializada:");
        log.info("Host: {}, Port: {}, Username: {}", rabbitmqHost, rabbitmqPort, rabbitmqUsername);
        log.info("Exchange: {}", EXCHANGE_REFRIGERACAO);
        log.info("Filas configuradas: {}, {}, {}, {}", 
                FILA_DADOS_REFRIGERACAO, FILA_LOGS_SISTEMA, FILA_ALERTAS, FILA_DLQ);
        return "RabbitMQ configurado com sucesso";
    }
}