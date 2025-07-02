package gerencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import acessos.ControleAcesso;
import auxiliares.Funcionario;
import auxiliares.ItemCarrinho;
import auxiliares.Pedido;
import auxiliares.Profissional;
import auxiliares.Receita;
import conex.DatabaseConnection;

public class GerenciadorVendas {

    private final ControleAcesso controleAcesso;

    public GerenciadorVendas() {
        this.controleAcesso = new ControleAcesso();
    }

    public long finalizarVenda(Carrinho carrinho, Funcionario executor, String formaPagamento, List<Receita> receitas,
            List<Profissional> profissionais) {
        if (!controleAcesso.temPermissao(executor.getMatricula(), "finalizar_venda")) {
            System.err.println("ACESSO NEGADO: " + executor.getNome() + " nao tem permissao para finalizar vendas");
            return -1;
        }
        if (carrinho.getItens().isEmpty()) {
            System.err.println("ERRO: O carrinho esta vazio");
            return -1;
        }

        Connection conn = null;
        GerenciadorReceitas gerenciadorReceitas = new GerenciadorReceitas();

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            String sqlPedido = "INSERT INTO Pedidos (dtPedido, valorTotal, sub_total, qtdItems, forma_pagamento, dtPagamento) VALUES (?, ?, ?, ?, ?, ?)";
            long idPedido;
            try (PreparedStatement stmtPedido = conn.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS)) {
                stmtPedido.setTimestamp(1, Timestamp.from(Instant.now()));
                stmtPedido.setBigDecimal(2, carrinho.calcularTotal());
                stmtPedido.setBigDecimal(3, carrinho.getSubtotal());
                stmtPedido.setInt(4, carrinho.getItens().size());
                stmtPedido.setString(5, formaPagamento);
                stmtPedido.setTimestamp(6, Timestamp.from(Instant.now()));
                stmtPedido.executeUpdate();

                try (ResultSet rs = stmtPedido.getGeneratedKeys()) {
                    if (rs.next()) {
                        idPedido = rs.getLong(1);
                    } else {
                        throw new SQLException("Falha ao obter ID do pedido");
                    }
                }
            }

            for (int i = 0; i < receitas.size(); i++) {
                Receita r = receitas.get(i);
                Profissional p = profissionais.get(i);
                r.setIdPedido(idPedido);
                r.setIdFuncionario(executor.getId());
                gerenciadorReceitas.cadastrarReceitaEProfissional(conn, r, p);
            }

            String sqlItemPedido = "INSERT INTO Itens_pedido (id_pedido, id_estoque, id_produto, quantidade, preco_unitario, sub_total) VALUES (?, ?, ?, ?, ?, ?)";
            String sqlBaixaEstoque = "UPDATE Estoque SET quantidade = quantidade - ? WHERE id = ? AND quantidade >= ?";

            try (PreparedStatement stmtItem = conn.prepareStatement(sqlItemPedido);
                    PreparedStatement stmtEstoque = conn.prepareStatement(sqlBaixaEstoque)) {

                for (ItemCarrinho item : carrinho.getItens()) {
                    stmtEstoque.setInt(1, item.getQuantidadeComprar());
                    stmtEstoque.setInt(2, item.getItemEstoque().getId());
                    stmtEstoque.setInt(3, item.getQuantidadeComprar());
                    if (stmtEstoque.executeUpdate() == 0) {
                        throw new SQLException(
                                "Estoque insuficiente para o produto: " + item.getItemEstoque().getProduto().getNome());
                    }

                    stmtItem.setLong(1, idPedido);
                    stmtItem.setInt(2, item.getItemEstoque().getId());
                    stmtItem.setInt(3, item.getItemEstoque().getProduto().getId());
                    stmtItem.setInt(4, item.getQuantidadeComprar());
                    stmtItem.setBigDecimal(5, item.getItemEstoque().getProduto().getPreco());
                    stmtItem.setBigDecimal(6, item.getSubtotal());
                    stmtItem.addBatch();
                }
                stmtItem.executeBatch();
            }

            conn.commit();
            return idPedido;

        } catch (SQLException e) {
            System.err.println("ERRO na transacao de venda: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("Transacao revertida com sucesso");
                } catch (SQLException ex) {
                    System.err.println("ERRO CRITICO: Falha ao reverter a transacao " + ex.getMessage());
                }
            }
            return -1;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean cancelarVenda(long idPedido, Funcionario executor) {
        if (!controleAcesso.temPermissao(executor.getMatricula(), "autorizar_reembolso")) {
            System.err.println("ACESSO NEGADO: " + executor.getNome() + " nao tem permissao para cancelar vendas");
            return false;
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Verificar se o pedido ja foi cancelado
            try (PreparedStatement stmtVerifica = conn
                    .prepareStatement("SELECT dtDevolucao FROM Pedidos WHERE id = ?")) {
                stmtVerifica.setLong(1, idPedido);
                try (ResultSet rs = stmtVerifica.executeQuery()) {
                    if (rs.next() && rs.getTimestamp("dtDevolucao") != null) {
                        throw new SQLException("Este pedido ja foi cancelado anteriormente");
                    }
                }
            }

            // Buscar os itens do pedido a ser cancelado
            String sqlBuscaItens = "SELECT id_estoque, quantidade FROM Itens_pedido WHERE id_pedido = ?";
            List<int[]> itensParaDevolver = new ArrayList<>();
            try (PreparedStatement stmtBusca = conn.prepareStatement(sqlBuscaItens)) {
                stmtBusca.setLong(1, idPedido);
                try (ResultSet rs = stmtBusca.executeQuery()) {
                    while (rs.next()) {
                        itensParaDevolver.add(new int[] { rs.getInt("id_estoque"), rs.getInt("quantidade") });
                    }
                }
            }

            if (itensParaDevolver.isEmpty()) {
                throw new SQLException("Pedido com ID " + idPedido + " nao encontrado ou nao possui itens");
            }

            // Devolver os itens ao estoque
            String sqlDevolveEstoque = "UPDATE Estoque SET quantidade = quantidade + ? WHERE id = ?";
            try (PreparedStatement stmtDevolve = conn.prepareStatement(sqlDevolveEstoque)) {
                for (int[] item : itensParaDevolver) {
                    stmtDevolve.setInt(1, item[1]); // quantidade
                    stmtDevolve.setInt(2, item[0]); // id_estoque
                    stmtDevolve.addBatch();
                }
                stmtDevolve.executeBatch();
            }

            // Marcar o pedido como cancelado/devolvido
            String sqlCancelaPedido = "UPDATE Pedidos SET dtDevolucao = ?, motivoDevolucao = ? WHERE id = ?";
            try (PreparedStatement stmtCancela = conn.prepareStatement(sqlCancelaPedido)) {
                stmtCancela.setTimestamp(1, Timestamp.from(Instant.now()));
                stmtCancela.setString(2, "Cancelado pelo operador: " + executor.getMatricula());
                stmtCancela.setLong(3, idPedido);
                stmtCancela.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("ERRO na transacao de cancelamento: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("Transacao de cancelamento revertida");
                } catch (SQLException ex) {
                    System.err
                            .println("ERRO CRITICO: Falha ao reverter a transacao de cancelamento " + ex.getMessage());
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<Pedido> buscarUltimosPedidos(int limite) {
        List<Pedido> pedidos = new ArrayList<>();
        String sql = "SELECT id, dtPedido, valorTotal, forma_pagamento, dtDevolucao FROM Pedidos ORDER BY dtPedido DESC LIMIT ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limite);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    pedidos.add(new Pedido(
                            rs.getLong("id"),
                            rs.getTimestamp("dtPedido").toLocalDateTime(),
                            rs.getBigDecimal("valorTotal"),
                            rs.getString("forma_pagamento"),
                            rs.getTimestamp("dtDevolucao") != null));
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao buscar historico de pedidos: " + e.getMessage());
            e.printStackTrace();
        }
        return pedidos;
    }
}