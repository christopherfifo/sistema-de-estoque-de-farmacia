package telas;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import acessos.ControleAcesso;
import auxiliares.Funcionario;
import gerencia.GerenciadorFuncionarios;
import java.awt.*;

public class TelaPrincipal extends JFrame {

    private final Funcionario usuarioLogado;
    private final ControleAcesso controleAcesso;
    private PainelDashboard painelDashboard;
    private PainelGerenciarEstoque painelEstoque;
    private PainelVenda painelVenda;
    private PainelHistoricoVendas painelHistorico;
    private PainelCadastroProduto painelCadastro;
    private PainelGerenciarPermissoes painelPermissoes;
    private PainelCadastrarFuncionario painelCadastrarFuncionario;

    public TelaPrincipal(Funcionario funcionario) {
        this.usuarioLogado = funcionario;
        this.controleAcesso = new ControleAcesso();

        setTitle("Sistema de Farmacia - Usuario: " + usuarioLogado.getNome() + " (" + usuarioLogado.getNomeCargo()
                + ")");
        setSize(850, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setJMenuBar(criarBarraDeMenu());

        JTabbedPane painelComAbas = new JTabbedPane();

        this.painelDashboard = new PainelDashboard();
        this.painelVenda = new PainelVenda(this.usuarioLogado);
        this.painelEstoque = new PainelGerenciarEstoque(this.usuarioLogado);
        this.painelHistorico = new PainelHistoricoVendas();

        painelComAbas.addTab("Inicio (Dashboard)", this.painelDashboard);
        painelComAbas.addTab("Ponto de Venda", this.painelVenda);
        painelComAbas.addTab("Gerenciar Estoque", this.painelEstoque);
        painelComAbas.addTab("Historico de Vendas", this.painelHistorico);

        JTabbedPane painelGestao = new JTabbedPane();

        if (controleAcesso.temPermissao(usuarioLogado.getMatricula(), "supervisionar_estoque")) {
            this.painelCadastro = new PainelCadastroProduto(this.usuarioLogado);
            painelGestao.addTab("Cadastro de Produtos", this.painelCadastro);
        }

        if (controleAcesso.temPermissao(usuarioLogado.getMatricula(), "cadastrar_funcionarios")) {
            this.painelCadastrarFuncionario = new PainelCadastrarFuncionario(this.usuarioLogado);
            painelGestao.addTab("Cadastro de Funcionarios", this.painelCadastrarFuncionario);
        }

        if (controleAcesso.temPermissao(usuarioLogado.getMatricula(), "controlar_acesso_funcionarios")) {
            this.painelPermissoes = new PainelGerenciarPermissoes(this.usuarioLogado);
            painelGestao.addTab("Gerenciar Permissoes", this.painelPermissoes);
        }

        if (painelGestao.getTabCount() > 0) {
            painelComAbas.addTab("Gestao", painelGestao);
        }

        painelComAbas.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                Component painelSelecionado = painelComAbas.getSelectedComponent();

                if (painelSelecionado == painelDashboard) {
                    painelDashboard.carregarAlertas();
                } else if (painelSelecionado == painelEstoque) {
                    painelEstoque.carregarEstoque();
                } else if (painelSelecionado == painelVenda) {
                    painelVenda.buscarItens();
                } else if (painelSelecionado == painelHistorico) {
                    painelHistorico.carregarHistorico();
                }
            }
        });

        painelDashboard.carregarAlertas();
        add(painelComAbas);
    }

    private JMenuBar criarBarraDeMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menuOpcoes = new JMenu("Opcoes");
        JMenuItem itemAlterarSenha = new JMenuItem("Alterar Senha");
        JMenuItem itemSair = new JMenuItem("Sair");

        itemAlterarSenha.addActionListener(e -> exibirDialogoAlterarSenha());

        itemSair.addActionListener(e -> {
            TelaPrincipal.this.dispose();
            new TelaLogin().setVisible(true);
        });

        menuOpcoes.add(itemAlterarSenha);
        menuOpcoes.addSeparator();
        menuOpcoes.add(itemSair);

        menuBar.add(menuOpcoes);
        return menuBar;
    }

    private void exibirDialogoAlterarSenha() {
        JPanel painel = new JPanel(new GridLayout(3, 2, 5, 5));
        JPasswordField txtSenhaAntiga = new JPasswordField();
        JPasswordField txtNovaSenha = new JPasswordField();
        JPasswordField txtConfirmaSenha = new JPasswordField();

        painel.add(new JLabel("Senha Antiga:"));
        painel.add(txtSenhaAntiga);
        painel.add(new JLabel("Nova Senha:"));
        painel.add(txtNovaSenha);
        painel.add(new JLabel("Confirmar Nova Senha:"));
        painel.add(txtConfirmaSenha);

        int result = JOptionPane.showConfirmDialog(this, painel, "Alterar Senha", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String senhaAntiga = new String(txtSenhaAntiga.getPassword());
            String novaSenha = new String(txtNovaSenha.getPassword());
            String confirmaSenha = new String(txtConfirmaSenha.getPassword());

            if (!novaSenha.equals(confirmaSenha)) {
                JOptionPane.showMessageDialog(this, "A nova senha e a confirmacao nao conferem", "Erro",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            GerenciadorFuncionarios gerenciador = new GerenciadorFuncionarios();
            boolean sucesso = gerenciador.alterarPropriaSenha(usuarioLogado.getMatricula(), senhaAntiga, novaSenha);

            if (sucesso) {
                JOptionPane.showMessageDialog(this, "Senha alterada com sucesso", "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Falha ao alterar a senha Verifique a senha antiga", "Erro",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}