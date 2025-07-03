package com.refriged.log_generator_mockup.service;

import com.refriged.log_generator_mockup.model.SistemaRefrigeracao;
import com.refriged.log_generator_mockup.utils.enums.StatusSistema;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

@Slf4j
@Service
public class RefrigeracaoDataService {

    private final Random random = new Random();
    
    private static final String[] NOMES_SISTEMAS = {
        "Motor Chiller de Imersão 1", "Motor Chiller de Imersão 2",
        "Motor Pré-Resfriador A", "Motor Túnel de Congelamento Rápido B",
        "Motor Câmara de Estocagem Congelados C1", "Motor Câmara de Estocagem Resfriados C2"
    };

    private static final String[] LOCALIZACOES = {
        "Casa de Máquinas - Painel 1", "Casa de Máquinas - Painel 2",
        "Casa de Máquinas - Painel 3", "Casa de Máquinas - Painel 4",
        "Casa de Máquinas - Painel 5", "Casa de Máquinas - Painel 6"
    };

    public SistemaRefrigeracao gerarDadosSistema(String sistemaId) {
        log.debug("Gerando dados mockados para sistema: {}", sistemaId);
        
        return SistemaRefrigeracao.builder()
                .sistemaId(sistemaId)
                .nomeSistema(obterNomeSistemaAleatorio())
                .localizacao(obterLocalizacaoAleatoria())
                .timestamp(LocalDateTime.now())
                .temperatura(gerarTemperatura())
                .pressao(gerarPressao())
                .umidade(gerarUmidade())
                .status(gerarStatus())
                .consumoEnergia(gerarConsumoEnergia())
                .velocidadeCompressor(gerarVelocidadeCompressor())
                .observacoes(gerarObservacoes())
                .build();
    }

    public List<SistemaRefrigeracao> gerarDadosMultiplosSistemas(int quantidade) {
        log.info("Gerando dados para {} sistemas", quantidade);
        
        return IntStream.range(1, quantidade + 1)
                .mapToObj(i -> gerarDadosSistema("SYS-" + String.format("%03d", i)))
                .toList();
    }

    public SistemaRefrigeracao obterDadosSistema(String sistemaId) {
        log.info("Obtendo dados para sistema: {}", sistemaId);
        return gerarDadosSistema(sistemaId);
    }

    private String obterNomeSistemaAleatorio() {
        return NOMES_SISTEMAS[random.nextInt(NOMES_SISTEMAS.length)];
    }

    private String obterLocalizacaoAleatoria() {
        return LOCALIZACOES[random.nextInt(LOCALIZACOES.length)];
    }

    private Double gerarTemperatura() {
        return -25.0 + (random.nextDouble() * 30.0);
    }

    private Double gerarPressao() {
        return 1.0 + (random.nextDouble() * 14.0);
    }

    private Double gerarUmidade() {
        return 40.0 + (random.nextDouble() * 50.0);
    }

    private StatusSistema gerarStatus() {
        StatusSistema[] status = StatusSistema.values();
        
        double probabilidade = random.nextDouble();
        if (probabilidade < 0.7) {
            return StatusSistema.OPERACIONAL;
        } else if (probabilidade < 0.85) {
            return StatusSistema.ALERTA;
        } else if (probabilidade < 0.95) {
            return StatusSistema.MANUTENCAO;
        } else if (probabilidade < 0.99) {
            return StatusSistema.CRITICO;
        } else {
            return StatusSistema.PARADO;
        }
    }

    private Double gerarConsumoEnergia() {
        return 5.0 + (random.nextDouble() * 45.0);
    }

    private Double gerarVelocidadeCompressor() {
        return 1000.0 + (random.nextDouble() * 2500.0);
    }

    private String gerarObservacoes() {
        String[] observacoes = {
            "Sistema funcionando dentro dos parâmetros normais",
            "Leve variação na temperatura detectada",
            "Manutenção preventiva agendada para próxima semana",
            "Compressor operando com eficiência ótima",
            "Monitoramento contínuo ativo",
            null
        };
        
        return observacoes[random.nextInt(observacoes.length)];
    }
}