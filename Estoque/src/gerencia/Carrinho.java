package gerencia;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import auxiliares.Estoque;
import auxiliares.ItemCarrinho;

public class Carrinho {

    private final List<ItemCarrinho> itens;
    private BigDecimal percentualDesconto;

    public Carrinho() {
        this.itens = new ArrayList<>();
        this.percentualDesconto = BigDecimal.ZERO;
    }

    public void adicionarItem(Estoque itemEstoque, int quantidade) {
        if (quantidade <= 0) {
            System.err.println("ERRO: Quantidade deve ser positiva");
            return;
        }

        if (quantidade > itemEstoque.getQuantidade()) {
            System.err.println("ERRO: Quantidade solicitada maior que o estoque disponivel");
            return;
        }

        for (ItemCarrinho item : itens) {
            if (item.getItemEstoque().getId() == itemEstoque.getId()) {
                item.setQuantidadeComprar(item.getQuantidadeComprar() + quantidade);
                return;
            }
        }

        this.itens.add(new ItemCarrinho(itemEstoque, quantidade));
    }

    public List<ItemCarrinho> getItens() {
        return this.itens;
    }

    public void aplicarDesconto(BigDecimal percentual) {
        if (percentual.compareTo(BigDecimal.ZERO) >= 0 && percentual.compareTo(new BigDecimal("100")) <= 0) {
            this.percentualDesconto = percentual;
        } else {
            System.err.println("ERRO: Percentual de desconto invalido");
        }
    }

    public BigDecimal getPercentualDesconto() {
        return this.percentualDesconto;
    }

    public BigDecimal getSubtotal() {
        return this.itens.stream()
                .map(ItemCarrinho::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getValorDesconto() {
        BigDecimal subtotal = getSubtotal();
        return subtotal.multiply(this.percentualDesconto)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal calcularTotal() {
        return getSubtotal().subtract(getValorDesconto());
    }

    public void limpar() {
        this.itens.clear();
        this.percentualDesconto = BigDecimal.ZERO;
    }
}