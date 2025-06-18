package gerencia;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import acessos.ControleAcesso;
import auxiliares.Funcionario;
import conex.DatabaseConnection; 


public class GerenciadorFuncionarios {

    private final ControleAcesso controleAcesso;

    public GerenciadorFuncionarios() {
        this.controleAcesso = new ControleAcesso();
    }


    public boolean cadastrarFuncionario(String matriculaExecutor, Funcionario novoFuncionario, String senha) {
        

        if (!controleAcesso.temPermissao(matriculaExecutor, "cadastrar_funcionarios")) {
            System.out.println("ACESSO NEGADO: O usuário com matrícula '" + matriculaExecutor + "' não tem permissão para cadastrar funcionários.");
            return false;
        }

        String sql = "INSERT INTO Funcionarios (nome, cpf, matricula, email, telefone, senha, tipo, id_cargo, atividade) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'ativo')";


        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, novoFuncionario.getNome());
            stmt.setString(2, novoFuncionario.getCpf());
            stmt.setString(3, novoFuncionario.getMatricula());
            stmt.setString(4, novoFuncionario.getEmail());
            stmt.setString(5, novoFuncionario.getTelefone());
            stmt.setString(6, senha); 
            stmt.setString(7, novoFuncionario.getTipo());
            stmt.setInt(8, novoFuncionario.getIdCargo());

            int linhasAfetadas = stmt.executeUpdate();

            if (linhasAfetadas > 0) {
                System.out.println("Funcionário '" + novoFuncionario.getNome() + "' cadastrado com sucesso!");
                return true;
            } else {
                System.err.println("Falha ao cadastrar o funcionário. Nenhuma linha foi alterada no banco de dados.");
                return false;
            }

        } catch (SQLException e) {
            // Trata erros comuns de SQL, como violação de chave única (UNIQUE)
            if (e.getSQLState().startsWith("23")) { 
                 System.err.println("Erro no cadastro: CPF, Matrícula ou E-mail já existem no sistema. " + e.getMessage());
            } else {
                 System.err.println("Erro de SQL ao tentar cadastrar funcionário: " + e.getMessage());
            }
            return false;
        } catch (Exception e) {
            System.err.println("Ocorreu um erro inesperado durante o cadastro do funcionário.");
            e.printStackTrace();
            return false;
        }
    }
}