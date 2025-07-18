package telas;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import acessos.ControleAcesso;
import auxiliares.Estoque;
import auxiliares.Funcionario;
import auxiliares.Profissional;
import auxiliares.Receita;
import gerencia.Carrinho;
import gerencia.GerenciadorEstoque;
import gerencia.GerenciadorVendas;
import gerencia.GeradorDeRecibos;

public class PainelVenda extends JPanel {

    private final Funcionario usuarioLogado;
    private final GerenciadorEstoque gerenciadorEstoque;
    private final GerenciadorVendas gerenciadorVendas;
    private final Carrinho carrinho;
    private final ControleAcesso controleAcesso;
    private final List<Receita> receitasDaVenda;
    private final List<Profissional> profissionaisDaVenda;

    private JTable tabelaBusca;
    private DefaultTableModel modeloTabelaBusca;
    private JTable tabelaCarrinho;
    private DefaultTableModel modeloTabelaCarrinho;
    private JLabel lblTotal;
    private JLabel lblDesconto;
    private JTextField txtBuscaProduto;

    public PainelVenda(Funcionario usuario) {
        this.usuarioLogado = usuario;
        this.gerenciadorEstoque = new GerenciadorEstoque();
        this.gerenciadorVendas = new GerenciadorVendas();
        this.carrinho = new Carrinho();
        this.controleAcesso = new ControleAcesso();
        this.receitasDaVenda = new ArrayList<>();
        this.profissionaisDaVenda = new ArrayList<>();

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
        JPanel painelValores = new JPanel();
        painelValores.setLayout(new BoxLayout(painelValores, BoxLayout.Y_AXIS));

        lblDesconto = new JLabel("Desconto: R$ 0.00 (0.00%)");
        lblTotal = new JLabel("Total: R$ 0.00");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 18));

        painelValores.add(lblDesconto);
        painelValores.add(lblTotal);

        JButton btnAplicarDesconto = new JButton("Aplicar Desconto");
        JButton btnFinalizarVenda = new JButton("Finalizar Venda");
        JButton btnCancelarVenda = new JButton("Cancelar Venda Anterior");

        painelBotoesAcao.add(btnAplicarDesconto);
        painelBotoesAcao.add(btnCancelarVenda);
        painelBotoesAcao.add(btnFinalizarVenda);

        painelSulCarrinho.add(painelValores, BorderLayout.WEST);
        painelSulCarrinho.add(painelBotoesAcao, BorderLayout.EAST);
        painelCarrinho.add(painelSulCarrinho, BorderLayout.SOUTH);

        add(painelCarrinho);

        btnBuscar.addActionListener(e -> buscarItens());
        btnAdicionarCarrinho.addActionListener(e -> adicionarAoCarrinho());
        btnFinalizarVenda.addActionListener(e -> finalizarVenda());
        btnCancelarVenda.addActionListener(e -> cancelarVendaAnterior());
        btnAplicarDesconto.addActionListener(e -> aplicarDesconto());
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

            boolean receitaColetada = exibirDialogoColetaReceita();
            if (!receitaColetada)
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
            atualizarVisualizacaoCarrinho();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Quantidade invalida", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean exibirDialogoColetaReceita() {
        JTextField nomeProfissional = new JTextField();
        JComboBox<String> tipoRegistro = new JComboBox<>(new String[] { "CRM", "COFEN", "CRO", "CRF", "OUTRO" });
        JTextField numeroRegistro = new JTextField();
        JTextField codigoReceita = new JTextField();
        JComboBox<String> tipoReceita = new JComboBox<>(new String[] { "Receita Branca Comum (Simples)",
                "Receita Branca de Controle Especial", "Receita Azul (Tipo A)", "Receita Amarela (Tipo A)" });
        JTextField cpfPaciente = new JTextField();
        JTextField nomePaciente = new JTextField();
        JTextField dataNascPaciente = new JTextField();
        JTextField dataValidadeReceita = new JTextField();

        JPanel painel = new JPanel(new GridLayout(0, 2, 5, 5));
        painel.add(new JLabel("Nome do Profissional:"));
        painel.add(nomeProfissional);
        painel.add(new JLabel("Tipo de Registro:"));
        painel.add(tipoRegistro);
        painel.add(new JLabel("Numero do Registro:"));
        painel.add(numeroRegistro);
        painel.add(new JLabel("Codigo da Receita:"));
        painel.add(codigoReceita);
        painel.add(new JLabel("Tipo da Receita:"));
        painel.add(tipoReceita);
        painel.add(new JLabel("CPF do Paciente:"));
        painel.add(cpfPaciente);
        painel.add(new JLabel("Nome do Paciente:"));
        painel.add(nomePaciente);
        painel.add(new JLabel("Data Nasc. Paciente (dd/MM/yyyy):"));
        painel.add(dataNascPaciente);
        painel.add(new JLabel("Data Validade Receita (dd/MM/yyyy):"));
        painel.add(dataValidadeReceita);

        int result = JOptionPane.showConfirmDialog(this, painel, "Cadastro de Receita Medica",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                Receita receita = new Receita(
                        (String) tipoReceita.getSelectedItem(),
                        codigoReceita.getText(),
                        cpfPaciente.getText(),
                        nomePaciente.getText(),
                        LocalDate.parse(dataNascPaciente.getText(), formatter),
                        LocalDate.parse(dataValidadeReceita.getText(), formatter));
                Profissional profissional = new Profissional(
                        nomeProfissional.getText(),
                        (String) tipoRegistro.getSelectedItem(),
                        numeroRegistro.getText());
                receitasDaVenda.add(receita);
                profissionaisDaVenda.add(profissional);
                return true;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Dados da receita invalidos Verifique as datas", "Erro de Formato",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return false;
    }

    private void atualizarVisualizacaoCarrinho() {
        modeloTabelaCarrinho.setRowCount(0);
        for (var item : carrinho.getItens()) {
            modeloTabelaCarrinho.addRow(new Object[] {
                    item.getItemEstoque().getProduto().getNome(),
                    item.getQuantidadeComprar(),
                    item.getItemEstoque().getProduto().getPreco(),
                    item.getSubtotal()
            });
        }
        String textoDesconto = String.format("Desconto: R$ %.2f (%.2f%%)", carrinho.getValorDesconto(),
                carrinho.getPercentualDesconto());
        lblDesconto.setText(textoDesconto);
        String textoTotal = String.format("Total: R$ %.2f", carrinho.calcularTotal());
        lblTotal.setText(textoTotal);
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

        long idVenda = gerenciadorVendas.finalizarVenda(carrinho, usuarioLogado, formaPagamento, receitasDaVenda,
                profissionaisDaVenda);

        if (idVenda != -1) {
            carrinho.limpar();
            receitasDaVenda.clear();
            profissionaisDaVenda.clear();
            atualizarVisualizacaoCarrinho();
            buscarItens();

            GeradorDeRecibos gerador = new GeradorDeRecibos();
            String recibo = gerador.gerarReciboVenda(idVenda);
            exibirRecibo(recibo);

        } else {
            JOptionPane.showMessageDialog(this, "Falha ao finalizar a venda Verifique o console", "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exibirRecibo(String textoRecibo) {
        JTextArea areaTexto = new JTextArea(textoRecibo);
        areaTexto.setFont(new Font("Monospaced", Font.PLAIN, 12));
        areaTexto.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(areaTexto);
        scrollPane.setPreferredSize(new Dimension(400, 450));
        JOptionPane.showMessageDialog(this, scrollPane, "Recibo da Venda", JOptionPane.INFORMATION_MESSAGE);
    }

    private void cancelarVendaAnterior() {
        String idPedidoStr = JOptionPane.showInputDialog(this, "Digite o ID do pedido a ser cancelado:",
                "Cancelar Venda", JOptionPane.PLAIN_MESSAGE);
        if (idPedidoStr == null || idPedidoStr.trim().isEmpty())
            return;

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

    private void aplicarDesconto() {
        if (!controleAcesso.temPermissao(usuarioLogado.getMatricula(), "aplicar_desconto")) {
            JOptionPane.showMessageDialog(this, "ACESSO NEGADO: Voce nao tem permissao para aplicar descontos", "Erro",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        String percStr = JOptionPane.showInputDialog(this, "Digite o percentual de desconto (%):", "Aplicar Desconto",
                JOptionPane.PLAIN_MESSAGE);
        if (percStr == null)
            return;
        try {
            BigDecimal percentual = new BigDecimal(percStr.replace(",", "."));
            carrinho.aplicarDesconto(percentual);
            atualizarVisualizacaoCarrinho();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Percentual de desconto invalido", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}