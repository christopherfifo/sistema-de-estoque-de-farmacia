package auxiliares;

public class Profissional {

    private long idReceita;
    private String nomeProfissional;
    private String tipoRegistro;
    private String numeroRegistro;

    public Profissional(String nomeProfissional, String tipoRegistro, String numeroRegistro) {
        this.nomeProfissional = nomeProfissional;
        this.tipoRegistro = tipoRegistro;
        this.numeroRegistro = numeroRegistro;
    }

    public void setIdReceita(long idReceita) {
        this.idReceita = idReceita;
    }

    public long getIdReceita() {
        return idReceita;
    }

    public String getNomeProfissional() {
        return nomeProfissional;
    }

    public String getTipoRegistro() {
        return tipoRegistro;
    }

    public String getNumeroRegistro() {
        return numeroRegistro;
    }
}