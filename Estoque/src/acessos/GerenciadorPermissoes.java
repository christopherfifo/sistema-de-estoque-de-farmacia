package acessos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import conex.DatabaseConnection;

/**
 * Gerencia a atribuição e remoção de permissões para os cargos do sistema.
 * 
 * REGRAS DE HIERARQUIA E SEGURANÇA:
 * - Administradores podem alterar permissões de Gerentes e Funcionários
 * - Administradores NÃO podem alterar permissões de outros Administradores (segurança)
 * - Gerentes podem alterar apenas permissões de Funcionários
 * - Gerentes NÃO podem alterar Gerentes ou Administradores
 * - Alterações em permissões de Administradores devem ser feitas pelo setor de TI
 */
public class GerenciadorPermissoes {

    private final ControleAcesso controleAcesso;

    // Usando ConcurrentHashMap para thread-safety
    private static final Set<String> PERMISSOES_VALIDAS = ConcurrentHashMap.newKeySet();

    static {
        carregarPermissoesValidas();
    }

    private static void carregarPermissoesValidas() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT nome FROM Permissoes");
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                PERMISSOES_VALIDAS.add(rs.getString("nome"));
            }
            
            if (PERMISSOES_VALIDAS.isEmpty()) {
                System.err.println("AVISO: Nenhuma permissão foi carregada do banco de dados.");
            }
            
        } catch (SQLException e) {
            System.err.println("ERRO FATAL: Falha ao carregar permissões válidas do banco de dados: " + e.getMessage());
            throw new RuntimeException("Não foi possível inicializar as permissões válidas da aplicação.", e);
        }
    }

    public GerenciadorPermissoes() {
        this.controleAcesso = new ControleAcesso();
    }

    public boolean concederPermissao(String matriculaExecutor, String nomeCargoAlvo, String nomePermissao) {
        return modificarPermissao(matriculaExecutor, nomeCargoAlvo, nomePermissao, 1, "CONCEDER");
    }

    public boolean removerPermissao(String matriculaExecutor, String nomeCargoAlvo, String nomePermissao) {
        return modificarPermissao(matriculaExecutor, nomeCargoAlvo, nomePermissao, 0, "REMOVER");
    }

    public Set<String> getPermissoesValidas() {
        return Collections.unmodifiableSet(PERMISSOES_VALIDAS);
    }

    private boolean modificarPermissao(String matriculaExecutor, String nomeCargoAlvo, String nomePermissao, int novoValor, String acao) {
        
        // VALIDAÇÃO DE ENTRADA
        if (matriculaExecutor == null || matriculaExecutor.trim().isEmpty()) {
            System.err.println("ERRO: Matrícula do executor não pode ser nula ou vazia.");
            return false;
        }
        
        if (nomeCargoAlvo == null || nomeCargoAlvo.trim().isEmpty()) {
            System.err.println("ERRO: Nome do cargo alvo não pode ser nulo ou vazio.");
            return false;
        }
        
        if (nomePermissao == null || nomePermissao.trim().isEmpty()) {
            System.err.println("ERRO: Nome da permissão não pode ser nulo ou vazio.");
            return false;
        }

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
            if (!podeAlterarCargo(cargoExecutor, nomeCargoAlvo)) {
                return false;
            }
            
            return executarModificacaoPermissao(conn, nomeCargoAlvo, nomePermissao, novoValor, acao);

        } catch (SQLException e) {
            System.err.println("Erro de SQL ao tentar modificar permissão: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Ocorreu um erro inesperado ao modificar a permissão.");
            e.printStackTrace();
            return false;
        }
    }

    private boolean podeAlterarCargo(String cargoExecutor, String nomeCargoAlvo) {
        if (cargoExecutor.equalsIgnoreCase("Administrador")) {
            if (nomeCargoAlvo.equalsIgnoreCase("Administrador")) {
                System.out.println("ACESSO NEGADO: Administradores não podem alterar permissões de outros Administradores. Contate o setor de TI para alterações de nível administrativo.");
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
        return true;
    }

    private boolean executarModificacaoPermissao(Connection conn, String nomeCargoAlvo, String nomePermissao, int novoValor, String acao) throws SQLException {
        String sql = "UPDATE Permissoes SET " + nomePermissao + " = ? WHERE id = (SELECT id_permissao FROM Cargos WHERE nome = ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, novoValor);
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