package com.facilitai.login;

import com.facilitai.conexao.Conexao;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name = "authServer", urlPatterns = "/api/auth/*")
public class Server extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String JWT_SECRET = System.getenv("JWT_SECRET");
    private static final Gson gson = new Gson();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse res) {
        res.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        switch (rota(req)) {
            case "/register" -> registrar(req, res);
            case "/login" -> login(req, res);
            case "/delete" -> excluir(req, res);
            default -> resposta(res, 404, "{\"error\":\"Rota não encontrada.\"}");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if ("/me".equals(rota(req))) consultar(req, res);
        else resposta(res, 404, "{\"error\":\"Rota não encontrada.\"}");
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if ("/delete".equals(rota(req))) excluir(req, res);
        else resposta(res, 404, "{\"error\":\"Rota não encontrada.\"}");
    }

    private void registrar(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            JsonObject json = gson.fromJson(req.getReader(), JsonObject.class);
            String nome = valor(json, "nome").trim();
            String sobrenome = valor(json, "sobrenome").trim();
            String email = valor(json, "email").toLowerCase().trim();
            String senha = json.has("senha") ? valor(json, "senha") : valor(json, "password");
            if (nome.isEmpty() || sobrenome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
                resposta(res, 400, "{\"error\":\"Nome, sobrenome, e-mail e senha são obrigatórios.\"}");
                return;
            }
            try (Connection conn = Conexao.getConnection()) {
                try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM usuarios WHERE email = ?")) {
                    ps.setString(1, email);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            resposta(res, 400, "{\"error\":\"Este e-mail já está em uso.\"}");
                            return;
                        }
                    }
                }
                String sql = "INSERT INTO usuarios (nome, sobrenome, email, senha) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, nome);
                    ps.setString(2, sobrenome);
                    ps.setString(3, email);
                    ps.setString(4, BCrypt.hashpw(senha, BCrypt.gensalt(12)));
                    if (ps.executeUpdate() > 0) {
                        try (ResultSet rs = ps.getGeneratedKeys()) {
                            if (rs.next()) {
                                usuarioEToken(res, 201, rs.getInt(1), nome, sobrenome, email);
                                return;
                            }
                        }
                    }
                    resposta(res, 500, "{\"error\":\"Erro ao criar usuário.\"}");
                }
            }
        } catch (Exception e) {
            erro(res, e);
        }
    }

    private void login(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            JsonObject json = gson.fromJson(req.getReader(), JsonObject.class);
            String email = valor(json, "email").toLowerCase().trim();
            String senha = json.has("senha") ? valor(json, "senha") : valor(json, "password");
            if (email.isEmpty() || senha.isEmpty()) {
                resposta(res, 400, "{\"error\":\"E-mail e senha são obrigatórios.\"}");
                return;
            }
            String sql = "SELECT id, nome, sobrenome, email, senha FROM usuarios WHERE email = ?";
            try (Connection conn = Conexao.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, email);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && BCrypt.checkpw(senha, rs.getString("senha"))) {
                        int id = rs.getInt("id");
                        String nome = rs.getString("nome");
                        String sobrenome = rs.getString("sobrenome");
                        String emailSalvo = rs.getString("email");
                        try (PreparedStatement update = conn.prepareStatement(
                                "UPDATE usuarios SET ultimo_login = CURRENT_TIMESTAMP WHERE id = ?")) {
                            update.setInt(1, id);
                            update.executeUpdate();
                        }
                        usuarioEToken(res, 200, id, nome, sobrenome, emailSalvo);
                        return;
                    }
                }
            }
            resposta(res, 401, "{\"error\":\"Credenciais inválidas.\"}");
        } catch (Exception e) {
            erro(res, e);
        }
    }

    private void consultar(HttpServletRequest req, HttpServletResponse res) throws IOException {
        Integer id = idToken(req, res);
        if (id == null) return;
        try (Connection conn = Conexao.getConnection(); PreparedStatement ps = conn.prepareStatement(
                "SELECT id, nome, sobrenome, email FROM usuarios WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    resposta(res, 404, "{\"error\":\"Usuário não encontrado.\"}");
                    return;
                }
                JsonObject json = new JsonObject();
                json.add("user", usuario(rs.getInt("id"), rs.getString("nome"),
                        rs.getString("sobrenome"), rs.getString("email")));
                resposta(res, 200, json.toString());
            }
        } catch (Exception e) {
            erro(res, e);
        }
    }

    private void excluir(HttpServletRequest req, HttpServletResponse res) throws IOException {
        Integer id = idToken(req, res);
        if (id == null) return;
        try (Connection conn = Conexao.getConnection(); PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM usuarios WHERE id = ?")) {
            ps.setInt(1, id);
            if (ps.executeUpdate() > 0)
                resposta(res, 200, "{\"success\":true,\"message\":\"Conta excluída com sucesso.\"}");
            else resposta(res, 404, "{\"error\":\"Usuário não encontrado.\"}");
        } catch (Exception e) {
            erro(res, e);
        }
    }

    private Integer idToken(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String auth = req.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            resposta(res, 401, "{\"error\":\"Não autenticado.\"}");
            return null;
        }
        String payload = JWT.verifyAndGetPayload(auth.substring(7), JWT_SECRET);
        if (payload == null) {
            resposta(res, 401, "{\"error\":\"Token inválido.\"}");
            return null;
        }
        try {
            return gson.fromJson(payload, JsonObject.class).get("id").getAsInt();
        } catch (Exception e) {
            resposta(res, 401, "{\"error\":\"Token inválido.\"}");
            return null;
        }
    }

    private void usuarioEToken(HttpServletResponse res, int status, int id,
                               String nome, String sobrenome, String email) throws IOException {
        JsonObject payload = new JsonObject();
        payload.addProperty("id", id);
        payload.addProperty("email", email);
        JsonObject json = new JsonObject();
        json.add("user", usuario(id, nome, sobrenome, email));
        json.addProperty("token", JWT.sign(payload.toString(), JWT_SECRET));
        resposta(res, status, json.toString());
    }

    private JsonObject usuario(int id, String nome, String sobrenome, String email) {
        JsonObject user = new JsonObject();
        user.addProperty("id", id);
        user.addProperty("nome", nome);
        user.addProperty("sobrenome", sobrenome);
        user.addProperty("email", email);
        return user;
    }

    private String valor(JsonObject json, String chave) {
        return json != null && json.has(chave) && !json.get(chave).isJsonNull()
                ? json.get(chave).getAsString() : "";
    }

    private String rota(HttpServletRequest req) {
        return req.getPathInfo() == null ? "" : req.getPathInfo();
    }

    private void resposta(HttpServletResponse res, int status, String body) throws IOException {
        res.setStatus(status);
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write(body);
    }

    private void erro(HttpServletResponse res, Exception e) throws IOException {
        e.printStackTrace();
        resposta(res, 500, "{\"error\":\"Erro interno no servidor.\"}");
    }
}
