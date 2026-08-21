package com.facilitai.dto;

public class IaResponse {
    private final boolean sucesso;
    private final String mensagem;
    private final Object dados;

    public IaResponse(boolean sucesso, String mensagem, Object dados) {
        this.sucesso = sucesso;
        this.mensagem = mensagem;
        this.dados = dados;
    }

    public boolean isSucesso() {
        return sucesso;
    }

    public String getMensagem() {
        return mensagem;
    }

    public Object getDados() {
        return dados;
    }
}
