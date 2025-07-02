package auxiliares;

public class AreaEstoque {
    private final int id;
    private final String descricao;

    public AreaEstoque(int id, String setor, String prateleira) {
        this.id = id;
        this.descricao = setor + " - " + prateleira;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return this.descricao;
    }
}