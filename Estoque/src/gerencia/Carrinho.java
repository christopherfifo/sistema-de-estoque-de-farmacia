package gerencia;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import auxiliares.Estoque;
import auxiliares.ItemCarrinho;

/**
 * Gerencia os itens do carrinho de compras de uma venda
 */
public class Carrinho {

    private final List<ItemCarrinho> itens;

    public Carrinho() {
        this.itens = new ArrayList<>();
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

    public void removerItem(int idItemEstoque) {
        this.itens.removeIf(item -> item.getItemEstoque().getId() == idItemEstoque);
    }

    public List<ItemCarrinho> getItens() {
        return this.itens;
    }

    public BigDecimal calcularTotal() {
        return this.itens.stream()
                .map(ItemCarrinho::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void limpar() {
        this.itens.clear();
    }
}