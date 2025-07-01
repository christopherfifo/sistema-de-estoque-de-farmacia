package acessos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    private static Set<String> PERMISSOES_VALIDAS;

    static {
        PERMISSOES_VALIDAS = new HashSet<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT nome FROM Permissoes");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                PERMISSOES_VALIDAS.add(rs.getString("nome"));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao carregar permissões válidas do banco: " + e.getMessage());
        }
    }

    public GerenciadorPermissoes() {
        this.controleAcesso = new ControleAcesso();
    }

    public boolean concederPermissao(String matriculaExecutor, String nomeCargoAlvo, String nomePermissao) {
        return modificarPermissao(matriculaExecutor, nomeCargoAlvo, nomePermissao, "sim", "CONCEDER");
    }

    public boolean removerPermissao(String matriculaExecutor, String nomeCargoAlvo, String nomePermissao) {
        return modificarPermissao(matriculaExecutor, nomeCargoAlvo, nomePermissao, "nao", "REMOVER");
    }

    public Set<String> getPermissoesValidas() {
        return PERMISSOES_VALIDAS;
    }

    private boolean modificarPermissao(String matriculaExecutor, String nomeCargoAlvo, String nomePermissao, String novoValor, String acao) {

        if (!controleAcesso.temPermissao(matriculaExecutor, "controlar_acesso_funcionarios")) {
            System.out.println("ACESSO NEGADO: O usuário com matrícula '" + matriculaExecutor + "' não tem a permissão básica para alterar acessos.");
            return false;
        }

        // VALIDAÇÃO DE SEGURANÇA
        if (!PERMISSOES_VALIDAS.contains(nomePermissao)) {
            System.err.println("ERRO DE SEGURANÇA: Tentativa de modificar uma permissão inválida: '" + nomePermissao + "'.");
            return false;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            
            String cargoExecutor = getCargoDoFuncionario(conn, matriculaExecutor);

            if (cargoExecutor == null) {
                System.err.println("Falha na operação: Não foi possível encontrar o cargo do executor com matrícula '" + matriculaExecutor + "'.");
                return false;
            }

            // REGRAS DE HIERARQUIA
            if (cargoExecutor.equalsIgnoreCase("Administrador")) {
                if (nomeCargoAlvo.equalsIgnoreCase("Administrador")) {
                    System.out.println("ACESSO NEGADO: Um Administrador não pode alterar as permissões de outro Administrador.");
                    return false;
                }
            } else if (cargoExecutor.equalsIgnoreCase("Gerente")) {
                if (nomeCargoAlvo.equalsIgnoreCase("Gerente")) {
                    System.out.println("ACESSO NEGADO: Um Gerente não pode alterar as permissões de outro Gerente.");
                    return false;
                }
                if (nomeCargoAlvo.equalsIgnoreCase("Administrador")) {
                    System.out.println("ACESSO NEGADO: Um Gerente não pode alterar as permissões de um Administrador.");
                    return false;
                }
            } else {
                System.out.println("ACESSO NEGADO: Apenas Administradores e Gerentes podem alterar permissões. Cargo atual: '" + cargoExecutor + "'.");
                return false;
            }
            
            String sql = "UPDATE Permissoes SET " + nomePermissao + " = ? WHERE id = (SELECT id_permissao FROM Cargos WHERE nome = ?)";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, novoValor);
                stmt.setString(2, nomeCargoAlvo);

                int linhasAfetadas = stmt.executeUpdate();

                if (linhasAfetadas > 0) {
                    System.out.println("SUCESSO: Permissão '" + nomePermissao + "' foi " + acao.toLowerCase() + "da para o cargo '" + nomeCargoAlvo + "'.");
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