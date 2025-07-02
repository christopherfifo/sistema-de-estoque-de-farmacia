package acessos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.Map;

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

        public int criarPermissoesPersonalizadas(String matriculaExecutor, String tipoPermissao, 
                                           Map<String, Boolean> permissoesPersonalizadas) {
        
        if (matriculaExecutor == null || matriculaExecutor.trim().isEmpty()) {
            System.err.println("ERRO: Matrícula do executor não pode ser nula ou vazia.");
            return -1;
        }
        
        if (tipoPermissao == null || tipoPermissao.trim().isEmpty()) {
            System.err.println("ERRO: Tipo de permissão não pode ser nulo ou vazio.");
            return -1;
        }
        
        if (permissoesPersonalizadas == null || permissoesPersonalizadas.isEmpty()) {
            System.err.println("ERRO: Lista de permissões não pode ser nula ou vazia.");
            return -1;
        }

        if (!controleAcesso.temPermissao(matriculaExecutor, "cadastrar_funcionarios")) {
            System.out.println("ACESSO NEGADO: O usuário com matrícula '" + matriculaExecutor + "' não tem permissão para cadastrar funcionários.");
            return -1;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            
            String cargoExecutor = getCargoDoFuncionario(conn, matriculaExecutor);
            
            if (cargoExecutor == null) {
                System.err.println("Falha na operação: Não foi possível encontrar o cargo do executor com matrícula '" + matriculaExecutor + "'.");
                return -1;
            }

            if (!podeGerenciarTipoPermissao(cargoExecutor, tipoPermissao)) {
                return -1;
            }

            if (!validarPermissoesPersonalizadas(permissoesPersonalizadas)) {
                return -1;
            }

            return executarCriacaoPermissao(conn, tipoPermissao, permissoesPersonalizadas);

        } catch (SQLException e) {
            System.err.println("Erro de SQL ao tentar criar permissões personalizadas: " + e.getMessage());
            return -1;
        } catch (Exception e) {
            System.err.println("Ocorreu um erro inesperado ao criar as permissões personalizadas.");
            e.printStackTrace();
            return -1;
        }
    }

        private boolean podeGerenciarTipoPermissao(String cargoExecutor, String tipoPermissao) {
        if (cargoExecutor.equalsIgnoreCase("Administrador")) {
            return true;
        } else if (cargoExecutor.equalsIgnoreCase("Gerente")) {
  
            if (tipoPermissao.equals("Permissao_Admin")) {
                System.out.println("ACESSO NEGADO: Um Gerente não pode criar permissões de Administrador.");
                return false;
            }
            return true;
        } else {
            System.out.println("ACESSO NEGADO: Apenas Administradores e Gerentes podem criar permissões personalizadas. Cargo atual: '" + cargoExecutor + "'.");
            return false;
        }
    }



        private boolean validarPermissoesPersonalizadas(Map<String, Boolean> permissoes) {
        for (String nomePermissao : permissoes.keySet()) {
            if (!PERMISSOES_VALIDAS.contains(nomePermissao)) {
                System.err.println("ERRO DE SEGURANÇA: Permissão inválida detectada: '" + nomePermissao + "'.");
                return false;
            }
        }
        return true;
    }

    public int criarPermissoesPadrao(String matriculaExecutor, String tipoPermissao) {
        Map<String, Boolean> permissoesPadrao = obterPermissoesPadrao(tipoPermissao);
        
        if (permissoesPadrao.isEmpty()) {
            System.err.println("ERRO: Tipo de permissão '" + tipoPermissao + "' não reconhecido.");
            return -1;
        }
        
        return criarPermissoesPersonalizadas(matriculaExecutor, tipoPermissao, permissoesPadrao);
    }

private Map<String, Boolean> obterPermissoesPadrao(String tipoPermissao) {
        Map<String, Boolean> permissoes = new HashMap<>();
        
        switch (tipoPermissao) {
            case "Permissao_Admin":
                // Administrador tem todas as permissões
                permissoes.put("cadastrar_funcionarios", true);
                permissoes.put("controlar_acesso_funcionarios", true);
                permissoes.put("cadastrar_compras", true);
                permissoes.put("cancelar_compras", true);
                permissoes.put("gerenciar_fornecedores", true);
                permissoes.put("supervisionar_estoque", true);
                permissoes.put("atualizar_estoque", true);
                permissoes.put("consultar_estoque", true);
                permissoes.put("registrar_baixa_estoque", true);
                permissoes.put("registrar_venda_receita", true);
                permissoes.put("registrar_venda_simples", true);
                permissoes.put("finalizar_venda", true);
                permissoes.put("emitir_nota_fiscal", true);
                permissoes.put("registrar_pagamento", true);
                permissoes.put("aplicar_desconto", true);
                permissoes.put("aplicar_desconto_simples", true);
                permissoes.put("autorizar_reembolso", true);
                permissoes.put("solicitar_reembolso", true);
                permissoes.put("analisar_receita", true);
                permissoes.put("autorizar_controlados", true);
                permissoes.put("relatorio_financeiro", true);
                permissoes.put("relatorio_vendas_diarias", true);
                permissoes.put("gerar_orcamento", true);
                permissoes.put("indicar_medicamento", true);
                permissoes.put("solicitar_autorizacao", true);
                break;
                
            case "Permissao_Gerente":
                // Gerente tem a maioria das permissões, exceto algumas administrativas
                permissoes.put("cadastrar_funcionarios", false);
                permissoes.put("controlar_acesso_funcionarios", false);
                permissoes.put("cadastrar_compras", true);
                permissoes.put("cancelar_compras", true);
                permissoes.put("gerenciar_fornecedores", true);
                permissoes.put("supervisionar_estoque", true);
                permissoes.put("atualizar_estoque", true);
                permissoes.put("consultar_estoque", true);
                permissoes.put("registrar_baixa_estoque", true);
                permissoes.put("registrar_venda_receita", true);
                permissoes.put("registrar_venda_simples", true);
                permissoes.put("finalizar_venda", true);
                permissoes.put("emitir_nota_fiscal", true);
                permissoes.put("registrar_pagamento", true);
                permissoes.put("aplicar_desconto", true);
                permissoes.put("aplicar_desconto_simples", true);
                permissoes.put("autorizar_reembolso", true);
                permissoes.put("solicitar_reembolso", true);
                permissoes.put("analisar_receita", true);
                permissoes.put("autorizar_controlados", true);
                permissoes.put("relatorio_financeiro", true);
                permissoes.put("relatorio_vendas_diarias", true);
                permissoes.put("gerar_orcamento", true);
                permissoes.put("indicar_medicamento", true);
                permissoes.put("solicitar_autorizacao", true);
                break;
                
            case "Permissao_Farmaceutico":
                // Farmacêutico tem permissões relacionadas a vendas e estoque
                permissoes.put("cadastrar_funcionarios", false);
                permissoes.put("controlar_acesso_funcionarios", false);
                permissoes.put("cadastrar_compras", false);
                permissoes.put("cancelar_compras", false);
                permissoes.put("gerenciar_fornecedores", false);
                permissoes.put("supervisionar_estoque", true);
                permissoes.put("atualizar_estoque", true);
                permissoes.put("consultar_estoque", true);
                permissoes.put("registrar_baixa_estoque", true);
                permissoes.put("registrar_venda_receita", true);
                permissoes.put("registrar_venda_simples", true);
                permissoes.put("finalizar_venda", true);
                permissoes.put("emitir_nota_fiscal", true);
                permissoes.put("registrar_pagamento", true);
                permissoes.put("aplicar_desconto", true);
                permissoes.put("aplicar_desconto_simples", true);
                permissoes.put("autorizar_reembolso", true);
                permissoes.put("solicitar_reembolso", true);
                permissoes.put("analisar_receita", true);
                permissoes.put("autorizar_controlados", true);
                permissoes.put("relatorio_financeiro", false);
                permissoes.put("relatorio_vendas_diarias", true);
                permissoes.put("gerar_orcamento", true);
                permissoes.put("indicar_medicamento", true);
                permissoes.put("solicitar_autorizacao", true);
                break;
                
            case "Permissao_Caixa":
                // Caixa tem permissões básicas de venda
                permissoes.put("cadastrar_funcionarios", false);
                permissoes.put("controlar_acesso_funcionarios", false);
                permissoes.put("cadastrar_compras", false);
                permissoes.put("cancelar_compras", false);
                permissoes.put("gerenciar_fornecedores", false);
                permissoes.put("supervisionar_estoque", false);
                permissoes.put("atualizar_estoque", false);
                permissoes.put("consultar_estoque", true);
                permissoes.put("registrar_baixa_estoque", false);
                permissoes.put("registrar_venda_receita", false);
                permissoes.put("registrar_venda_simples", true);
                permissoes.put("finalizar_venda", true);
                permissoes.put("emitir_nota_fiscal", true);
                permissoes.put("registrar_pagamento", true);
                permissoes.put("aplicar_desconto", true);
                permissoes.put("aplicar_desconto_simples", false);
                permissoes.put("autorizar_reembolso", false);
                permissoes.put("solicitar_reembolso", false);
                permissoes.put("analisar_receita", false);
                permissoes.put("autorizar_controlados", false);
                permissoes.put("relatorio_financeiro", false);
                permissoes.put("relatorio_vendas_diarias", true);
                permissoes.put("gerar_orcamento", false);
                permissoes.put("indicar_medicamento", false);
                permissoes.put("solicitar_autorizacao", false);
                break;
        }
        
        return permissoes;
    }


    private int executarCriacaoPermissao(Connection conn, String tipoPermissao, 
                                       Map<String, Boolean> permissoes) throws SQLException {
        
        StringBuilder sqlBuilder = new StringBuilder("INSERT INTO Permissoes (nome");
        StringBuilder valuesBuilder = new StringBuilder("VALUES (?");
        
        for (String nomePermissao : permissoes.keySet()) {
            sqlBuilder.append(", ").append(nomePermissao);
            valuesBuilder.append(", ?");
        }
        
        sqlBuilder.append(") ").append(valuesBuilder).append(")");
        
        try (PreparedStatement stmt = conn.prepareStatement(sqlBuilder.toString(), 
                                                          PreparedStatement.RETURN_GENERATED_KEYS)) {
            
            int paramIndex = 1;
            stmt.setString(paramIndex++, tipoPermissao);
            
            for (Boolean valor : permissoes.values()) {
                stmt.setBoolean(paramIndex++, valor);
            }
            
            int linhasAfetadas = stmt.executeUpdate();
            
            if (linhasAfetadas > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int novoId = generatedKeys.getInt(1);
                        System.out.println("SUCESSO: Permissão '" + tipoPermissao + "' criada com ID: " + novoId);
                        return novoId;
                    }
                }
            }
            
            System.err.println("ERRO: Falha ao criar a permissão personalizada.");
            return -1;
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