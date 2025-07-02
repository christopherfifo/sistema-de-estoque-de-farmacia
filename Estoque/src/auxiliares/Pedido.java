package auxiliares;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Pedido {
    private final long id;
    private final LocalDateTime dataPedido;
    private final BigDecimal valorTotal;
    private final String formaPagamento;
    private final boolean cancelado;

    public Pedido(long id, LocalDateTime dataPedido, BigDecimal valorTotal, String formaPagamento, boolean cancelado) {
        this.id = id;
        this.dataPedido = dataPedido;
        this.valorTotal = valorTotal;
        this.formaPagamento = formaPagamento;
        this.cancelado = cancelado;
    }

    public long getId() {
        return id;
    }

    public LocalDateTime getDataPedido() {
        return dataPedido;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public String getFormaPagamento() {
        return formaPagamento;
    }

    public boolean isCancelado() {
        return cancelado;
    }
}