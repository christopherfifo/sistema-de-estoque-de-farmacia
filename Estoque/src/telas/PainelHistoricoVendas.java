package telas;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import auxiliares.Pedido;
import gerencia.GerenciadorVendas;

public class PainelHistoricoVendas extends JPanel {

    private final GerenciadorVendas gerenciadorVendas;
    private final DefaultTableModel modeloTabela;

    public PainelHistoricoVendas() {
        this.gerenciadorVendas = new GerenciadorVendas();
        setLayout(new BorderLayout(10, 10));

        String[] colunas = { "ID Pedido", "Data", "Valor Total", "Pagamento", "Status" };
        this.modeloTabela = new DefaultTableModel(colunas, 0);
        JTable tabelaHistorico = new JTable(modeloTabela);
        add(new JScrollPane(tabelaHistorico), BorderLayout.CENTER);

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnAtualizar = new JButton("Atualizar Historico");
        painelBotoes.add(btnAtualizar);
        add(painelBotoes, BorderLayout.SOUTH);

        btnAtualizar.addActionListener(e -> carregarHistorico());
    }

    public void carregarHistorico() {
        List<Pedido> pedidos = gerenciadorVendas.buscarUltimosPedidos(100); // Busca os ultimos 100 pedidos
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
}