package auxiliares;

import java.time.LocalDate;

public class Receita {

    private long id;
    private int idFuncionario;
    private long idPedido;
    private String tipo;
    private String codigo;
    private String cpfPaciente;
    private String nomePaciente;
    private LocalDate dataNascimento;
    private LocalDate dataValidade;

    public Receita(String tipo, String codigo, String cpfPaciente, String nomePaciente, LocalDate dataNascimento,
            LocalDate dataValidade) {
        this.tipo = tipo;
        this.codigo = codigo;
        this.cpfPaciente = cpfPaciente;
        this.nomePaciente = nomePaciente;
        this.dataNascimento = dataNascimento;
        this.dataValidade = dataValidade;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setIdFuncionario(int idFuncionario) {
        this.idFuncionario = idFuncionario;
    }

    public void setIdPedido(long idPedido) {
        this.idPedido = idPedido;
    }

    public int getIdFuncionario() {
        return idFuncionario;
    }

    public long getIdPedido() {
        return idPedido;
    }

    public String getTipo() {
        return tipo;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getCpfPaciente() {
        return cpfPaciente;
    }

    public String getNomePaciente() {
        return nomePaciente;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public LocalDate getDataValidade() {
        return dataValidade;
    }
}