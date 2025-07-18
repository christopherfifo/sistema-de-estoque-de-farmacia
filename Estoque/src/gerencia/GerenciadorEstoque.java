
package gerencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import acessos.ControleAcesso;
import auxiliares.Funcionario;
import auxiliares.Produto;
import auxiliares.Estoque;
import auxiliares.AreaEstoque;
import conex.DatabaseConnection;

public class GerenciadorEstoque {

    private final ControleAcesso controleAcesso;

    public GerenciadorEstoque() {
        this.controleAcesso = new ControleAcesso();
    }

    private Estoque criarEstoqueDoResultSet(ResultSet rs) throws SQLException {
        Produto produto = new Produto(rs.getInt("id_produto"), rs.getString("nome"), rs.getString("descricao"),
                rs.getString("fabricante"), rs.getString("categoria"), rs.getString("tarja"), rs.getBigDecimal("preco"),
                rs.getString("receita_obrigatoria").equalsIgnoreCase("sim"));
        AreaEstoque area = new AreaEstoque(rs.getInt("id_area"), rs.getString("setor"), rs.getString("prateleira"));

        return new Estoque(
                rs.getInt("id"),
                rs.getInt("quantidade"),
                rs.getInt("qtd_minima"),
                rs.getString("lote"),
                rs.getDate("data_fabricacao").toLocalDate(),
                rs.getDate("data_validade").toLocalDate(),
                rs.getBigDecimal("precoVenda"),
                produto,
                area);
    }

    public List<Estoque> buscarItensEstoque(String nomeProduto, Funcionario executor) {
        if (!controleAcesso.temPermissao(executor.getMatricula(), "consultar_estoque")) {
            System.err.println("ACESSO NEGADO: " + executor.getNome() + " nao tem permissao para consultar o estoque");
            return new ArrayList<>();
        }

        List<Estoque> itensEncontrados = new ArrayList<>();
        String sql = "SELECT e.*, p.id as id_produto, p.nome, p.descricao, p.fabricante, p.categoria, p.tarja, p.preco, p.receita_obrigatoria, a.id as id_area, a.setor, a.prateleira "
                +
                "FROM Estoque e " +
                "JOIN Produtos p ON e.id_produto = p.id " +
                "JOIN Areas_estoque a ON e.id_local = a.id " +
                "WHERE p.nome LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + nomeProduto + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    itensEncontrados.add(criarEstoqueDoResultSet(rs));
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao buscar itens de estoque: " + e.getMessage());
        }
        return itensEncontrados;
    }

    public Estoque buscarItemEstoquePorId(int idEstoque, Funcionario executor) {
        if (!controleAcesso.temPermissao(executor.getMatricula(), "consultar_estoque")) {
            System.err.println("ACESSO NEGADO: " + executor.getNome() + " nao tem permissao para consultar o estoque");
            return null;
        }

        String sql = "SELECT e.*, p.id as id_produto, p.nome, p.descricao, p.fabricante, p.categoria, p.tarja, p.preco, p.receita_obrigatoria, a.id as id_area, a.setor, a.prateleira "
                +
                "FROM Estoque e " +
                "JOIN Produtos p ON e.id_produto = p.id " +
                "JOIN Areas_estoque a ON e.id_local = a.id " +
                "WHERE e.id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idEstoque);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return criarEstoqueDoResultSet(rs);
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao buscar item de estoque por ID: " + e.getMessage());
        }
        return null;
    }

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

    public List<AreaEstoque> buscarAreasDeEstoque() {
        List<AreaEstoque> areas = new ArrayList<>();
        String sql = "SELECT id, setor, prateleira FROM Areas_estoque ORDER BY setor, prateleira";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                areas.add(new AreaEstoque(rs.getInt("id"), rs.getString("setor"), rs.getString("prateleira")));
            }
        } catch (Exception e) {
            System.err.println("Erro ao buscar areas de estoque: " + e.getMessage());
            e.printStackTrace();
        }
        return areas;
    }

    public boolean modificarLocalEstoque(int idEstoque, int idNovaArea, Funcionario executor) {
        if (!controleAcesso.temPermissao(executor.getMatricula(), "atualizar_estoque")) {
            System.err.println(
                    "ACESSO NEGADO: " + executor.getNome() + " nao tem permissao para alterar locais de estoque");
            return false;
        }

        String sql = "UPDATE Estoque SET id_local = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idNovaArea);
            stmt.setInt(2, idEstoque);
            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("Erro de SQL ao tentar modificar local do estoque: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<Estoque> buscarProdutosComEstoqueBaixo() {
        List<Estoque> itensEncontrados = new ArrayList<>();
        String sql = "SELECT e.*, p.id as id_produto, p.nome, p.descricao, p.fabricante, p.categoria, p.tarja, p.preco, p.receita_obrigatoria, a.id as id_area, a.setor, a.prateleira "
                +
                "FROM Estoque e " +
                "JOIN Produtos p ON e.id_produto = p.id " +
                "JOIN Areas_estoque a ON e.id_local = a.id " +
                "WHERE e.quantidade <= e.qtd_minima AND e.qtd_minima > 0 " +
                "ORDER BY e.quantidade ASC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                itensEncontrados.add(criarEstoqueDoResultSet(rs));
            }
        } catch (Exception e) {
            System.err.println("Erro ao buscar produtos com estoque baixo: " + e.getMessage());
        }
        return itensEncontrados;
    }

    public List<Estoque> buscarProdutosProximosDoVencimento(int dias) {
        List<Estoque> itensEncontrados = new ArrayList<>();
        String sql = "SELECT e.*, p.id as id_produto, p.nome, p.descricao, p.fabricante, p.categoria, p.tarja, p.preco, p.receita_obrigatoria, a.id as id_area, a.setor, a.prateleira "
                +
                "FROM Estoque e " +
                "JOIN Produtos p ON e.id_produto = p.id " +
                "JOIN Areas_estoque a ON e.id_local = a.id " +
                "WHERE e.data_validade BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL ? DAY) " +
                "ORDER BY e.data_validade ASC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, dias);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    itensEncontrados.add(criarEstoqueDoResultSet(rs));
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao buscar produtos proximos do vencimento: " + e.getMessage());
        }
        return itensEncontrados;
    }

    public boolean adicionarLoteDeEstoque(int idProduto, int quantidade, String lote, LocalDate dataValidade,
            int idArea, int qtdMinima, Funcionario executor) {
        if (!controleAcesso.temPermissao(executor.getMatricula(), "atualizar_estoque")) {
            System.err.println("ACESSO NEGADO: " + executor.getNome() + " nao tem permissao para adicionar estoque");
            return false;
        }

        String sql = "INSERT INTO Estoque (id_produto, id_local, quantidade, qtd_minima, lote, data_fabricacao, data_validade, precoVenda) "
                +
                "VALUES (?, ?, ?, ?, ?, CURDATE(), ?, (SELECT preco FROM Produtos WHERE id = ?))";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idProduto);
            stmt.setInt(2, idArea);
            stmt.setInt(3, quantidade);
            stmt.setInt(4, qtdMinima);
            stmt.setString(5, lote);
            stmt.setDate(6, java.sql.Date.valueOf(dataValidade));
            stmt.setInt(7, idProduto);

            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("Erro de SQL ao tentar adicionar lote de estoque: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}