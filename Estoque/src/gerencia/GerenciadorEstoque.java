// Local: Estoque/src/gerencia/GerenciadorEstoque.java
package gerencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import acessos.ControleAcesso;
import auxiliares.Funcionario;
import auxiliares.Produto;
import auxiliares.Estoque; // Importa a nova classe
import conex.DatabaseConnection;

public class GerenciadorEstoque {

    private final ControleAcesso controleAcesso;

    public GerenciadorEstoque() {
        this.controleAcesso = new ControleAcesso();
    }

    /**
     * Busca itens no estoque, juntando dados do Produto e do Estoque.
     * Retorna uma lista de itens de estoque que correspondem à busca.
     * Requer permissão de "consultar_estoque".
     */
    public List<Estoque> buscarItensEstoque(String nomeProduto, Funcionario executor) {
        if (!controleAcesso.temPermissao(executor.getMatricula(), "consultar_estoque")) {
            System.err.println("ACESSO NEGADO: " + executor.getNome() + " nao tem permissao para consultar o estoque");
            return new ArrayList<>();
        }

        List<Estoque> itensEncontrados = new ArrayList<>();

        String sql = "SELECT e.id, e.quantidade, e.lote, e.data_validade, " +
                "p.id as id_produto, p.nome, p.descricao, p.fabricante, p.categoria, p.tarja, p.preco, p.receita_obrigatoria "
                +
                "FROM Estoque e " +
                "JOIN Produtos p ON e.id_produto = p.id " +
                "WHERE p.nome LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + nomeProduto + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Produto produto = new Produto(
                            rs.getInt("id_produto"),
                            rs.getString("nome"),
                            rs.getString("descricao"),
                            rs.getString("fabricante"),
                            rs.getString("categoria"),
                            rs.getString("tarja"),
                            rs.getBigDecimal("preco"),
                            rs.getString("receita_obrigatoria").equalsIgnoreCase("sim"));

                    Estoque itemEstoque = new Estoque(
                            rs.getInt("id"),
                            rs.getInt("quantidade"),
                            rs.getString("lote"),
                            rs.getDate("data_validade").toLocalDate(),
                            produto);
                    itensEncontrados.add(itemEstoque);
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao buscar itens de estoque: " + e.getMessage());
            e.printStackTrace();
        }

        return itensEncontrados;
    }

    public boolean modificarQuantidadeEstoque(int idEstoque, int novaQuantidade, Funcionario executor) {
        if (!controleAcesso.temPermissao(executor.getMatricula(), "atualizar_estoque")) {
            System.err.println("ACESSO NEGADO: " + executor.getNome() + " nao tem permissao para atualizar o estoque");
            return false;
        }

        if (novaQuantidade < 0) {
            System.err.println("ERRO: A quantidade nao pode ser negativa");
            return false;
        }

        String sql = "UPDATE Estoque SET quantidade = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, novaQuantidade);
            stmt.setInt(2, idEstoque);

            int linhasAfetadas = stmt.executeUpdate();

            return linhasAfetadas > 0;

        } catch (Exception e) {
            System.err.println("Erro ao tentar modificar o estoque: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Busca um unico item de estoque pelo seu ID especifico
     */
    public Estoque buscarItemEstoquePorId(int idEstoque, Funcionario executor) {
        if (!controleAcesso.temPermissao(executor.getMatricula(), "consultar_estoque")) {
            System.err.println("ACESSO NEGADO: " + executor.getNome() + " nao tem permissao para consultar o estoque");
            return null;
        }

        String sql = "SELECT e.id, e.quantidade, e.lote, e.data_validade, " +
                "p.id as id_produto, p.nome, p.descricao, p.fabricante, p.categoria, p.tarja, p.preco, p.receita_obrigatoria "
                +
                "FROM Estoque e " +
                "JOIN Produtos p ON e.id_produto = p.id " +
                "WHERE e.id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idEstoque);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Produto produto = new Produto(
                            rs.getInt("id_produto"),
                            rs.getString("nome"),
                            rs.getString("descricao"),
                            rs.getString("fabricante"),
                            rs.getString("categoria"),
                            rs.getString("tarja"),
                            rs.getBigDecimal("preco"),
                            rs.getString("receita_obrigatoria").equalsIgnoreCase("sim"));

                    return new Estoque(
                            rs.getInt("id"),
                            rs.getInt("quantidade"),
                            rs.getString("lote"),
                            rs.getDate("data_validade").toLocalDate(),
                            produto);
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao buscar item de estoque por ID: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
}