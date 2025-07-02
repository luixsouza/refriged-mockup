package com.refriged.log_generator_mockup.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.refriged.log_generator_mockup.utils.enums.StatusSistema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SistemaRefrigeracao {

    @NotBlank(message = "ID do sistema é obrigatório")
    private String sistemaId;

    @NotBlank(message = "Nome do sistema é obrigatório")
    private String nomeSistema;

    @NotBlank(message = "Localização é obrigatória")
    private String localizacao;

    @NotNull(message = "Timestamp é obrigatório")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    @NotNull(message = "Temperatura é obrigatória")
    @DecimalMin(value = "-50.0", message = "Temperatura mínima é -50°C")
    @DecimalMax(value = "50.0", message = "Temperatura máxima é 50°C")
    private Double temperatura;

    @NotNull(message = "Pressão é obrigatória")
    @DecimalMin(value = "0.0", message = "Pressão deve ser positiva")
    @DecimalMax(value = "100.0", message = "Pressão máxima é 100 Bar")
    private Double pressao;

    @NotNull(message = "Umidade é obrigatória")
    @DecimalMin(value = "0.0", message = "Umidade mínima é 0%")
    @DecimalMax(value = "100.0", message = "Umidade máxima é 100%")
    private Double umidade;

    @NotNull(message = "Status é obrigatório")
    private StatusSistema status;

    @NotNull(message = "Consumo de energia é obrigatório")
    @DecimalMin(value = "0.0", message = "Consumo deve ser positivo")
    private Double consumoEnergia;

    @NotNull(message = "Velocidade do compressor é obrigatória")
    @DecimalMin(value = "0.0", message = "Velocidade deve ser positiva")
    @DecimalMax(value = "10000.0", message = "Velocidade máxima é 10000 RPM")
    private Double velocidadeCompressor;

    private String observacoes;
}