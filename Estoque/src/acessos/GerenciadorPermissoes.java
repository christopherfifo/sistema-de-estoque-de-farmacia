package acessos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import conex.DatabaseConnection;

/**
 * Gerencia a atribuição e remoção de permissões para os cargos do sistema.
 * Implementa regras de hierarquia: Admins podem alterar Gerentes, mas Gerentes
 * não podem alterar outros Gerentes ou Admins.
 */
public class GerenciadorPermissoes {

    private final ControleAcesso controleAcesso;

    private static final Set<String> PERMISSOES_VALIDAS = new HashSet<>(Arrays.asList(
            "cadastrar_funcionarios", "controlar_acesso_funcionarios", "cadastrar_compras",
            "cancelar_compras", "gerenciar_fornecedores", "supervisionar_estoque",
            "atualizar_estoque", "consultar_estoque", "registrar_baixa_estoque",
            "registrar_venda_receita", "registrar_venda_simples", "finalizar_venda",
            "emitir_nota_fiscal", "registrar_pagamento", "aplicar_desconto",
            "aplicar_desconto_simples", "autorizar_reembolso", "solicitar_reembolso",
            "analisar_receita", "autorizar_controlados", "relatorio_financeiro",
            "relatorio_vendas_diarias", "gerar_orcamento", "indicar_medicamento",
            "solicitar_autorizacao"
    ));

    public GerenciadorPermissoes() {
        this.controleAcesso = new ControleAcesso();
    }

    public boolean modificarPermissaoCargo(String matriculaExecutor, String nomeCargoAlvo, String nomePermissao, boolean conceder) {

        if (!controleAcesso.temPermissao(matriculaExecutor, "controlar_acesso_funcionarios")) {
            System.out.println("ACESSO NEGADO: O usuário com matrícula '" + matriculaExecutor + "' não tem a permissão básica para alterar acessos.");
            return false;
        }

        // 2. VALIDAÇÃO DE SEGURANÇA
        if (!PERMISSOES_VALIDAS.contains(nomePermissao)) {
            System.err.println("ERRO DE SEGURANÇA: Tentativa de modificar uma permissão inválida: '" + nomePermissao + "'.");
            return false;
        }

        // Conexão será usada para a verificação de hierarquia e para a atualização
        try (Connection conn = DatabaseConnection.getConnection()) {
            
            String cargoExecutor = getCargoDoFuncionario(conn, matriculaExecutor);

            if (cargoExecutor == null) {
                System.err.println("Falha na operação: Não foi possível encontrar o cargo do executor com matrícula '" + matriculaExecutor + "'.");
                return false;
            }

            // REGRA: Gerente não pode alterar permissões de outro Gerente ou de um Administrador
            if (cargoExecutor.equalsIgnoreCase("Gerente")) {
                if (nomeCargoAlvo.equalsIgnoreCase("Gerente")) {
                    System.out.println("ACESSO NEGADO: Um Gerente não pode alterar as permissões de outro Gerente.");
                    return false;
                }
                if (nomeCargoAlvo.equalsIgnoreCase("Administrador")) {
                    System.out.println("ACESSO NEGADO: Um Gerente não pode alterar as permissões de um Administrador.");
                    return false;
                }
            }
            
            String sql = "UPDATE Permissoes SET " + nomePermissao + " = ? WHERE id = (SELECT id_permissao FROM Cargos WHERE nome = ?)";
            String novoValor = conceder ? "sim" : "nao";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, novoValor);
                stmt.setString(2, nomeCargoAlvo);

                int linhasAfetadas = stmt.executeUpdate();

                if (linhasAfetadas > 0) {
                    System.out.println("SUCESSO: Permissão '" + nomePermissao + "' foi atualizada para '" + novoValor + "' no cargo '" + nomeCargoAlvo + "'.");
                    return true;
                } else {
                    System.err.println("Falha ao atualizar: O cargo '" + nomeCargoAlvo + "' não foi encontrado.");
                    return false;
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro de SQL ao tentar modificar permissão: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Ocorreu um erro inesperado ao modificar a permissão.");
            e.printStackTrace();
            return false;
        }
    }

    private String getCargoDoFuncionario(Connection conn, String matricula) throws SQLException {
        String sql = "SELECT c.nome FROM Funcionarios f JOIN Cargos c ON f.id_cargo = c.id WHERE f.matricula = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, matricula);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("nome");
                }
            }
        }
        return null;
    }
}