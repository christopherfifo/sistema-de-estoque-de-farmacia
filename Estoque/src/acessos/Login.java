package acessos;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;
import conex.DatabaseConnection;

public class Login {

    public static class LoginResult {
        public String token;
        public String cpf;
        public String matricula;
        public String nome;
        public String tipo;
        public int idCargo;
        public String cargo;

        public LoginResult(String token, String cpf, String matricula, String nome, String tipo, int idCargo, String cargo) {
            this.token = token;
            this.cpf = cpf;
            this.matricula = matricula;
            this.nome = nome;
            this.tipo = tipo;
            this.idCargo = idCargo;
            this.cargo = cargo;
        }
    }


    public LoginResult login(String matricula, String senha) {
        String sql = "SELECT f.cpf, f.matricula, f.nome, f.tipo, f.id_cargo, c.nome as cargo " +
                     "FROM Funcionarios f " +
                     "JOIN Cargos c ON f.id_cargo = c.id " +
                     "WHERE f.matricula = ? AND f.senha = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, matricula);
            stmt.setString(2, senha);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String token = UUID.randomUUID().toString();
                    return new LoginResult(
                        token,
                        rs.getString("cpf"),
                        rs.getString("matricula"),
                        rs.getString("nome"),
                        rs.getString("tipo"),
                        rs.getInt("id_cargo"),
                        rs.getString("cargo")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}