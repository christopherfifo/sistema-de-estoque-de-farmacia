package gerencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import acessos.ControleAcesso;
import auxiliares.Funcionario;
import auxiliares.Produto;
import conex.DatabaseConnection;

public class GerenciadorEstoque {

    private final ControleAcesso controleAcesso;

    public GerenciadorEstoque() {
        this.controleAcesso = new ControleAcesso();
    }

    public List<Produto> buscarProdutoPorNome(String nomeBusca, Funcionario executor) {
        if (!controleAcesso.temPermissao(executor.getMatricula(), "consultar_estoque")) {
            System.err.println("ACESSO NEGADO: " + executor.getNome() + " não tem permissão para consultar o estoque.");
            return new ArrayList<>();
        }

        List<Produto> produtosEncontrados = new ArrayList<>();
        String sql = "SELECT id, nome, descricao, fabricante, categoria, tarja, preco, receita_obrigatoria FROM Produtos WHERE nome LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + nomeBusca + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Produto produto = new Produto(
                            rs.getInt("id"),
                            rs.getString("nome"),
                            rs.getString("descricao"),
                            rs.getString("fabricante"),
                            rs.getString("categoria"),
                            rs.getString("tarja"),
                            rs.getBigDecimal("preco"),
                            rs.getString("receita_obrigatoria").equalsIgnoreCase("sim"));
                    produtosEncontrados.add(produto);
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao buscar produtos: " + e.getMessage());
            e.printStackTrace();
        }

        return produtosEncontrados;
    }
}