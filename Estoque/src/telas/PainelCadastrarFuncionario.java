package telas;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.BorderFactory;
import auxiliares.Funcionario;
import gerencia.GerenciadorFuncionarios;

public class PainelCadastrarFuncionario extends JPanel {

    private final Funcionario adminLogado;
    
    private JTextField txtNome;
    private JTextField txtCpf;
    private JTextField txtMatricula;
    private JTextField txtEmail;
    private JTextField txtTelefone;
    private JPasswordField txtSenha;
    private JComboBox<String> comboTipo;
    private JComboBox<String> comboCargo;

    public PainelCadastrarFuncionario(Funcionario admin) {
        this.adminLogado = admin;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Cadastrar Novo Funcionario"));

        JPanel painelFormulario = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtNome = new JTextField(20);
        txtCpf = new JTextField(20);
        txtMatricula = new JTextField(20);
        txtEmail = new JTextField(20);
        txtTelefone = new JTextField(20);
        txtSenha = new JPasswordField(20);
        comboTipo = new JComboBox<>(new String[]{"funcionario", "adm", "dono"});
        comboCargo = new JComboBox<>(new String[]{"Caixa", "Farmaceutico", "Gerente", "Administrador"});

        gbc.gridx = 0; gbc.gridy = 0; painelFormulario.add(new JLabel("Nome Completo:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; painelFormulario.add(txtNome, gbc);
        gbc.gridx = 0; gbc.gridy = 1; painelFormulario.add(new JLabel("CPF:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; painelFormulario.add(txtCpf, gbc);
        gbc.gridx = 0; gbc.gridy = 2; painelFormulario.add(new JLabel("Matricula:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; painelFormulario.add(txtMatricula, gbc);
        gbc.gridx = 0; gbc.gridy = 3; painelFormulario.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; painelFormulario.add(txtEmail, gbc);
        gbc.gridx = 0; gbc.gridy = 4; painelFormulario.add(new JLabel("Telefone:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; painelFormulario.add(txtTelefone, gbc);
        gbc.gridx = 0; gbc.gridy = 5; painelFormulario.add(new JLabel("Senha Provisoria:"), gbc);
        gbc.gridx = 1; gbc.gridy = 5; painelFormulario.add(txtSenha, gbc);
        gbc.gridx = 0; gbc.gridy = 6; painelFormulario.add(new JLabel("Tipo de Conta:"), gbc);
        gbc.gridx = 1; gbc.gridy = 6; painelFormulario.add(comboTipo, gbc);
        gbc.gridx = 0; gbc.gridy = 7; painelFormulario.add(new JLabel("Cargo:"), gbc);
        gbc.gridx = 1; gbc.gridy = 7; painelFormulario.add(comboCargo, gbc);

        add(painelFormulario, BorderLayout.CENTER);
        
        JButton btnCadastrar = new JButton("Cadastrar Funcionario");
        add(btnCadastrar, BorderLayout.SOUTH);

        btnCadastrar.addActionListener(e -> cadastrarFuncionario());
    }

    private void cadastrarFuncionario() {
        String nome = txtNome.getText();
        String senha = new String(txtSenha.getPassword());

        if (nome.trim().isEmpty() || senha.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nome e Senha sao obrigatorios", "Erro de Validacao", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Funcionario novoFuncionario = new Funcionario(
            nome,
            txtCpf.getText(),
            txtMatricula.getText(),
            txtEmail.getText(),
            txtTelefone.getText(),
            (String) comboTipo.getSelectedItem(),
            senha,
            (String) comboCargo.getSelectedItem()
        );

        GerenciadorFuncionarios gerenciador = new GerenciadorFuncionarios();
        boolean sucesso = gerenciador.cadastrarFuncionario(adminLogado.getMatricula(), novoFuncionario);

        if (sucesso) {
            JOptionPane.showMessageDialog(this, "Funcionario cadastrado com sucesso", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            limparCampos();
        } else {
            JOptionPane.showMessageDialog(this, "Falha ao cadastrar o funcionario Verifique os dados e o console", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limparCampos() {
        txtNome.setText("");
        txtCpf.setText("");
        txtMatricula.setText("");
        txtEmail.setText("");
        txtTelefone.setText("");
        txtSenha.setText("");
        comboTipo.setSelectedIndex(0);
        comboCargo.setSelectedIndex(0);
    }
}