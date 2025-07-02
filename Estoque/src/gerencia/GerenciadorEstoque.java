package gerencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import acessos.ControleAcesso;
import auxiliares.Funcionario;
import auxiliares.Produto;
import auxiliares.Estoque;
import conex.DatabaseConnection;

public class GerenciadorEstoque {

    private final ControleAcesso controleAcesso;

    public GerenciadorEstoque() {
        this.controleAcesso = new ControleAcesso();
    }

    public List<Estoque> buscarItensEstoque(String nomeProduto, Funcionario executor) {
        if (!controleAcesso.temPermissao(executor.getMatricula(), "consultar_estoque")) {
            System.err.println("ACESSO NEGADO: " + executor.getNome() + " nao tem permissao para consultar o estoque");
            return new ArrayList<>();
        }

        List<Estoque> itensEncontrados = new ArrayList<>();
        
        String sql = "SELECT e.id, e.quantidade, e.lote, e.data_validade, " +
                     "p.id as id_produto, p.nome, p.descricao, p.fabricante, p.categoria, p.tarja, p.preco, p.receita_obrigatoria " +
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
                        rs.getString("receita_obrigatoria").equalsIgnoreCase("sim")
                    );

                    Estoque itemEstoque = new Estoque(
                        rs.getInt("id"),
                        rs.getInt("quantidade"),
                        rs.getString("lote"),
                        rs.getDate("data_validade").toLocalDate(),
                        produto
                    );
                    itensEncontrados.add(itemEstoque);
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao buscar itens de estoque: " + e.getMessage());
            e.printStackTrace();
        }

        return itensEncontrados;
    }
    
    public Estoque buscarItemEstoquePorId(int idEstoque, Funcionario executor) {
        if (!controleAcesso.temPermissao(executor.getMatricula(), "consultar_estoque")) {
            System.err.println("ACESSO NEGADO: " + executor.getNome() + " nao tem permissao para consultar o estoque");
            return null;
        }

        String sql = "SELECT e.id, e.quantidade, e.lote, e.data_validade, " +
                     "p.id as id_produto, p.nome, p.descricao, p.fabricante, p.categoria, p.tarja, p.preco, p.receita_obrigatoria " +
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
                        rs.getString("receita_obrigatoria").equalsIgnoreCase("sim")
                    );

                    return new Estoque(
                        rs.getInt("id"),
                        rs.getInt("quantidade"),
                        rs.getString("lote"),
                        rs.getDate("data_validade").toLocalDate(),
                        produto
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao buscar item de estoque por ID: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Verifica se um determinado item de estoque exige receita
     */
    public boolean produtoExigeReceita(int idEstoque) {
        String sql = "SELECT p.receita_obrigatoria FROM Estoque e " +
                     "JOIN Produtos p ON e.id_produto = p.id WHERE e.id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idEstoque);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("receita_obrigatoria").equalsIgnoreCase("sim");
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao verificar exigencia de receita: " + e.getMessage());
        }
        return false;
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
            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("Erro de SQL ao tentar modificar o estoque: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}