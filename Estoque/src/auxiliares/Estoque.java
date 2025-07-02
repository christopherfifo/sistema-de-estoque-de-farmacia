package auxiliares;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Estoque {

    private final int id;
    private final int quantidade;
    private final int qtdMinima;
    private final String lote;
    private final LocalDate dataFabricacao;
    private final LocalDate dataValidade;
    private final BigDecimal precoVenda;
    private final Produto produto;
    private final AreaEstoque areaEstoque;

    public Estoque(int id, int quantidade, int qtdMinima, String lote, LocalDate dataFabricacao, LocalDate dataValidade,
            BigDecimal precoVenda, Produto produto, AreaEstoque areaEstoque) {
        this.id = id;
        this.quantidade = quantidade;
        this.qtdMinima = qtdMinima;
        this.lote = lote;
        this.dataFabricacao = dataFabricacao;
        this.dataValidade = dataValidade;
        this.precoVenda = precoVenda;
        this.produto = produto;
        this.areaEstoque = areaEstoque;
    }

    public int getId() {
        return id;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public int getQtdMinima() {
        return qtdMinima;
    }

    public String getLote() {
        return lote;
    }

    public LocalDate getDataFabricacao() {
        return dataFabricacao;
    }

    public LocalDate getDataValidade() {
        return dataValidade;
    }

    public BigDecimal getPrecoVenda() {
        return precoVenda;
    }

    public Produto getProduto() {
        return produto;
    }

    public AreaEstoque getAreaEstoque() {
        return areaEstoque;
    }
}