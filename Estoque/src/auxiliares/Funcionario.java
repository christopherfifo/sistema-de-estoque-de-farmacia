package auxiliares;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import conex.DatabaseConnection;

public class Funcionario {

    // Atributos do funcionário
    private String nome;
    private String cpf;
    private String matricula;
    private String email;
    private String telefone;
    private String tipo;
    private int idCargo;
    private String nomeCargo;

    private Funcionario(String nome, String cpf, String matricula, String email, String telefone, String tipo,
            int idCargo, String nomeCargo) {
        this.nome = nome;
        this.cpf = cpf;
        this.matricula = matricula;
        this.email = email;
        this.telefone = telefone;
        this.tipo = tipo;
        this.idCargo = idCargo;
        this.nomeCargo = nomeCargo;
    }

    public static Funcionario login(String matricula, String senha) {
        String sql = "SELECT f.cpf, f.matricula, f.nome, f.email, f.telefone, f.tipo, f.id_cargo, c.nome as cargo_nome "
                +
                "FROM Funcionarios f " +
                "JOIN Cargos c ON f.id_cargo = c.id " +
                "WHERE f.matricula = ? AND f.senha = ? AND f.atividade = 'ativo'";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, matricula);
            stmt.setString(2, senha);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Funcionario(
                            rs.getString("nome"),
                            rs.getString("cpf"),
                            rs.getString("matricula"),
                            rs.getString("email"),
                            rs.getString("telefone"),
                            rs.getString("tipo"),
                            rs.getInt("id_cargo"),
                            rs.getString("cargo_nome"));
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao tentar realizar o login: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public String getNome() {
        return nome;
    }

    public String getCpf() {
        return cpf;
    }

    public String getMatricula() {
        return matricula;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getTipo() {
        return tipo;
    }

    public int getIdCargo() {
        return idCargo;
    }

    public String getNomeCargo() {
        return nomeCargo;
    }
}