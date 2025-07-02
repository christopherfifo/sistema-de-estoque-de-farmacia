package telas;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import auxiliares.Funcionario;
import auxiliares.Estoque;
import gerencia.GerenciadorEstoque;

public class PainelGerenciarEstoque extends JPanel {

    private final Funcionario usuarioLogado;
    private final GerenciadorEstoque gerenciadorEstoque;

    private JTextField txtBuscaProduto;
    private JButton btnBuscar;
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
        btnBuscar = new JButton("Buscar");
        painelBusca.add(btnBuscar);
        add(painelBusca, BorderLayout.NORTH);

        String[] colunas = { "ID Estoque", "Nome", "Fabricante", "Quantidade", "Preço" };
        modeloTabela = new DefaultTableModel(colunas, 0);
        tabelaEstoque = new JTable(modeloTabela);
        add(new JScrollPane(tabelaEstoque), BorderLayout.CENTER);

        JPanel painelAcoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnModificarQtd = new JButton("Modificar Quantidade");
        painelAcoes.add(btnModificarQtd);
        add(painelAcoes, BorderLayout.SOUTH);

        btnBuscar.addActionListener(e -> carregarEstoque());
        btnModificarQtd.addActionListener(e -> modificarQuantidade());
    }

    /**
     * Método público que carrega os dados do estoque na tabela.
     * Pode ser chamado de fora da classe (pela TelaPrincipal).
     */
    public void carregarEstoque() {
        String nomeBusca = txtBuscaProduto.getText();
        List<Estoque> itens = gerenciadorEstoque.buscarItensEstoque(nomeBusca, usuarioLogado);

        modeloTabela.setRowCount(0);
        for (Estoque item : itens) {
            modeloTabela.addRow(new Object[] {
                    item.getId(),
                    item.getProduto().getNome(),
                    item.getProduto().getFabricante(),
                    item.getQuantidade(),
                    item.getProduto().getPreco()
            });
        }
    }

    private void modificarQuantidade() {
        int linhaSelecionada = tabelaEstoque.getSelectedRow();
        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, selecione um item na tabela", "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idItemEstoque = (int) modeloTabela.getValueAt(linhaSelecionada, 0);

        String novaQtdStr = JOptionPane.showInputDialog(this,
                "Digite a nova quantidade para o item " + idItemEstoque + ":", "Modificar Estoque",
                JOptionPane.PLAIN_MESSAGE);

        if (novaQtdStr != null && !novaQtdStr.trim().isEmpty()) {
            try {
                int novaQuantidade = Integer.parseInt(novaQtdStr);

                boolean sucesso = gerenciadorEstoque.modificarQuantidadeEstoque(idItemEstoque, novaQuantidade,
                        usuarioLogado);

                if (sucesso) {
                    JOptionPane.showMessageDialog(this, "Quantidade atualizada com sucesso!", "Sucesso",
                            JOptionPane.INFORMATION_MESSAGE);
                    carregarEstoque();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Falha ao atualizar a quantidade. Verifique as permissoes ou o console para erros", "Erro",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Por favor, insira um numero valido", "Erro de Formato",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}