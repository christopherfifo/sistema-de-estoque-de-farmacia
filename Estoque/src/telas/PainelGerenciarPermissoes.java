package telas;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import acessos.GerenciadorPermissoes;
import auxiliares.Funcionario;
import gerencia.GerenciadorFuncionarios;

public class PainelGerenciarPermissoes extends JPanel {

    private final Funcionario adminLogado;
    private final GerenciadorFuncionarios gerenciadorFuncionarios;
    private final GerenciadorPermissoes gerenciadorPermissoes;

    private JComboBox<Funcionario> comboFuncionarios;
    private JPanel painelCheckboxes;
    private Map<String, JCheckBox> mapCheckboxes = new HashMap<>();
    private Map<String, Boolean> permissoesOriginais = new HashMap<>();

    public PainelGerenciarPermissoes(Funcionario admin) {
        this.adminLogado = admin;
        this.gerenciadorFuncionarios = new GerenciadorFuncionarios();
        this.gerenciadorPermissoes = new GerenciadorPermissoes();
        setLayout(new BorderLayout(10, 10));

        JPanel painelSelecao = new JPanel();
        this.comboFuncionarios = new JComboBox<>();
        painelSelecao.add(new JLabel("Selecione o Funcionario:"));
        painelSelecao.add(comboFuncionarios);

        this.painelCheckboxes = new JPanel(new GridLayout(0, 3));
        JScrollPane scrollPane = new JScrollPane(painelCheckboxes);

        JPanel painelAcao = new JPanel();
        JButton btnSalvar = new JButton("Salvar Alteracoes");
        painelAcao.add(btnSalvar);

        add(painelSelecao, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(painelAcao, BorderLayout.SOUTH);

        comboFuncionarios.addActionListener(e -> carregarPermissoesDoFuncionario());
        btnSalvar.addActionListener(e -> salvarAlteracoes());

        carregarListaDeFuncionarios();
        preencherPainelDePermissoes();
    }

    private void carregarListaDeFuncionarios() {
        List<Funcionario> funcionarios = gerenciadorFuncionarios.buscarTodosFuncionarios(adminLogado);
        comboFuncionarios.removeAllItems();
        for (Funcionario f : funcionarios) {
            comboFuncionarios.addItem(f);
        }
        comboFuncionarios.setSelectedIndex(-1);
    }

    private void preencherPainelDePermissoes() {
        painelCheckboxes.removeAll();
        mapCheckboxes.clear();

        Set<String> nomesPermissoesSet = gerenciadorPermissoes.getPermissoesValidas();
        List<String> nomesPermissoes = new ArrayList<>(nomesPermissoesSet);
        Collections.sort(nomesPermissoes);

        for (String nome : nomesPermissoes) {
            JCheckBox checkbox = new JCheckBox(nome.replace("_", " "));
            mapCheckboxes.put(nome, checkbox);
            painelCheckboxes.add(checkbox);
        }
        painelCheckboxes.revalidate();
        painelCheckboxes.repaint();
    }

    private void carregarPermissoesDoFuncionario() {
        Funcionario selecionado = (Funcionario) comboFuncionarios.getSelectedItem();
        if (selecionado == null) {
            for (JCheckBox checkbox : mapCheckboxes.values()) {
                checkbox.setSelected(false);
            }
            return;
        }

        this.permissoesOriginais = gerenciadorPermissoes.getPermissoesDoCargo(selecionado.getNomeCargo());

        for (Map.Entry<String, JCheckBox> entry : mapCheckboxes.entrySet()) {
            String nomePermissao = entry.getKey();
            JCheckBox checkbox = entry.getValue();
            checkbox.setSelected(permissoesOriginais.getOrDefault(nomePermissao, false));
        }
    }

    private void salvarAlteracoes() {
        Funcionario selecionado = (Funcionario) comboFuncionarios.getSelectedItem();
        if (selecionado == null) {
            JOptionPane.showMessageDialog(this, "Nenhum funcionario selecionado", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (permissoesOriginais.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "As permissoes do funcionario nao foram carregadas Selecione o funcionario novamente", "Alerta",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int sucessos = 0;
        int falhas = 0;

        for (Map.Entry<String, JCheckBox> entry : mapCheckboxes.entrySet()) {
            String nomePermissao = entry.getKey();
            boolean estadoAtual = entry.getValue().isSelected();
            boolean estadoOriginal = this.permissoesOriginais.getOrDefault(nomePermissao, false);

            if (estadoAtual != estadoOriginal) {
                boolean resultado;
                if (estadoAtual) {
                    resultado = gerenciadorPermissoes.concederPermissao(adminLogado.getMatricula(),
                            selecionado.getNomeCargo(), nomePermissao);
                } else {
                    resultado = gerenciadorPermissoes.removerPermissao(adminLogado.getMatricula(),
                            selecionado.getNomeCargo(), nomePermissao);
                }

                if (resultado) {
                    sucessos++;
                } else {
                    falhas++;
                }
            }
        }

        StringBuilder mensagemFinal = new StringBuilder();
        if (sucessos > 0) {
            mensagemFinal.append(sucessos).append(" permissoes alteradas com sucesso");
        }
        if (falhas > 0) {
            if (sucessos > 0)
                mensagemFinal.append("\n");
            mensagemFinal.append(falhas).append(" alteracoes falharam por falta de permissao (ver console)");
        }

        if (sucessos == 0 && falhas == 0) {
            JOptionPane.showMessageDialog(this, "Nenhuma alteracao foi feita", "Aviso",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            int messageType = (falhas > 0) ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE;
            JOptionPane.showMessageDialog(this, mensagemFinal.toString(), "Resultado da Operacao", messageType);
        }

        carregarPermissoesDoFuncionario();
    }
}