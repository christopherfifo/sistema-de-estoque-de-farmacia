package auxiliares;

import java.time.LocalDate;

public class Estoque {

    private final int id;
    private final int quantidade;
    private final String lote;
    private final LocalDate dataValidade;
    private final Produto produto;
    private final AreaEstoque areaEstoque;

    public Estoque(int id, int quantidade, String lote, LocalDate dataValidade, Produto produto,
            AreaEstoque areaEstoque) {
        this.id = id;
        this.quantidade = quantidade;
        this.lote = lote;
        this.dataValidade = dataValidade;
        this.produto = produto;
        this.areaEstoque = areaEstoque;
    }

    public int getId() {
        return id;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public String getLote() {
        return lote;
    }

    public LocalDate getDataValidade() {
        return dataValidade;
    }

    public Produto getProduto() {
        return produto;
    }

    public AreaEstoque getAreaEstoque() {
        return areaEstoque;
    }
}