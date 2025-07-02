package telas;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import auxiliares.Funcionario;

public class TelaLogin extends JFrame {

    private JTextField txtMatricula;
    private JPasswordField txtSenha;
    private JButton btnLogin;

    public TelaLogin() {
        setTitle("Login - Sistema de Farmacia");
        setSize(350, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        JLabel lblMatricula = new JLabel("Matrícula:");
        lblMatricula.setBounds(30, 30, 80, 25);
        add(lblMatricula);

        txtMatricula = new JTextField();
        txtMatricula.setBounds(110, 30, 180, 25);
        add(txtMatricula);

        JLabel lblSenha = new JLabel("Senha:");
        lblSenha.setBounds(30, 70, 80, 25);
        add(lblSenha);

        txtSenha = new JPasswordField();
        txtSenha.setBounds(110, 70, 180, 25);
        add(txtSenha);

        btnLogin = new JButton("Entrar");
        btnLogin.setBounds(110, 110, 100, 30);
        add(btnLogin);

        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                realizarLogin();
            }
        });
    }

    private void realizarLogin() {
        String matricula = txtMatricula.getText();
        String senha = new String(txtSenha.getPassword());

        if (matricula.isEmpty() || senha.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Matrícula e senha são obrigatórios.", "Erro",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        Funcionario funcionarioLogado = Funcionario.login(matricula, senha);

        if (funcionarioLogado != null) {
            JOptionPane.showMessageDialog(this, "Bem-vindo, " + funcionarioLogado.getNome() + "!", "Sucesso",
                    JOptionPane.INFORMATION_MESSAGE);

            new TelaPrincipal(funcionarioLogado).setVisible(true);
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Matrícula ou senha inválida.", "Falha no Login",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}