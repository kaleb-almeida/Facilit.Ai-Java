package com.facilitai.dto;

public class TituloConversa {
    private final String conversa_uuid;
    private final String titulo;
    private final String assistente_tipo;
    private final String ultima_mensagem;

    public TituloConversa(String conversaUuid, String titulo, String assistenteTipo, String ultimaMensagem) {
        this.conversa_uuid = conversaUuid;
        this.titulo = titulo;
        this.assistente_tipo = assistenteTipo;
        this.ultima_mensagem = ultimaMensagem;
    }
}
