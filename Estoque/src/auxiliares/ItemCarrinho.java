package auxiliares;

import java.math.BigDecimal;

public class ItemCarrinho {
    private Estoque itemEstoque;
    private int quantidadeComprar;

    public ItemCarrinho(Estoque itemEstoque, int quantidadeComprar) {
        this.itemEstoque = itemEstoque;
        this.quantidadeComprar = quantidadeComprar;
    }

    public Estoque getItemEstoque() {
        return itemEstoque;
    }

    public int getQuantidadeComprar() {
        return quantidadeComprar;
    }

    public void setQuantidadeComprar(int quantidadeComprar) {
        this.quantidadeComprar = quantidadeComprar;
    }

    public BigDecimal getSubtotal() {
        BigDecimal precoUnitario = itemEstoque.getProduto().getPreco();
        return precoUnitario.multiply(new BigDecimal(this.quantidadeComprar));
    }
}