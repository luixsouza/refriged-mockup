package com.refriged.log_generator_mockup.service;

import com.refriged.log_generator_mockup.config.RabbitMQConfig;
import com.refriged.log_generator_mockup.model.SistemaRefrigeracao;
import com.refriged.log_generator_mockup.utils.enums.StatusSistema;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMQProducerService {

    private final RabbitTemplate rabbitTemplate;

    public void enviarDadosSistema(SistemaRefrigeracao sistema) {
        try {
            log.info("Enviando dados do sistema {} para fila RabbitMQ", sistema.getSistemaId());
            
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_REFRIGERACAO,
                    RabbitMQConfig.ROUTING_KEY_DADOS,
                    sistema
            );
            
            log.info("Dados do sistema {} enviados com sucesso para fila: {}", 
                    sistema.getSistemaId(), RabbitMQConfig.FILA_DADOS_REFRIGERACAO);
            
        } catch (Exception e) {
            log.error("Erro ao enviar dados do sistema {} para RabbitMQ: {}", 
                    sistema.getSistemaId(), e.getMessage(), e);
            throw new RuntimeException("Falha ao enviar dados para RabbitMQ", e);
        }
    }

    public void enviarLog(String sistemaId, String nivel, String mensagem, Map<String, Object> detalhes) {
        try {
            Map<String, Object> logData = new HashMap<>();
            logData.put("sistemaId", sistemaId);
            logData.put("timestamp", LocalDateTime.now());
            logData.put("nivel", nivel);
            logData.put("mensagem", mensagem);
            logData.put("detalhes", detalhes);
            logData.put("origem", "refrigeracao-microservice");
            
            log.debug("Enviando log do sistema {} para fila RabbitMQ", sistemaId);
            
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_REFRIGERACAO,
                    RabbitMQConfig.ROUTING_KEY_LOGS,
                    logData
            );
            
            log.debug("Log do sistema {} enviado com sucesso para fila: {}", 
                    sistemaId, RabbitMQConfig.FILA_LOGS_SISTEMA);
            
        } catch (Exception e) {
            log.error("Erro ao enviar log do sistema {} para RabbitMQ: {}", 
                    sistemaId, e.getMessage(), e);
        }
    }

    public void enviarAlerta(SistemaRefrigeracao sistema, String tipoAlerta, String descricao, String severidade) {
        try {
            Map<String, Object> alertaData = new HashMap<>();
            alertaData.put("sistemaId", sistema.getSistemaId());
            alertaData.put("nomeSistema", sistema.getNomeSistema());
            alertaData.put("localizacao", sistema.getLocalizacao());
            alertaData.put("timestamp", LocalDateTime.now());
            alertaData.put("tipoAlerta", tipoAlerta);
            alertaData.put("descricao", descricao);
            alertaData.put("severidade", severidade);
            alertaData.put("temperatura", sistema.getTemperatura());
            alertaData.put("pressao", sistema.getPressao());
            alertaData.put("status", sistema.getStatus());
            alertaData.put("origem", "refrigeracao-microservice");
            
            log.warn("Enviando alerta {} do sistema {} para fila RabbitMQ", tipoAlerta, sistema.getSistemaId());
            
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_REFRIGERACAO,
                    RabbitMQConfig.ROUTING_KEY_ALERTAS,
                    alertaData
            );
            
            log.warn("Alerta {} do sistema {} enviado com sucesso para fila: {}", 
                    tipoAlerta, sistema.getSistemaId(), RabbitMQConfig.FILA_ALERTAS);
            
        } catch (Exception e) {
            log.error("Erro ao enviar alerta do sistema {} para RabbitMQ: {}", 
                    sistema.getSistemaId(), e.getMessage(), e);
            throw new RuntimeException("Falha ao enviar alerta para RabbitMQ", e);
        }
    }

    public void enviarLogSimples(String sistemaId, String nivel, String mensagem) {
        enviarLog(sistemaId, nivel, mensagem, new HashMap<>());
    }

    public void verificarEEnviarAlertas(SistemaRefrigeracao sistema) {
        if (sistema.getTemperatura() > 0 || sistema.getTemperatura() < -30) {
            enviarAlerta(sistema, "TEMPERATURA_CRITICA", 
                    String.format("Temperatura fora do range seguro: %.2f°C", sistema.getTemperatura()),
                    "HIGH");
        }

        if (sistema.getPressao() > 20 || sistema.getPressao() < 0.5) {
            enviarAlerta(sistema, "PRESSAO_CRITICA",
                    String.format("Pressão fora do range seguro: %.2f Bar", sistema.getPressao()),
                    "HIGH");
        }

        if (sistema.getStatus() == StatusSistema.CRITICO) {
            enviarAlerta(sistema, "STATUS_CRITICO",
                    "Sistema em estado crítico: " + sistema.getStatus().getDescricao(),
                    "CRITICAL");
        } else if (sistema.getStatus() == StatusSistema.PARADO) {
            enviarAlerta(sistema, "SISTEMA_PARADO",
                    "Sistema parado: " + sistema.getStatus().getDescricao(),
                    "HIGH");
        } else if (sistema.getStatus() == StatusSistema.ALERTA) {
            enviarAlerta(sistema, "SISTEMA_ALERTA",
                    "Sistema em alerta: " + sistema.getStatus().getDescricao(),
                    "MEDIUM");
        }

        if (sistema.getConsumoEnergia() > 40) {
            enviarAlerta(sistema, "CONSUMO_ELEVADO",
                    String.format("Consumo de energia elevado: %.2f kWh", sistema.getConsumoEnergia()),
                    "MEDIUM");
        }
    }
}

