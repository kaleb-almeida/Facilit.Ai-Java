package com.facilitai.dto;

public class MensagemConversa {
    private final String remetente;
    private final String texto;
    private final String criado_em;

    public MensagemConversa(String remetente, String texto, String criadoEm) {
        this.remetente = remetente;
        this.texto = texto;
        this.criado_em = criadoEm;
    }
}
