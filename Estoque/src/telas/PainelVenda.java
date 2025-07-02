package telas;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import auxiliares.Estoque;
import auxiliares.Funcionario;
import gerencia.Carrinho;
import gerencia.GerenciadorEstoque;

public class PainelVenda extends JPanel {

    private final Funcionario usuarioLogado;
    private final GerenciadorEstoque gerenciadorEstoque;
    private final Carrinho carrinho;

    private JTable tabelaBusca;
    private DefaultTableModel modeloTabelaBusca;
    private JTable tabelaCarrinho;
    private DefaultTableModel modeloTabelaCarrinho;
    private JLabel lblTotal;
    private JTextField txtBuscaProduto;

    public PainelVenda(Funcionario usuario) {
        this.usuarioLogado = usuario;
        this.gerenciadorEstoque = new GerenciadorEstoque();
        this.carrinho = new Carrinho();

        setLayout(new GridLayout(2, 1, 10, 10));

        JPanel painelBusca = new JPanel(new BorderLayout(5, 5));
        painelBusca.setBorder(BorderFactory.createTitledBorder("Buscar Produtos"));

        txtBuscaProduto = new JTextField();
        JButton btnBuscar = new JButton("Buscar");

        JPanel painelInputBusca = new JPanel(new BorderLayout());
        painelInputBusca.add(txtBuscaProduto, BorderLayout.CENTER);
        painelInputBusca.add(btnBuscar, BorderLayout.EAST);
        painelBusca.add(painelInputBusca, BorderLayout.NORTH);

        String[] colunasBusca = { "ID Estoque", "Nome", "Estoque Atual" };
        modeloTabelaBusca = new DefaultTableModel(colunasBusca, 0);
        tabelaBusca = new JTable(modeloTabelaBusca);
        painelBusca.add(new JScrollPane(tabelaBusca), BorderLayout.CENTER);

        JButton btnAdicionarCarrinho = new JButton("Adicionar ao Carrinho");
        painelBusca.add(btnAdicionarCarrinho, BorderLayout.SOUTH);

        add(painelBusca);

        JPanel painelCarrinho = new JPanel(new BorderLayout(5, 5));
        painelCarrinho.setBorder(BorderFactory.createTitledBorder("Carrinho"));

        String[] colunasCarrinho = { "Produto", "Qtd", "Preco Unit", "Subtotal" };
        modeloTabelaCarrinho = new DefaultTableModel(colunasCarrinho, 0);
        tabelaCarrinho = new JTable(modeloTabelaCarrinho);
        painelCarrinho.add(new JScrollPane(tabelaCarrinho), BorderLayout.CENTER);

        JPanel painelSulCarrinho = new JPanel(new BorderLayout());
        lblTotal = new JLabel("Total: R$ 0.00");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        JButton btnFinalizarVenda = new JButton("Finalizar Venda");
        painelSulCarrinho.add(lblTotal, BorderLayout.WEST);
        painelSulCarrinho.add(btnFinalizarVenda, BorderLayout.EAST);
        painelCarrinho.add(painelSulCarrinho, BorderLayout.SOUTH);

        add(painelCarrinho);

        btnBuscar.addActionListener(e -> buscarItens());
        btnAdicionarCarrinho.addActionListener(e -> adicionarAoCarrinho());
    }

    /**
     * Busca itens no estoque com base no texto do campo de busca e atualiza a
     * tabela
     */
    public void buscarItens() {
        String nomeProduto = txtBuscaProduto.getText();
        List<Estoque> itens = gerenciadorEstoque.buscarItensEstoque(nomeProduto, usuarioLogado);
        modeloTabelaBusca.setRowCount(0);
        for (Estoque item : itens) {
            modeloTabelaBusca.addRow(new Object[] {
                    item.getId(),
                    item.getProduto().getNome(),
                    item.getQuantidade()
            });
        }
    }

    private void adicionarAoCarrinho() {
        int linhaSelecionada = tabelaBusca.getSelectedRow();
        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um produto para adicionar", "Alerta",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idEstoque = (int) modeloTabelaBusca.getValueAt(linhaSelecionada, 0);

        Estoque itemSelecionado = gerenciadorEstoque.buscarItemEstoquePorId(idEstoque, usuarioLogado);

        if (itemSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Produto nao encontrado", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String qtdStr = JOptionPane.showInputDialog(this, "Digite a quantidade:", "Adicionar ao Carrinho",
                JOptionPane.PLAIN_MESSAGE);
        try {
            int quantidade = Integer.parseInt(qtdStr);
            carrinho.adicionarItem(itemSelecionado, quantidade);
            atualizarTabelaCarrinho();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Quantidade invalida", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void atualizarTabelaCarrinho() {
        modeloTabelaCarrinho.setRowCount(0);
        for (var item : carrinho.getItens()) {
            modeloTabelaCarrinho.addRow(new Object[] {
                    item.getItemEstoque().getProduto().getNome(),
                    item.getQuantidadeComprar(),
                    item.getItemEstoque().getProduto().getPreco(),
                    item.getSubtotal()
            });
        }
        lblTotal.setText("Total: R$ " + carrinho.calcularTotal());
    }
}