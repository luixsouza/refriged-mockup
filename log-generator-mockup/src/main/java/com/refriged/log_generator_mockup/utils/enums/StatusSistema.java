package com.refriged.log_generator_mockup.utils.enums;

public enum StatusSistema {
        OPERACIONAL("Sistema funcionando normalmente"),
        MANUTENCAO("Sistema em manutenção"),
        ALERTA("Sistema com alertas"),
        CRITICO("Sistema em estado crítico"),
        PARADO("Sistema parado");

        private final String descricao;

        StatusSistema(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }