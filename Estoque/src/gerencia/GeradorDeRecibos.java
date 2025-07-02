package gerencia;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import conex.DatabaseConnection;

/**
 * Gera recibos para vendas
 */
public class GeradorDeRecibos {

    public String gerarReciboVenda(long idPedido) {
        StringBuilder recibo = new StringBuilder();
        Locale brLocale = new Locale("pt", "BR");
        NumberFormat formatadorMoeda = NumberFormat.getCurrencyInstance(brLocale);
        DateTimeFormatter formatadorData = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        String sqlPedido = "SELECT valorTotal, sub_total, forma_pagamento, dtPedido FROM Pedidos WHERE id = ?";
        String sqlItens = "SELECT i.quantidade, i.preco_unitario, p.nome " +
                "FROM Itens_pedido i JOIN Produtos p ON i.id_produto = p.id " +
                "WHERE i.id_pedido = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmtPedido = conn.prepareStatement(sqlPedido);
                PreparedStatement stmtItens = conn.prepareStatement(sqlItens)) {

            stmtPedido.setLong(1, idPedido);
            try (ResultSet rsPedido = stmtPedido.executeQuery()) {
                if (rsPedido.next()) {
                    BigDecimal total = rsPedido.getBigDecimal("valorTotal");
                    BigDecimal subtotal = rsPedido.getBigDecimal("sub_total");
                    BigDecimal desconto = (subtotal != null) ? subtotal.subtract(total) : BigDecimal.ZERO;

                    recibo.append("--------------------------------------------------\n");
                    recibo.append("              RECIBO DE VENDA\n");
                    recibo.append("                  FARMA IFSP\n");
                    recibo.append("--------------------------------------------------\n");
                    recibo.append("Pedido ID: ").append(idPedido).append("\n");
                    recibo.append("Data: ")
                            .append(rsPedido.getTimestamp("dtPedido").toLocalDateTime().format(formatadorData))
                            .append("\n\n");
                    recibo.append(String.format("%-25s %5s %10s\n", "PRODUTO", "QTD", "PRECO"));
                    recibo.append("--------------------------------------------------\n");

                    stmtItens.setLong(1, idPedido);
                    try (ResultSet rsItens = stmtItens.executeQuery()) {
                        while (rsItens.next()) {
                            String nome = rsItens.getString("nome");
                            if (nome.length() > 24)
                                nome = nome.substring(0, 24);

                            recibo.append(String.format("%-25s %5d %10s\n",
                                    nome,
                                    rsItens.getInt("quantidade"),
                                    formatadorMoeda.format(rsItens.getBigDecimal("preco_unitario"))));
                        }
                    }

                    recibo.append("--------------------------------------------------\n");
                    if (subtotal != null) {
                        recibo.append(String.format("Subtotal: %36s\n", formatadorMoeda.format(subtotal)));
                        recibo.append(String.format("Desconto: %36s\n", formatadorMoeda.format(desconto)));
                    }
                    recibo.append(String.format("TOTAL: %39s\n", formatadorMoeda.format(total)));
                    recibo.append("Forma de Pagamento: ").append(rsPedido.getString("forma_pagamento")).append("\n");
                    recibo.append("--------------------------------------------------\n");

                } else {
                    return "ERRO: Pedido com ID " + idPedido + " nao encontrado";
                }
            }
        } catch (SQLException e) {
            System.err.println("ERRO ao gerar recibo: " + e.getMessage());
            return "ERRO ao gerar recibo Verifique o console";
        }

        return recibo.toString();
    }
}