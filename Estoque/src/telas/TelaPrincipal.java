package telas;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import auxiliares.Funcionario;
import java.awt.*;

public class TelaPrincipal extends JFrame {

    private final Funcionario usuarioLogado;
    private PainelGerenciarEstoque painelEstoque;
    private PainelVenda painelVenda;
    private PainelHistoricoVendas painelHistorico;

    private boolean estoqueJaCarregado = false;
    private boolean vendaJaCarregada = false;
    private boolean historicoJaCarregado = false;

    public TelaPrincipal(Funcionario funcionario) {
        this.usuarioLogado = funcionario;

        setTitle("Sistema de Farmacia - Usuario: " + usuarioLogado.getNome() + " (" + usuarioLogado.getNomeCargo()
                + ")");
        setSize(850, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane painelComAbas = new JTabbedPane();

        JPanel painelBoasVindas = new JPanel(new GridBagLayout());
        JLabel lblMensagem = new JLabel("Selecione uma opcao nas abas acima");
        lblMensagem.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        painelBoasVindas.add(lblMensagem);

        this.painelEstoque = new PainelGerenciarEstoque(this.usuarioLogado);
        this.painelVenda = new PainelVenda(this.usuarioLogado);
        this.painelHistorico = new PainelHistoricoVendas();

        painelComAbas.addTab("Inicio", painelBoasVindas);
        painelComAbas.addTab("Ponto de Venda", this.painelVenda);
        painelComAbas.addTab("Gerenciar Estoque", this.painelEstoque);
        painelComAbas.addTab("Historico de Vendas", this.painelHistorico);

        painelComAbas.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (painelComAbas.getSelectedComponent() == painelEstoque && !estoqueJaCarregado) {
                    painelEstoque.carregarEstoque();
                    estoqueJaCarregado = true;
                } else if (painelComAbas.getSelectedComponent() == painelVenda && !vendaJaCarregada) {
                    painelVenda.buscarItens();
                    vendaJaCarregada = true;
                } else if (painelComAbas.getSelectedComponent() == painelHistorico && !historicoJaCarregado) {
                    painelHistorico.carregarHistorico();
                    historicoJaCarregado = true;
                }
            }
        });

        add(painelComAbas);
    }
}