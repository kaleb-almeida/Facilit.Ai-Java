package com.facilitai.conexao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.facilitai.dto.MensagemConversa;
import com.facilitai.dto.TituloConversa;

public class MensagensDAO {
	public boolean salvarMensagem(Mensagens mensagem) {
		String sql = "INSERT INTO mensagens(usuario_id, assistente_tipo, conversa_uuid, titulo, remetente, texto) VALUES (?, ?, ?, ?, ?, ?)";

		Connection conn = Conexao.getConnection();
		if (conn == null) {
			return false;
		}

		try (Connection conexao = conn;
				PreparedStatement prepare = conexao.prepareStatement(sql)) {

			prepare.setInt(1, mensagem.getUsuarioId());
			prepare.setString(2, mensagem.getAssistenteTipo());
			prepare.setString(3, mensagem.getConversaUuid());
			prepare.setString(4, mensagem.getTitulo());
			prepare.setString(5, mensagem.getRemetente());
			prepare.setString(6, mensagem.getTexto());
			return prepare.executeUpdate() > 0;

		} catch (SQLException erro) {
			erro.printStackTrace();
			return false;
		}
	}

	public List<TituloConversa> pegarTitulo(Mensagens mensagem) {
		String sql = "SELECT conversa_uuid, titulo, assistente_tipo, MAX(criado_em) AS ultima_mensagem FROM mensagens WHERE usuario_id = ? GROUP BY conversa_uuid, titulo, assistente_tipo ORDER BY ultima_mensagem DESC;";

		Connection conn = Conexao.getConnection();
		if (conn == null) {
			throw new IllegalStateException("Não foi possível conectar ao banco de dados.");
		}

		try (Connection conexao = conn;
				PreparedStatement prepare = conexao.prepareStatement(sql)) {

			prepare.setInt(1, mensagem.getUsuarioId());
			List<TituloConversa> titulos = new ArrayList<>();

			try (ResultSet resultado = prepare.executeQuery()) {
				while (resultado.next()) {
					titulos.add(new TituloConversa(
							resultado.getString("conversa_uuid"),
							resultado.getString("titulo"),
							resultado.getString("assistente_tipo"),
							resultado.getString("ultima_mensagem")));
				}
			}
			return titulos;

		} catch (SQLException erro) {
			throw new RuntimeException("Erro ao buscar os títulos das conversas.", erro);
		}
	}

	public List<MensagemConversa> pegarConversa(Mensagens mensagem) {
		String sql = "SELECT usuario_id, assistente_tipo, conversa_uuid, titulo, remetente, texto, criado_em FROM mensagens WHERE conversa_uuid = ? AND usuario_id = ? AND assistente_tipo = ? ORDER BY criado_em ASC;";

		Connection conn = Conexao.getConnection();
		if (conn == null) {
			throw new IllegalStateException("Não foi possível conectar ao banco de dados.");
		}

		try (Connection conexao = conn;
				PreparedStatement prepare = conexao.prepareStatement(sql)) {

			prepare.setString(1, mensagem.getConversaUuid());
			prepare.setInt(2, mensagem.getUsuarioId());
			prepare.setString(3, mensagem.getAssistenteTipo());
			List<MensagemConversa> conversa = new ArrayList<>();

			try (ResultSet resultado = prepare.executeQuery()) {
				while (resultado.next()) {
					conversa.add(new MensagemConversa(
							resultado.getString("remetente"),
							resultado.getString("texto"),
							resultado.getString("criado_em")));
				}
			}
			return conversa;

		} catch (SQLException erro) {
			throw new RuntimeException("Erro ao buscar a conversa.", erro);
		}
	}

	public boolean alterarTitulo(Mensagens mensagem) {
		String sql = "UPDATE mensagens SET titulo = ? WHERE usuario_id = ? AND conversa_uuid = ? AND assistente_tipo = ?";

		Connection conn = Conexao.getConnection();
		if (conn == null) {
			return false;
		}

		try (Connection conexao = conn;
				PreparedStatement prepare = conexao.prepareStatement(sql)) {

			prepare.setString(1, mensagem.getTitulo());
			prepare.setInt(2, mensagem.getUsuarioId());
			prepare.setString(3, mensagem.getConversaUuid());
			prepare.setString(4, mensagem.getAssistenteTipo());
			return prepare.executeUpdate() > 0;

		} catch (SQLException erro) {
			erro.printStackTrace();
			return false;
		}
	}

	public boolean excluirConversa(Mensagens mensagem) {
		String sql = "DELETE FROM mensagens WHERE usuario_id = ? AND conversa_uuid = ? AND assistente_tipo = ?";

		Connection conn = Conexao.getConnection();
		if (conn == null) {
			return false;
		}

		try (Connection conexao = conn;
				PreparedStatement prepare = conexao.prepareStatement(sql)) {

			prepare.setInt(1, mensagem.getUsuarioId());
			prepare.setString(2, mensagem.getConversaUuid());
			prepare.setString(3, mensagem.getAssistenteTipo());
			return prepare.executeUpdate() > 0;

		} catch (SQLException erro) {
			erro.printStackTrace();
			return false;
		}
	}
}
