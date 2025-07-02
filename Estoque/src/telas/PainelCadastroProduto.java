package telas;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.BorderFactory;
import auxiliares.AreaEstoque;
import auxiliares.Funcionario;
import auxiliares.Produto;
import gerencia.GerenciadorEstoque;
import gerencia.GerenciadorProdutos;

public class PainelCadastroProduto extends JPanel {

    private final Funcionario usuarioLogado;
    private final GerenciadorProdutos gerenciadorProdutos;
    private final GerenciadorEstoque gerenciadorEstoque;

    private JTextField txtNome;
    private JTextField txtDescricao;
    private JTextField txtFabricante;
    private JTextField txtPreco;
    private JComboBox<String> comboCategoria;
    private JComboBox<String> comboTarja;
    private JCheckBox checkReceita;

    public PainelCadastroProduto(Funcionario usuario) {
        this.usuarioLogado = usuario;
        this.gerenciadorProdutos = new GerenciadorProdutos();
        this.gerenciadorEstoque = new GerenciadorEstoque();

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Cadastrar Novo Produto"));

        JPanel painelFormulario = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtNome = new JTextField(20);
        txtDescricao = new JTextField(20);
        txtFabricante = new JTextField(20);
        txtPreco = new JTextField(10);
        checkReceita = new JCheckBox("Exige Receita?");

        String[] categorias = { "medicamento", "antibiotico", "higiene", "cosmetico", "suplemento", "materiais_medicos",
                "infantil", "dermocosmetico", "outros" };
        comboCategoria = new JComboBox<>(categorias);

        String[] tarjas = { "vermelha", "preta", "amarela", "isento" };
        comboTarja = new JComboBox<>(tarjas);

        gbc.gridx = 0;
        gbc.gridy = 0;
        painelFormulario.add(new JLabel("Nome:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        painelFormulario.add(txtNome, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        painelFormulario.add(new JLabel("Descricao:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        painelFormulario.add(txtDescricao, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        painelFormulario.add(new JLabel("Fabricante:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 2;
        painelFormulario.add(txtFabricante, gbc);
        gbc.gridx = 0;
        gbc.gridy = 3;
        painelFormulario.add(new JLabel("Preco (ex: 12.50):"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 3;
        painelFormulario.add(txtPreco, gbc);
        gbc.gridx = 0;
        gbc.gridy = 4;
        painelFormulario.add(new JLabel("Categoria:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 4;
        painelFormulario.add(comboCategoria, gbc);
        gbc.gridx = 0;
        gbc.gridy = 5;
        painelFormulario.add(new JLabel("Tarja:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 5;
        painelFormulario.add(comboTarja, gbc);
        gbc.gridx = 1;
        gbc.gridy = 6;
        painelFormulario.add(checkReceita, gbc);

        add(painelFormulario, BorderLayout.CENTER);

        JButton btnCadastrar = new JButton("Cadastrar Produto e Adicionar Estoque");
        add(btnCadastrar, BorderLayout.SOUTH);

        btnCadastrar.addActionListener(e -> cadastrarProdutoEEstoque());
    }

    private void cadastrarProdutoEEstoque() {
        try {
            Produto novoProduto = new Produto(
                    0, // ID sera gerado pelo banco
                    txtNome.getText(),
                    txtDescricao.getText(),
                    txtFabricante.getText(),
                    (String) comboCategoria.getSelectedItem(),
                    (String) comboTarja.getSelectedItem(),
                    new BigDecimal(txtPreco.getText().replace(",", ".")),
                    checkReceita.isSelected());

            long idNovoProduto = gerenciadorProdutos.cadastrarNovoProduto(novoProduto, usuarioLogado);

            if (idNovoProduto != -1) {
                JOptionPane.showMessageDialog(this, "Produto cadastrado com sucesso ID: " + idNovoProduto, "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);
                solicitarDadosDoLote(idNovoProduto);
                limparCampos();
            } else {
                JOptionPane.showMessageDialog(this, "Falha ao cadastrar o produto", "Erro", JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Formato do preco invalido", "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro inesperado: " + ex.getMessage(), "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void solicitarDadosDoLote(long idProduto) {
        JTextField qtdField = new JTextField(5);
        JTextField loteField = new JTextField(10);
        JTextField validadeField = new JTextField(10);

        List<AreaEstoque> areas = gerenciadorEstoque.buscarAreasDeEstoque();
        JComboBox<AreaEstoque> comboAreas = new JComboBox<>(areas.toArray(new AreaEstoque[0]));

        JPanel painelLote = new JPanel();
        painelLote.add(new JLabel("Quantidade:"));
        painelLote.add(qtdField);
        painelLote.add(new JLabel("Lote:"));
        painelLote.add(loteField);
        painelLote.add(new JLabel("Validade (dd/MM/yyyy):"));
        painelLote.add(validadeField);
        painelLote.add(new JLabel("Local:"));
        painelLote.add(comboAreas);

        int result = JOptionPane.showConfirmDialog(this, painelLote, "Adicionar Lote Inicial de Estoque",
                JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                int quantidade = Integer.parseInt(qtdField.getText());
                String lote = loteField.getText();
                LocalDate validade = LocalDate.parse(validadeField.getText(),
                        DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                AreaEstoque areaSelecionada = (AreaEstoque) comboAreas.getSelectedItem();

                boolean sucesso = gerenciadorEstoque.adicionarLoteDeEstoque((int) idProduto, quantidade, lote, validade,
                        areaSelecionada.getId(), usuarioLogado);

                if (sucesso) {
                    JOptionPane.showMessageDialog(this, "Lote de estoque adicionado com sucesso", "Sucesso",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Falha ao adicionar lote de estoque", "Erro",
                            JOptionPane.ERROR_MESSAGE);
                }

            } catch (NumberFormatException | DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Dados do lote invalidos", "Erro de Formato",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void limparCampos() {
        txtNome.setText("");
        txtDescricao.setText("");
        txtFabricante.setText("");
        txtPreco.setText("");
        comboCategoria.setSelectedIndex(0);
        comboTarja.setSelectedIndex(0);
        checkReceita.setSelected(false);
    }
}