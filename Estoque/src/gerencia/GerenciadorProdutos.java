package gerencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import acessos.ControleAcesso;
import auxiliares.Funcionario;
import auxiliares.Produto;
import conex.DatabaseConnection;

public class GerenciadorProdutos {
    
    private final ControleAcesso controleAcesso;

    public GerenciadorProdutos() {
        this.controleAcesso = new ControleAcesso();
    }

    public long cadastrarNovoProduto(Produto produto, Funcionario executor) {
        if (!controleAcesso.temPermissao(executor.getMatricula(), "supervisionar_estoque")) {
            System.err.println("ACESSO NEGADO: " + executor.getNome() + " nao tem permissao para cadastrar produtos");
            return -1;
        }

        String sql = "INSERT INTO Produtos (nome, descricao, armazenamento, armazenamento_especial, receita_obrigatoria, fabricante, categoria, tarja, preco) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, produto.getNome());
            stmt.setString(2, produto.getDescricao());
            stmt.setString(3, "Temperatura ambiente");
            stmt.setString(4, "nao");
            stmt.setString(5, produto.isReceitaObrigatoria() ? "sim" : "nao");
            stmt.setString(6, produto.getFabricante());
            stmt.setString(7, produto.getCategoria());
            stmt.setString(8, produto.getTarja());
            stmt.setBigDecimal(9, produto.getPreco());

            int linhasAfetadas = stmt.executeUpdate();

            if (linhasAfetadas > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getLong(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao cadastrar novo produto: " + e.getMessage());
            e.printStackTrace();
        }

        return -1;
    }
}