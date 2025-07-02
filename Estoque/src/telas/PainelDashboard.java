package telas;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import auxiliares.Estoque;
import gerencia.GerenciadorEstoque;

public class PainelDashboard extends JPanel {

    private final GerenciadorEstoque gerenciadorEstoque;
    private final DefaultTableModel modeloTabelaBaixoEstoque;
    private final DefaultTableModel modeloTabelaVencimento;

    public PainelDashboard() {
        this.gerenciadorEstoque = new GerenciadorEstoque();
        setLayout(new GridLayout(2, 1, 10, 10));

        JPanel painelBaixoEstoque = new JPanel(new BorderLayout());
        painelBaixoEstoque.setBorder(BorderFactory.createTitledBorder("Alerta: Produtos com Estoque Baixo"));
        String[] colunasBaixoEstoque = { "Produto", "Fabricante", "Qtd Atual", "Qtd Minima" };
        this.modeloTabelaBaixoEstoque = new DefaultTableModel(colunasBaixoEstoque, 0);
        JTable tabelaBaixoEstoque = new JTable(modeloTabelaBaixoEstoque);
        painelBaixoEstoque.add(new JScrollPane(tabelaBaixoEstoque), BorderLayout.CENTER);

        JPanel painelVencimento = new JPanel(new BorderLayout());
        painelVencimento
                .setBorder(BorderFactory.createTitledBorder("Alerta: Produtos Proximos do Vencimento (60 dias)"));
        String[] colunasVencimento = { "Produto", "Lote", "Qtd Atual", "Data de Vencimento" };
        this.modeloTabelaVencimento = new DefaultTableModel(colunasVencimento, 0);
        JTable tabelaVencimento = new JTable(modeloTabelaVencimento);
        painelVencimento.add(new JScrollPane(tabelaVencimento), BorderLayout.CENTER);

        add(painelBaixoEstoque);
        add(painelVencimento);
    }

    public void carregarAlertas() {
        List<Estoque> itensBaixoEstoque = gerenciadorEstoque.buscarProdutosComEstoqueBaixo();
        modeloTabelaBaixoEstoque.setRowCount(0);
        for (Estoque item : itensBaixoEstoque) {
            modeloTabelaBaixoEstoque.addRow(new Object[] {
                    item.getProduto().getNome(),
                    item.getProduto().getFabricante(),
                    item.getQuantidade(),
                    item.getQtdMinima()
            });
        }

        List<Estoque> itensVencimento = gerenciadorEstoque.buscarProdutosProximosDoVencimento(60);
        modeloTabelaVencimento.setRowCount(0);
        DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (Estoque item : itensVencimento) {
            modeloTabelaVencimento.addRow(new Object[] {
                    item.getProduto().getNome(),
                    item.getLote(),
                    item.getQuantidade(),
                    item.getDataValidade().format(formatador)
            });
        }
    }
}