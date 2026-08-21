package com.facilitai.service;

import java.util.List;

import com.facilitai.conexao.Mensagens;
import com.facilitai.conexao.MensagensDAO;
import com.facilitai.dto.MensagemConversa;
import com.facilitai.dto.TituloConversa;

public class IaService {
	private final MensagensDAO mensagensDAO;

	public IaService() {
		this.mensagensDAO = new MensagensDAO();
	}

	public boolean salvarMensagem(Mensagens mensagem) {
		mensagem.setAssistenteTipo(mensagem.getAssistenteTipo().trim());
		mensagem.setConversaUuid(mensagem.getConversaUuid().trim());
		mensagem.setTitulo(mensagem.getTitulo().trim());
		mensagem.setRemetente(mensagem.getRemetente().trim());
		mensagem.setTexto(mensagem.getTexto().trim());
		return mensagensDAO.salvarMensagem(mensagem);
	}

	public List<TituloConversa> pegarTitulos(Mensagens mensagem) {
		return mensagensDAO.pegarTitulo(mensagem);
	}

	public List<MensagemConversa> pegarConversa(Mensagens mensagem) {
		normalizarIdentificacao(mensagem);
		return mensagensDAO.pegarConversa(mensagem);
	}

	public boolean alterarTitulo(Mensagens mensagem) {
		normalizarIdentificacao(mensagem);
		mensagem.setTitulo(mensagem.getTitulo().trim());
		return mensagensDAO.alterarTitulo(mensagem);
	}

	public boolean excluirConversa(Mensagens mensagem) {
		normalizarIdentificacao(mensagem);
		return mensagensDAO.excluirConversa(mensagem);
	}

	private void normalizarIdentificacao(Mensagens mensagem) {
		mensagem.setAssistenteTipo(mensagem.getAssistenteTipo().trim());
		mensagem.setConversaUuid(mensagem.getConversaUuid().trim());
	}
}
