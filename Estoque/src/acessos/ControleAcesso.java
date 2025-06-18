package acessos;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import conex.DatabaseConnection;

public class ControleAcesso {

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
                    return "sim".equalsIgnoreCase(rs.getString(1));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}