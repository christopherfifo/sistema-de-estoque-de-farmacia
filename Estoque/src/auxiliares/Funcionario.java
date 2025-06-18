package auxiliares;
public class Funcionario {

    private String nome;
    private String cpf;
    private String matricula;
    private String email;
    private String telefone;
    private String tipo; 
    private int idCargo;

    public Funcionario(String nome, String cpf, String matricula, String email, String telefone, String tipo, int idCargo) {
        this.nome = nome;
        this.cpf = cpf;
        this.matricula = matricula;
        this.email = email;
        this.telefone = telefone;
        this.tipo = tipo;
        this.idCargo = idCargo;
    }

    // Getters
    public String getNome() {
        return nome;
    }

    public String getCpf() {
        return cpf;
    }

    public String getMatricula() {
        return matricula;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getTipo() {
        return tipo;
    }

    public int getIdCargo() {
        return idCargo;
    }
}