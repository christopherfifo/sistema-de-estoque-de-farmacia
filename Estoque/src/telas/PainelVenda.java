package telas;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import acessos.ControleAcesso;
import auxiliares.Estoque;
import auxiliares.Funcionario;
import gerencia.Carrinho;
import gerencia.GerenciadorEstoque;
import gerencia.GerenciadorVendas;

public class PainelVenda extends JPanel {

    private final Funcionario usuarioLogado;
    private final GerenciadorEstoque gerenciadorEstoque;
    private final GerenciadorVendas gerenciadorVendas;
    private final Carrinho carrinho;
    private final ControleAcesso controleAcesso;

    private JTable tabelaBusca;
    private DefaultTableModel modeloTabelaBusca;
    private JTable tabelaCarrinho;
    private DefaultTableModel modeloTabelaCarrinho;
    private JLabel lblTotal;
    private JTextField txtBuscaProduto;

    public PainelVenda(Funcionario usuario) {
        this.usuarioLogado = usuario;
        this.gerenciadorEstoque = new GerenciadorEstoque();
        this.gerenciadorVendas = new GerenciadorVendas();
        this.carrinho = new Carrinho();
        this.controleAcesso = new ControleAcesso();

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
        JPanel painelBotoesAcao = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        lblTotal = new JLabel("Total: R$ 0.00");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        JButton btnFinalizarVenda = new JButton("Finalizar Venda");
        JButton btnCancelarVenda = new JButton("Cancelar Venda Anterior"); // Botao novo

        painelBotoesAcao.add(btnCancelarVenda);
        painelBotoesAcao.add(btnFinalizarVenda);

        painelSulCarrinho.add(lblTotal, BorderLayout.WEST);
        painelSulCarrinho.add(painelBotoesAcao, BorderLayout.EAST);
        painelCarrinho.add(painelSulCarrinho, BorderLayout.SOUTH);

        add(painelCarrinho);

        btnBuscar.addActionListener(e -> buscarItens());
        btnAdicionarCarrinho.addActionListener(e -> adicionarAoCarrinho());
        btnFinalizarVenda.addActionListener(e -> finalizarVenda());
        btnCancelarVenda.addActionListener(e -> cancelarVendaAnterior());
    }

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
        boolean exigeReceita = gerenciadorEstoque.produtoExigeReceita(idEstoque);

        if (exigeReceita) {
            boolean temPermissao = controleAcesso.temPermissao(usuarioLogado.getMatricula(), "analisar_receita");
            if (!temPermissao) {
                JOptionPane.showMessageDialog(this,
                        "ACESSO NEGADO: Voce nao tem permissao para vender itens com receita", "Erro",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            int resposta = JOptionPane.showConfirmDialog(this,
                    "Este produto exige receita\nA receita foi apresentada e validada?", "Validacao de Receita",
                    JOptionPane.YES_NO_OPTION);
            if (resposta != JOptionPane.YES_OPTION)
                return;
        }

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
        lblTotal.setText("Total: R$ " + String.format("%.2f", carrinho.calcularTotal()));
    }

    private void finalizarVenda() {
        if (carrinho.getItens().isEmpty()) {
            JOptionPane.showMessageDialog(this, "O carrinho esta vazio", "Alerta", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String[] formasPagamento = { "dinheiro", "pix", "cartao_credito", "cartao_debito" };
        String formaPagamento = (String) JOptionPane.showInputDialog(this, "Selecione a forma de pagamento",
                "Pagamento", JOptionPane.QUESTION_MESSAGE, null, formasPagamento, formasPagamento[0]);

        if (formaPagamento == null)
            return;

        long idVenda = gerenciadorVendas.finalizarVenda(carrinho, usuarioLogado, formaPagamento);

        if (idVenda != -1) {
            JOptionPane.showMessageDialog(this, "Venda finalizada com sucesso ID do Pedido: " + idVenda, "Sucesso",
                    JOptionPane.INFORMATION_MESSAGE);
            carrinho.limpar();
            atualizarTabelaCarrinho();
            buscarItens();
        } else {
            JOptionPane.showMessageDialog(this, "Falha ao finalizar a venda Verifique o console", "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancelarVendaAnterior() {
        String idPedidoStr = JOptionPane.showInputDialog(this, "Digite o ID do pedido a ser cancelado:",
                "Cancelar Venda", JOptionPane.PLAIN_MESSAGE);
        if (idPedidoStr == null || idPedidoStr.trim().isEmpty()) {
            return;
        }

        try {
            long idPedido = Long.parseLong(idPedidoStr);
            boolean sucesso = gerenciadorVendas.cancelarVenda(idPedido, usuarioLogado);

            if (sucesso) {
                JOptionPane.showMessageDialog(this,
                        "Pedido " + idPedido + " cancelado com sucesso\nO estoque foi atualizado", "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);
                buscarItens();
            } else {
                JOptionPane.showMessageDialog(this, "Falha ao cancelar o pedido Verifique o ID e suas permissoes",
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "ID do pedido invalido", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}