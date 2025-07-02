package gerencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import acessos.ControleAcesso;
import auxiliares.Funcionario;
import auxiliares.ItemCarrinho;
import conex.DatabaseConnection;

/**
 * Gerencia o processo de finalizacao de vendas e registro de pedidos
 */
public class GerenciadorVendas {

    private final ControleAcesso controleAcesso;

    public GerenciadorVendas() {
        this.controleAcesso = new ControleAcesso();
    }

    public long finalizarVenda(Carrinho carrinho, Funcionario executor, String formaPagamento) {
        if (!controleAcesso.temPermissao(executor.getMatricula(), "finalizar_venda")) {
            System.err.println("ACESSO NEGADO: " + executor.getNome() + " nao tem permissao para finalizar vendas");
            return -1;
        }

        if (carrinho.getItens().isEmpty()) {
            System.err.println("ERRO: O carrinho esta vazio");
            return -1;
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            String sqlPedido = "INSERT INTO Pedidos (dtPedido, valorTotal, qtdItems, forma_pagamento, dtPagamento) VALUES (?, ?, ?, ?, ?)";
            long idPedido;
            try (PreparedStatement stmtPedido = conn.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS)) {
                stmtPedido.setTimestamp(1, Timestamp.from(Instant.now()));
                stmtPedido.setBigDecimal(2, carrinho.calcularTotal());
                stmtPedido.setInt(3, carrinho.getItens().size());
                stmtPedido.setString(4, formaPagamento);
                stmtPedido.setTimestamp(5, Timestamp.from(Instant.now()));
                stmtPedido.executeUpdate();

                try (ResultSet rs = stmtPedido.getGeneratedKeys()) {
                    if (rs.next()) {
                        idPedido = rs.getLong(1);
                    } else {
                        throw new SQLException("Falha ao obter ID do pedido, nenhuma chave gerada");
                    }
                }
            }

            String sqlItemPedido = "INSERT INTO Itens_pedido (id_pedido, id_estoque, id_produto, quantidade, preco_unitario, sub_total) VALUES (?, ?, ?, ?, ?, ?)";
            String sqlBaixaEstoque = "UPDATE Estoque SET quantidade = quantidade - ? WHERE id = ? AND quantidade >= ?";

            try (PreparedStatement stmtItem = conn.prepareStatement(sqlItemPedido);
                 PreparedStatement stmtEstoque = conn.prepareStatement(sqlBaixaEstoque)) {

                for (ItemCarrinho item : carrinho.getItens()) {
                    // Decrementa o estoque
                    stmtEstoque.setInt(1, item.getQuantidadeComprar());
                    stmtEstoque.setInt(2, item.getItemEstoque().getId());
                    stmtEstoque.setInt(3, item.getQuantidadeComprar());
                    int linhasAfetadas = stmtEstoque.executeUpdate();

                    if (linhasAfetadas == 0) {
                        throw new SQLException("Estoque insuficiente para o produto: " + item.getItemEstoque().getProduto().getNome());
                    }

                    // Insere o item no pedido
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
}