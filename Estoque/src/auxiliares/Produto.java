package auxiliares;

import java.math.BigDecimal;

public class Produto {

    private int id;
    private String nome;
    private String descricao;
    private String fabricante;
    private String categoria;
    private String tarja;
    private BigDecimal preco;
    private boolean receitaObrigatoria;

    public Produto(int id, String nome, String descricao, String fabricante, String categoria, String tarja,
            BigDecimal preco, boolean receitaObrigatoria) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.fabricante = fabricante;
        this.categoria = categoria;
        this.tarja = tarja;
        this.preco = preco;
        this.receitaObrigatoria = receitaObrigatoria;
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getFabricante() {
        return fabricante;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getTarja() {
        return tarja;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public boolean isReceitaObrigatoria() {
        return receitaObrigatoria;
    }

    @Override
    public String toString() {
        return this.nome + " (" + this.fabricante + ")";
    }
}