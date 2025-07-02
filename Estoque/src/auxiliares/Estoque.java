package auxiliares;

import java.time.LocalDate;

public class Estoque {

    private int id;
    private int quantidade;
    private String lote;
    private LocalDate dataValidade;
    private Produto produto;

    public Estoque(int id, int quantidade, String lote, LocalDate dataValidade, Produto produto) {
        this.id = id;
        this.quantidade = quantidade;
        this.lote = lote;
        this.dataValidade = dataValidade;
        this.produto = produto;
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
}