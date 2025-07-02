package telas;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import auxiliares.Pedido;
import gerencia.GeradorDeRecibos;
import gerencia.GerenciadorVendas;

public class PainelHistoricoVendas extends JPanel {

    private final GerenciadorVendas gerenciadorVendas;
    private final DefaultTableModel modeloTabela;
    private final JTable tabelaHistorico;

    public PainelHistoricoVendas() {
        this.gerenciadorVendas = new GerenciadorVendas();
        setLayout(new BorderLayout(10, 10));

        String[] colunas = { "ID Pedido", "Data", "Valor Total", "Pagamento", "Status" };
        this.modeloTabela = new DefaultTableModel(colunas, 0);
        this.tabelaHistorico = new JTable(modeloTabela);
        add(new JScrollPane(tabelaHistorico), BorderLayout.CENTER);

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnVerRecibo = new JButton("Visualizar Recibo");
        JButton btnAtualizar = new JButton("Atualizar Historico");
        painelBotoes.add(btnVerRecibo);
        painelBotoes.add(btnAtualizar);
        add(painelBotoes, BorderLayout.SOUTH);

        btnAtualizar.addActionListener(e -> carregarHistorico());
        btnVerRecibo.addActionListener(e -> visualizarRecibo());
    }

    public void carregarHistorico() {
        List<Pedido> pedidos = gerenciadorVendas.buscarUltimosPedidos(100);
        modeloTabela.setRowCount(0);
        DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (Pedido p : pedidos) {
            modeloTabela.addRow(new Object[] {
                    p.getId(),
                    p.getDataPedido().format(formatador),
                    p.getValorTotal(),
                    p.getFormaPagamento(),
                    p.isCancelado() ? "Cancelado" : "Finalizado"
            });
        }
    }

    private void visualizarRecibo() {
        int linhaSelecionada = tabelaHistorico.getSelectedRow();
        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um pedido na tabela para ver o recibo", "Alerta",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        long idPedido = (long) modeloTabela.getValueAt(linhaSelecionada, 0);

        GeradorDeRecibos gerador = new GeradorDeRecibos();
        String recibo = gerador.gerarReciboVenda(idPedido);

        JTextArea areaTexto = new JTextArea(recibo);
        areaTexto.setFont(new Font("Monospaced", Font.PLAIN, 12));
        areaTexto.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(areaTexto);
        scrollPane.setPreferredSize(new Dimension(400, 450));

        JOptionPane.showMessageDialog(this, scrollPane, "Recibo do Pedido " + idPedido,
                JOptionPane.INFORMATION_MESSAGE);
    }
}