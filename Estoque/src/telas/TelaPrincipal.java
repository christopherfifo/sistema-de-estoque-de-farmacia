package telas;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import auxiliares.Funcionario;

public class TelaPrincipal extends JFrame {

    private Funcionario usuarioLogado;

    public TelaPrincipal(Funcionario funcionario) {
        this.usuarioLogado = funcionario;

        setTitle("Sistema de Estoque - Farma IFSP");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel painelVertical = new JPanel();
        painelVertical.setLayout(new BoxLayout(painelVertical, BoxLayout.Y_AXIS));

        painelVertical.add(Box.createVerticalGlue());

        JLabel lblBoasVindas = new JLabel("Bem-vindo(a), " + usuarioLogado.getNome());
        lblBoasVindas.setFont(lblBoasVindas.getFont().deriveFont(20.0f));
        lblBoasVindas.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblCargo = new JLabel("Cargo: " + usuarioLogado.getNomeCargo());
        lblCargo.setFont(lblCargo.getFont().deriveFont(16.0f));
        lblCargo.setAlignmentX(Component.CENTER_ALIGNMENT);

        painelVertical.add(lblBoasVindas);

        painelVertical.add(Box.createRigidArea(new Dimension(0, 10)));

        painelVertical.add(lblCargo);

        painelVertical.add(Box.createVerticalGlue());

        add(painelVertical, BorderLayout.CENTER);
    }
}