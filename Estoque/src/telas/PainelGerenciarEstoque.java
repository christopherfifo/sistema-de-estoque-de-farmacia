package telas;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import auxiliares.Estoque;
import auxiliares.Funcionario;
import auxiliares.AreaEstoque;
import gerencia.GerenciadorEstoque;

public class PainelGerenciarEstoque extends JPanel {

    private final Funcionario usuarioLogado;
    private final GerenciadorEstoque gerenciadorEstoque;

    private JTextField txtBuscaProduto;
    private JTable tabelaEstoque;
    private DefaultTableModel modeloTabela;

    public PainelGerenciarEstoque(Funcionario usuario) {
        this.usuarioLogado = usuario;
        this.gerenciadorEstoque = new GerenciadorEstoque();
        setLayout(new BorderLayout(10, 10));

        JPanel painelBusca = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelBusca.add(new JLabel("Buscar Produto:"));
        txtBuscaProduto = new JTextField(30);
        painelBusca.add(txtBuscaProduto);
        JButton btnBuscar = new JButton("Buscar");
        painelBusca.add(btnBuscar);
        add(painelBusca, BorderLayout.NORTH);

        String[] colunas = { "ID", "Produto", "Lote", "Qtd", "Qtd. Min", "Preco Venda", "Fabricacao", "Vencimento",
                "Local" };
        modeloTabela = new DefaultTableModel(colunas, 0);
        tabelaEstoque = new JTable(modeloTabela);
        add(new JScrollPane(tabelaEstoque), BorderLayout.CENTER);

        JPanel painelAcoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnModificarQtd = new JButton("Modificar Quantidade");
        JButton btnModificarLocal = new JButton("Modificar Local");
        painelAcoes.add(btnModificarQtd);
        painelAcoes.add(btnModificarLocal);
        add(painelAcoes, BorderLayout.SOUTH);

        btnBuscar.addActionListener(e -> carregarEstoque());
        btnModificarQtd.addActionListener(e -> modificarQuantidade());
        btnModificarLocal.addActionListener(e -> modificarLocal());
    }

    public void carregarEstoque() {
        String nomeBusca = txtBuscaProduto.getText();
        List<Estoque> itens = gerenciadorEstoque.buscarItensEstoque(nomeBusca, usuarioLogado);

        modeloTabela.setRowCount(0);
        DateTimeFormatter formatadorData = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Estoque item : itens) {
            modeloTabela.addRow(new Object[] {
                    item.getId(),
                    item.getProduto().getNome(),
                    item.getLote(),
                    item.getQuantidade(),
                    item.getQtdMinima(),
                    String.format("%.2f", item.getPrecoVenda()),
                    item.getDataFabricacao().format(formatadorData),
                    item.getDataValidade().format(formatadorData),
                    item.getAreaEstoque().toString()
            });
        }
    }

    private void modificarQuantidade() {
        int linhaSelecionada = tabelaEstoque.getSelectedRow();
        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um item na tabela", "Alerta", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idItemEstoque = (int) modeloTabela.getValueAt(linhaSelecionada, 0);

        String novaQtdStr = JOptionPane.showInputDialog(this,
                "Digite a nova quantidade para o item ID " + idItemEstoque + ":", "Modificar Estoque",
                JOptionPane.PLAIN_MESSAGE);

        if (novaQtdStr != null && !novaQtdStr.trim().isEmpty()) {
            try {
                int novaQuantidade = Integer.parseInt(novaQtdStr);

                boolean sucesso = gerenciadorEstoque.modificarQuantidadeEstoque(idItemEstoque, novaQuantidade,
                        usuarioLogado);

                if (sucesso) {
                    JOptionPane.showMessageDialog(this, "Quantidade atualizada com sucesso", "Sucesso",
                            JOptionPane.INFORMATION_MESSAGE);
                    carregarEstoque();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Falha ao atualizar a quantidade Verifique as permissoes ou o console", "Erro",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Por favor, insira um numero valido", "Erro de Formato",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void modificarLocal() {
        int linhaSelecionada = tabelaEstoque.getSelectedRow();
        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um item na tabela", "Alerta", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int idItemEstoque = (int) modeloTabela.getValueAt(linhaSelecionada, 0);
        String nomeProduto = (String) modeloTabela.getValueAt(linhaSelecionada, 1);

        List<AreaEstoque> areas = gerenciadorEstoque.buscarAreasDeEstoque();
        if (areas.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nenhuma area de estoque cadastrada", "Erro",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        AreaEstoque novaArea = (AreaEstoque) JOptionPane.showInputDialog(
                this,
                "Selecione o novo local para: " + nomeProduto,
                "Modificar Local de Armazenamento",
                JOptionPane.QUESTION_MESSAGE,
                null,
                areas.toArray(),
                areas.get(0));

        if (novaArea != null) {
            boolean sucesso = gerenciadorEstoque.modificarLocalEstoque(idItemEstoque, novaArea.getId(), usuarioLogado);
            if (sucesso) {
                JOptionPane.showMessageDialog(this, "Local de armazenamento atualizado com sucesso", "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);
                carregarEstoque();
            } else {
                JOptionPane.showMessageDialog(this, "Falha ao atualizar o local Verifique as permissoes ou o console",
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}