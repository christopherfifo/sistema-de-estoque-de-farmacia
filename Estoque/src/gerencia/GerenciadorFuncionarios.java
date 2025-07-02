package gerencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import acessos.ControleAcesso;
import acessos.GerenciadorPermissoes;
import auxiliares.Funcionario;
import conex.DatabaseConnection;

public class GerenciadorFuncionarios {

    private final ControleAcesso controleAcesso;
    private final GerenciadorPermissoes gerenciadorPermissoes;

    public GerenciadorFuncionarios() {
        this.controleAcesso = new ControleAcesso();
        this.gerenciadorPermissoes = new GerenciadorPermissoes();
    }

    public boolean cadastrarFuncionario(String matriculaExecutor, Funcionario novoFuncionario) {

        if (!controleAcesso.temPermissao(matriculaExecutor, "cadastrar_funcionarios")) {
            System.out.println("ACESSO NEGADO: O usuário com matrícula '" + matriculaExecutor
                    + "' não tem permissão para cadastrar funcionários.");
            return false;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Inicia transação
            conn.setAutoCommit(false);

            try {
                // 1. Criar permissões padrão baseadas no cargo
                String tipoPermissao = mapearCargoParaTipoPermissao(novoFuncionario.getNomeCargo());
                if (tipoPermissao == null) {
                    System.err.println("ERRO: Cargo '" + novoFuncionario.getNomeCargo() + "' não reconhecido.");
                    conn.rollback();
                    return false;
                }

                int idPermissaoPersonalizada = gerenciadorPermissoes.criarPermissoesPadrao(matriculaExecutor,
                        tipoPermissao);

                if (idPermissaoPersonalizada == -1) {
                    System.err.println("ERRO: Falha ao criar permissões para o funcionário.");
                    conn.rollback();
                    return false;
                }

                int idCargoPersonalizado = criarCargoPersonalizado(conn, novoFuncionario.getNomeCargo(),
                        idPermissaoPersonalizada);

                if (idCargoPersonalizado == -1) {
                    System.err.println("ERRO: Falha ao criar cargo personalizado.");
                    conn.rollback();
                    return false;
                }

                // 3. Cadastrar o funcionário vinculado ao cargo criado
                boolean funcionarioCadastrado = inserirFuncionario(conn, novoFuncionario, idCargoPersonalizado);

                if (funcionarioCadastrado) {
                    conn.commit();
                    System.out.println("Funcionário '" + novoFuncionario.getNome() + "' cadastrado com sucesso!");
                    System.out.println("Permissões criadas com ID: " + idPermissaoPersonalizada);
                    System.out.println("Cargo criado com ID: " + idCargoPersonalizado);
                    return true;
                } else {
                    System.err.println("ERRO: Falha ao inserir funcionário no banco de dados.");
                    conn.rollback();
                    return false;
                }

            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            if (e.getSQLState().startsWith("23")) {
                System.err
                        .println("Erro no cadastro: CPF, Matrícula ou E-mail já existem no sistema. " + e.getMessage());
            } else {
                System.err.println("Erro de SQL ao tentar cadastrar funcionário: " + e.getMessage());
            }
            return false;
        } catch (Exception e) {
            System.err.println("Ocorreu um erro inesperado durante o cadastro do funcionário.");
            e.printStackTrace();
            return false;
        }
    }

    private String mapearCargoParaTipoPermissao(String nomeCargo) {
        switch (nomeCargo.toLowerCase()) {
            case "administrador":
                return "Permissao_Admin";
            case "gerente":
                return "Permissao_Gerente";
            case "farmaceutico":
                return "Permissao_Farmaceutico";
            case "caixa":
                return "Permissao_Caixa";
            default:
                return null;
        }
    }

    private int criarCargoPersonalizado(Connection conn, String nomeCargo, int idPermissao) throws SQLException {
        // Cria uma nova linha na tabela Cargos com o nome do cargo e vincula às
        // permissões
        String sql = "INSERT INTO Cargos (nome, id_permissao) VALUES (?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, nomeCargo);
            stmt.setInt(2, idPermissao);

            int linhasAfetadas = stmt.executeUpdate();

            if (linhasAfetadas > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        }

        return -1;
    }

    private boolean inserirFuncionario(Connection conn, Funcionario novoFuncionario, int idCargo) throws SQLException {
        String sql = "INSERT INTO Funcionarios (nome, cpf, matricula, email, telefone, senha, tipo, id_cargo, atividade) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'ativo')";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, novoFuncionario.getNome());
            stmt.setString(2, novoFuncionario.getCpf());
            stmt.setString(3, novoFuncionario.getMatricula());
            stmt.setString(4, novoFuncionario.getEmail());
            stmt.setString(5, novoFuncionario.getTelefone());
            stmt.setString(6, novoFuncionario.getSenha());
            stmt.setString(7, novoFuncionario.getTipo());
            stmt.setInt(8, idCargo);

            int linhasAfetadas = stmt.executeUpdate();

            if (linhasAfetadas > 0) {
                return true;
            } else {
                System.err.println("Falha ao cadastrar o funcionário. Nenhuma linha foi alterada no banco de dados.");
                return false;
            }
        }
    }

    public boolean editarFuncionario(String matriculaExecutor, String matriculaAlvo, Funcionario dadosAtualizados) {
        if (!controleAcesso.temPermissao(matriculaExecutor, "cadastrar_funcionarios")) {
            System.out.println("ACESSO NEGADO: O usuário com matrícula '" + matriculaExecutor
                    + "' não tem permissão para editar funcionários.");
            return false;
        }

        String sql = "UPDATE Funcionarios SET nome = ?, cpf = ?, email = ?, telefone = ?, tipo = ? WHERE matricula = ? AND atividade = 'ativo'";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, dadosAtualizados.getNome());
            stmt.setString(2, dadosAtualizados.getCpf());
            stmt.setString(3, dadosAtualizados.getEmail());
            stmt.setString(4, dadosAtualizados.getTelefone());
            stmt.setString(5, dadosAtualizados.getTipo());
            stmt.setString(6, matriculaAlvo);

            int linhasAfetadas = stmt.executeUpdate();

            if (linhasAfetadas > 0) {
                System.out.println("Funcionário com matrícula '" + matriculaAlvo + "' editado com sucesso!");
                return true;
            } else {
                System.err.println("Nenhum funcionário ativo encontrado com a matrícula: " + matriculaAlvo);
                return false;
            }

        } catch (SQLException e) {
            if (e.getSQLState().startsWith("23")) {
                System.err.println("Erro na edição: CPF ou E-mail já existem no sistema. " + e.getMessage());
            } else {
                System.err.println("Erro de SQL ao tentar editar funcionário: " + e.getMessage());
            }
            return false;
        } catch (Exception e) {
            System.err.println("Ocorreu um erro inesperado durante a edição do funcionário.");
            e.printStackTrace();
            return false;
        }
    }

    public boolean desativarFuncionario(String matriculaExecutor, String matriculaAlvo) {
        if (!controleAcesso.temPermissao(matriculaExecutor, "controlar_acesso_funcionarios")) {
            System.out.println("ACESSO NEGADO: O usuário com matrícula '" + matriculaExecutor
                    + "' não tem permissão para desativar funcionários.");
            return false;
        }

        String sql = "UPDATE Funcionarios SET atividade = 'inativo' WHERE matricula = ? AND atividade = 'ativo'";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, matriculaAlvo);

            int linhasAfetadas = stmt.executeUpdate();

            if (linhasAfetadas > 0) {
                System.out.println("Funcionário com matrícula '" + matriculaAlvo + "' desativado com sucesso!");
                return true;
            } else {
                System.err.println("Nenhum funcionário ativo encontrado com a matrícula: " + matriculaAlvo);
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Erro de SQL ao tentar desativar funcionário: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Ocorreu um erro inesperado durante a desativação do funcionário.");
            e.printStackTrace();
            return false;
        }
    }

    public Funcionario buscarFuncionario(String matriculaExecutor, String matriculaBusca) {
        if (!controleAcesso.temPermissao(matriculaExecutor, "cadastrar_funcionarios")) {
            System.out.println("ACESSO NEGADO: O usuário com matrícula '" + matriculaExecutor
                    + "' não tem permissão para buscar funcionários.");
            return null;
        }

        String sql = "SELECT f.nome, f.cpf, f.matricula, f.email, f.telefone, f.tipo, f.id_cargo, c.nome as cargo_nome "
                +
                "FROM Funcionarios f " +
                "JOIN Cargos c ON f.id_cargo = c.id " +
                "WHERE f.matricula = ? AND f.atividade = 'ativo'";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, matriculaBusca);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Funcionario(
                            rs.getString("nome"),
                            rs.getString("cpf"),
                            rs.getString("matricula"),
                            rs.getString("email"),
                            rs.getString("telefone"),
                            rs.getString("tipo"),
                            rs.getString("cargo_nome"),
                            "");
                } else {
                    System.out.println("Nenhum funcionário ativo encontrado com a matrícula: " + matriculaBusca);
                    return null;
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro de SQL ao buscar funcionário: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("Ocorreu um erro inesperado durante a busca do funcionário.");
            e.printStackTrace();
            return null;
        }
    }

    public boolean alterarPropriaSenha(String matricula, String senhaAntiga, String novaSenha) {
        if (novaSenha == null || novaSenha.trim().isEmpty()) {
            System.err.println("ERRO: A nova senha nao pode ser vazia");
            return false;
        }

        String sqlVerifica = "SELECT id FROM Funcionarios WHERE matricula = ? AND senha = ?";
        String sqlAtualiza = "UPDATE Funcionarios SET senha = ? WHERE matricula = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmtVerifica = conn.prepareStatement(sqlVerifica);
                PreparedStatement stmtAtualiza = conn.prepareStatement(sqlAtualiza)) {

            stmtVerifica.setString(1, matricula);
            stmtVerifica.setString(2, senhaAntiga);

            try (ResultSet rs = stmtVerifica.executeQuery()) {
                if (!rs.next()) {
                    System.err.println("ERRO: Senha antiga incorreta");
                    return false;
                }
            }

            stmtAtualiza.setString(1, novaSenha);
            stmtAtualiza.setString(2, matricula);

            return stmtAtualiza.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erro de SQL ao tentar alterar a senha: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}