package acessos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import conex.DatabaseConnection;

public class ControleAcesso {

    /**
     * Verifica se um funcionário possui uma permissão específica.
     * 
     * @param matricula     A matrícula do funcionário.
     * @param nomePermissao O nome da coluna de permissão no banco de dados (ex:
     *                      "consultar_estoque").
     * @return true se o funcionário tiver a permissão, false caso contrário.
     */
    public boolean temPermissao(String matricula, String permissao) {
        String sql = "SELECT p." + permissao + " " +
                "FROM Funcionarios f " +
                "JOIN Cargos c ON f.id_cargo = c.id " +
                "JOIN Permissoes p ON c.id_permissao = p.id " +
                "WHERE f.matricula = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, matricula);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean(1);
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao verificar permissões: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}