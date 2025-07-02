package telas;

import javax.swing.*;
import java.awt.Font;
import java.awt.GridBagLayout;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import auxiliares.Funcionario;

public class TelaPrincipal extends JFrame {

    private Funcionario usuarioLogado;
    private PainelGerenciarEstoque painelEstoque;
    private boolean estoqueJaCarregado = false;

    public TelaPrincipal(Funcionario funcionario) {
        this.usuarioLogado = funcionario;

        setTitle("Sistema de Estoque - Usuário: " + usuarioLogado.getNome() + " (" + usuarioLogado.getNomeCargo()
                + ")");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane painelComAbas = new JTabbedPane();

        JPanel painelBoasVindas = new JPanel(new GridBagLayout());
        JLabel lblMensagem = new JLabel("Selecione uma opção nas abas acima.");
        lblMensagem.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        painelBoasVindas.add(lblMensagem);

        this.painelEstoque = new PainelGerenciarEstoque(this.usuarioLogado);

        painelComAbas.addTab("Início", painelBoasVindas);
        painelComAbas.addTab("Gerenciar Estoque", this.painelEstoque);

        painelComAbas.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (painelComAbas.getSelectedComponent() == painelEstoque && !estoqueJaCarregado) {
                    System.out.println("Aba de estoque selecionada. Carregando dados...");
                    painelEstoque.carregarEstoque();
                    estoqueJaCarregado = true;
                }
            }
        });

        add(painelComAbas);
    }
}