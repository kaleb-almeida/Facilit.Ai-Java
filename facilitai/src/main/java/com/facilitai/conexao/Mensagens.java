package com.facilitai.conexao;

public class Mensagens {
	    private int id;
	    private int usuario_id;
	    private String assistente_tipo;
	    private String conversa_uuid;
	    private String titulo;
	    private String remetente;
	    private String texto;
	    private String ativo;
	    private String criado_em;
	    private String acao;

	    // Getters
	    public int getId() { return id; }
	    public int getUsuarioId() { return usuario_id; }
	    public String getAssistenteTipo() { return assistente_tipo; }
	    public String getConversaUuid() { return conversa_uuid; }
	    public String getTitulo() { return titulo; }
	    public String getRemetente() { return remetente; }
	    public String getTexto() { return texto; }
	    public String getAtivo() { return ativo; }
	    public String getCriadoEm() { return criado_em; }
	    public String getAcao() { return acao; }

	    // Setters
	    public void setId(int id) { this.id = id; }
	    public void setUsuarioId(int usuario_id) { this.usuario_id = usuario_id; }
	    public void setAssistenteTipo(String assistente_tipo) { this.assistente_tipo = assistente_tipo; }
	    public void setConversaUuid(String conversa_uuid) { this.conversa_uuid = conversa_uuid; }
	    public void setTitulo(String titulo) { this.titulo = titulo; }
	    public void setRemetente(String remetente) { this.remetente = remetente; }
	    public void setTexto(String texto) { this.texto = texto; }
	    public void setAtivo(String ativo) { this.ativo = ativo; }
	    public void setCriadoEm(String criado_em) { this.criado_em = criado_em; }
	    public void setAcao(String acao) { this.acao = acao; }
}
