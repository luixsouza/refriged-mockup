package com.refriged.log_generator_mockup.controller;

import com.refriged.log_generator_mockup.model.SistemaRefrigeracao;
import com.refriged.log_generator_mockup.service.RabbitMQProducerService;
import com.refriged.log_generator_mockup.service.RefrigeracaoDataService;
import com.refriged.log_generator_mockup.utils.enums.StatusSistema;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/refrigeracao")
@RequiredArgsConstructor
@Validated
@CrossOrigin(origins = "*")
public class RefrigeracaoController {

    private final RefrigeracaoDataService refrigeracaoDataService;
    private final RabbitMQProducerService rabbitMQProducerService;

    @GetMapping("/sistema/{sistemaId}")
    public ResponseEntity<SistemaRefrigeracao> obterDadosSistema(
            @PathVariable @NotBlank(message = "ID do sistema é obrigatório") String sistemaId) {
        
        log.info("Requisição recebida para obter dados do sistema: {}", sistemaId);
        
        try {
            SistemaRefrigeracao sistema = refrigeracaoDataService.obterDadosSistema(sistemaId);
            
            Map<String, Object> detalhes = new HashMap<>();
            detalhes.put("endpoint", "obterDadosSistema");
            detalhes.put("temperatura", sistema.getTemperatura());
            detalhes.put("status", sistema.getStatus());
            rabbitMQProducerService.enviarLog(sistemaId, "INFO", 
                    "Dados do sistema obtidos via API", detalhes);
            
            log.info("Dados do sistema {} obtidos com sucesso. Status: {}, Temperatura: {}°C", 
                    sistemaId, sistema.getStatus(), sistema.getTemperatura());
            
            return ResponseEntity.ok(sistema);
            
        } catch (Exception e) {
            log.error("Erro ao obter dados do sistema {}: {}", sistemaId, e.getMessage(), e);
            
            Map<String, Object> detalhes = new HashMap<>();
            detalhes.put("endpoint", "obterDadosSistema");
            detalhes.put("erro", e.getMessage());
            rabbitMQProducerService.enviarLog(sistemaId, "ERROR", 
                    "Erro ao obter dados do sistema", detalhes);
            
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/sistemas")
    public ResponseEntity<List<SistemaRefrigeracao>> obterDadosMultiplosSistemas(
            @RequestParam(defaultValue = "5") 
            @Min(value = 1, message = "Quantidade mínima é 1")
            @Max(value = 50, message = "Quantidade máxima é 50") 
            int quantidade) {
        
        log.info("Requisição recebida para obter dados de {} sistemas", quantidade);
        
        try {
            List<SistemaRefrigeracao> sistemas = refrigeracaoDataService.gerarDadosMultiplosSistemas(quantidade);
            
            for (SistemaRefrigeracao sistema : sistemas) {
                rabbitMQProducerService.enviarDadosSistema(sistema);
                rabbitMQProducerService.verificarEEnviarAlertas(sistema);
            }
            
            Map<String, Object> detalhes = new HashMap<>();
            detalhes.put("endpoint", "obterDadosMultiplosSistemas");
            detalhes.put("quantidadeSistemas", sistemas.size());
            rabbitMQProducerService.enviarLog("MULTIPLOS_SISTEMAS", "INFO", 
                    "Dados de múltiplos sistemas gerados e enviados", detalhes);
            
            log.info("Dados de {} sistemas gerados com sucesso", sistemas.size());
            
            return ResponseEntity.ok(sistemas);
            
        } catch (Exception e) {
            log.error("Erro ao gerar dados de múltiplos sistemas: {}", e.getMessage(), e);
            
            Map<String, Object> detalhes = new HashMap<>();
            detalhes.put("endpoint", "obterDadosMultiplosSistemas");
            detalhes.put("erro", e.getMessage());
            rabbitMQProducerService.enviarLog("MULTIPLOS_SISTEMAS", "ERROR", 
                    "Erro ao gerar dados de múltiplos sistemas", detalhes);
            
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/sistema/{sistemaId}/gerar-dados")
    public ResponseEntity<SistemaRefrigeracao> gerarDadosELogs(
            @PathVariable @NotBlank(message = "ID do sistema é obrigatório") String sistemaId) {
        
        log.info("Iniciando geração de dados e logs para sistema: {}", sistemaId);
        
        try {
            SistemaRefrigeracao sistema = refrigeracaoDataService.gerarDadosSistema(sistemaId);
            
            rabbitMQProducerService.enviarDadosSistema(sistema);
            
            log.info("DADOS_SISTEMA_GERADOS - Sistema: {}, Timestamp: {}, Temperatura: {}°C, " +
                    "Pressão: {} Bar, Umidade: {}%, Status: {}, Consumo: {} kWh, RPM: {}",
                    sistema.getSistemaId(),
                    sistema.getTimestamp(),
                    String.format("%.2f", sistema.getTemperatura()),
                    String.format("%.2f", sistema.getPressao()),
                    String.format("%.2f", sistema.getUmidade()),
                    sistema.getStatus(),
                    String.format("%.2f", sistema.getConsumoEnergia()),
                    String.format("%.0f", sistema.getVelocidadeCompressor()));

            Map<String, Object> detalhes = new HashMap<>();
            detalhes.put("endpoint", "gerarDadosELogs");
            detalhes.put("temperatura", sistema.getTemperatura());
            detalhes.put("pressao", sistema.getPressao());
            detalhes.put("umidade", sistema.getUmidade());
            detalhes.put("status", sistema.getStatus());
            detalhes.put("consumoEnergia", sistema.getConsumoEnergia());
            detalhes.put("velocidadeCompressor", sistema.getVelocidadeCompressor());
            
            rabbitMQProducerService.enviarLog(sistemaId, "INFO", 
                    "Dados do sistema gerados com sucesso", detalhes);

            rabbitMQProducerService.verificarEEnviarAlertas(sistema);

            if (sistema.getStatus() != StatusSistema.OPERACIONAL) {
                log.warn("ALERTA_SISTEMA - Sistema {} em status não operacional: {} - {}",
                        sistemaId, sistema.getStatus(), sistema.getStatus().getDescricao());
                
                rabbitMQProducerService.enviarLog(sistemaId, "WARN", 
                        "Sistema em status não operacional: " + sistema.getStatus().getDescricao(), detalhes);
            }

            if (sistema.getTemperatura() > 0 || sistema.getTemperatura() < -30) {
                log.error("TEMPERATURA_CRITICA - Sistema {} com temperatura fora do range: {}°C",
                        sistemaId, String.format("%.2f", sistema.getTemperatura()));
                
                rabbitMQProducerService.enviarLog(sistemaId, "ERROR", 
                        "Temperatura crítica detectada: " + String.format("%.2f°C", sistema.getTemperatura()), detalhes);
            }

            log.info("Dados e logs gerados com sucesso para sistema: {}", sistemaId);
            
            return ResponseEntity.ok(sistema);
            
        } catch (Exception e) {
            log.error("Erro ao gerar dados e logs para sistema {}: {}", sistemaId, e.getMessage(), e);
            
            Map<String, Object> detalhes = new HashMap<>();
            detalhes.put("endpoint", "gerarDadosELogs");
            detalhes.put("erro", e.getMessage());
            rabbitMQProducerService.enviarLog(sistemaId, "ERROR", 
                    "Erro ao gerar dados e logs do sistema", detalhes);
            
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/sistema/{sistemaId}/enviar-rabbitmq")
    public ResponseEntity<Map<String, Object>> enviarDadosParaRabbitMQ(
            @PathVariable @NotBlank(message = "ID do sistema é obrigatório") String sistemaId) {
        
        log.info("Enviando dados do sistema {} para RabbitMQ", sistemaId);
        
        try {
            SistemaRefrigeracao sistema = refrigeracaoDataService.gerarDadosSistema(sistemaId);
            
            rabbitMQProducerService.enviarDadosSistema(sistema);
            
            rabbitMQProducerService.verificarEEnviarAlertas(sistema);
            
            rabbitMQProducerService.enviarLogSimples(sistemaId, "INFO", 
                    "Dados enviados para RabbitMQ com sucesso");
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "sucesso");
            response.put("sistemaId", sistemaId);
            response.put("timestamp", sistema.getTimestamp());
            response.put("mensagem", "Dados enviados para RabbitMQ com sucesso");
            
            log.info("Dados do sistema {} enviados para RabbitMQ com sucesso", sistemaId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erro ao enviar dados do sistema {} para RabbitMQ: {}", sistemaId, e.getMessage(), e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "erro");
            response.put("sistemaId", sistemaId);
            response.put("mensagem", "Erro ao enviar dados para RabbitMQ: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        log.debug("Health check solicitado");
        return ResponseEntity.ok("Microserviço de Refrigeração Industrial - Status: OK");
    }
}

