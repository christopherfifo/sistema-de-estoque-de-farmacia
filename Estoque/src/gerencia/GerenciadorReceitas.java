package gerencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import auxiliares.Profissional;
import auxiliares.Receita;

public class GerenciadorReceitas {

    /**
     * Cadastra a receita e o profissional associado de forma transacional
     */
    public boolean cadastrarReceitaEProfissional(Connection conn, Receita receita, Profissional profissional)
            throws SQLException {

        String sqlReceita = "INSERT INTO Receitas (id_funcionario, id_pedido, tipo, codigo, cpf_paciente, nome_paciente, data_nascimento, data_validade) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        long idReceita;

        try (PreparedStatement stmtReceita = conn.prepareStatement(sqlReceita, Statement.RETURN_GENERATED_KEYS)) {
            stmtReceita.setInt(1, receita.getIdFuncionario());
            stmtReceita.setLong(2, receita.getIdPedido());
            stmtReceita.setString(3, receita.getTipo());
            stmtReceita.setString(4, receita.getCodigo());
            stmtReceita.setString(5, receita.getCpfPaciente());
            stmtReceita.setString(6, receita.getNomePaciente());
            stmtReceita.setDate(7, java.sql.Date.valueOf(receita.getDataNascimento()));
            stmtReceita.setDate(8, java.sql.Date.valueOf(receita.getDataValidade()));
            stmtReceita.executeUpdate();

            try (ResultSet rs = stmtReceita.getGeneratedKeys()) {
                if (rs.next()) {
                    idReceita = rs.getLong(1);
                } else {
                    throw new SQLException("Falha ao obter ID da receita");
                }
            }
        }

        String sqlProfissional = "INSERT INTO Profissional (id_receita, nome_profissional, tipo_registro, numero_registro) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmtProfissional = conn.prepareStatement(sqlProfissional)) {
            stmtProfissional.setLong(1, idReceita);
            stmtProfissional.setString(2, profissional.getNomeProfissional());
            stmtProfissional.setString(3, profissional.getTipoRegistro());
            stmtProfissional.setString(4, profissional.getNumeroRegistro());
            return stmtProfissional.executeUpdate() > 0;
        }
    }
}